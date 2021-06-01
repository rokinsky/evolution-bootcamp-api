package com.evolutiongaming.bootcamp.applications

import cats.effect.Sync
import cats.syntax.all._
import com.evolutiongaming.bootcamp.applications.ApplicationError.{
  ApplicationAlreadyExists,
  ApplicationNotFound,
  ApplicationNotPending,
  ApplicationSolutionAlreadyExists
}
import com.evolutiongaming.bootcamp.applications.dto.{ApplicationSubmitDto, CreateApplicationDto, UpdateApplicationDto}
import com.evolutiongaming.bootcamp.auth.Auth
import com.evolutiongaming.bootcamp.courses.CourseError.CourseNotFound
import com.evolutiongaming.bootcamp.courses.CourseService
import com.evolutiongaming.bootcamp.shared.HttpCommon._
import com.evolutiongaming.bootcamp.sr.dto.SRApplicationWebhookPayload
import com.evolutiongaming.bootcamp.sr.{SRApplicationStatus, SRHttpClient}
import org.http4s._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.util.CaseInsensitiveString
import tsec.authentication.asAuthed
import tsec.jwt.algorithms.JWTMacAlgo

final class ApplicationHttpEndpoint[F[_]: Sync, Auth: JWTMacAlgo](
  applicationService: ApplicationService[F],
  auth:               AuthHandler[F, Auth],
  srClient:           SRHttpClient[F],
  courseService:      CourseService[F],
) extends Http4sDsl[F] {
  private def createApplicationEndpoint: AuthEndpoint[F, Auth] = { case req @ POST -> Root asAuthed _ =>
    (for {
      createApplicationDto <- req.request.as[CreateApplicationDto]
      savedApplication     <- applicationService.create(createApplicationDto)
      res                  <- Ok(savedApplication)
    } yield res).handleErrorWith(applicationErrorInterceptor)
  }

  private def getApplicationEndpoint: AuthEndpoint[F, Auth] = { case GET -> Root / UUIDVar(id) asAuthed _ =>
    (for {
      application <- applicationService.get(id)
      res         <- Ok(application)
    } yield res).handleErrorWith(applicationErrorInterceptor)
  }

  private def updateApplicationEndpoint(): AuthEndpoint[F, Auth] = { case req @ PUT -> Root / UUIDVar(id) asAuthed _ =>
    (for {
      updateApplicationDto <- req.request.as[UpdateApplicationDto]
      application          <- Application.of(id, updateApplicationDto).pure[F]
      updatedApplication   <- applicationService.update(application)
      res                  <- Accepted(updatedApplication)
    } yield res).handleErrorWith(applicationErrorInterceptor)
  }

  private def deleteApplicationEndpoint(): AuthEndpoint[F, Auth] = { case DELETE -> Root / UUIDVar(id) asAuthed _ =>
    (for {
      _   <- applicationService.delete(id)
      res <- Accepted()
    } yield res).handleErrorWith(applicationErrorInterceptor)
  }

  private def listApplicationsEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root :? OptionalPageSizeMatcher(pageSize) :? OptionalOffsetMatcher(
          offset,
        ) asAuthed _ =>
      for {
        applications <- applicationService.list(pageSize.getOrElse(10), offset.getOrElse(0))
        res          <- Ok(applications)
      } yield res
  }

  private def submitUserApplicationSolutionEndpoint: AuthEndpoint[F, Auth] = {
    case req @ PATCH -> Root / UUIDVar(id) / "submit" asAuthed user =>
      (for {
        applicationSubmitDto <- req.request.as[ApplicationSubmitDto]
        savedApplication <- applicationService.updateApplicationSolution(
          id,
          user.id,
          applicationSubmitDto.solutionMessage.some
        )
        res <- Accepted(savedApplication)
      } yield res).handleErrorWith(applicationErrorInterceptor)
  }

  private def getUserApplicationEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root / "user" / UUIDVar(id) asAuthed user =>
      (for {
        application <- applicationService.getUserApplication(id, user.id)
        res         <- Ok(application)
      } yield res).handleErrorWith(applicationErrorInterceptor)
  }

  private def listUserApplicationsEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root / "user" :? OptionalPageSizeMatcher(pageSize) :? OptionalOffsetMatcher(
          offset,
        ) asAuthed user =>
      for {
        applications <- applicationService.listByUserId(user.id, pageSize.getOrElse(10), offset.getOrElse(0))
        res          <- Ok(applications)
      } yield res
  }

  // https://dev.smartrecruiters.com/customer-api/live-docs/webhooks-subscriptions-api/#/subscriptions/subscriptions.activate
  private def hookApplicationEndpoint(): HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root / "hook" =>
    val action = (for {
      applicationPayload <- req.as[SRApplicationWebhookPayload]
      candidateStatus    <- srClient.getCandidateStatus(applicationPayload.jobId, applicationPayload.candidateId)
      application <- applicationService.updateApplicationStatusBySR(
        applicationPayload.candidateId,
        candidateStatus.status
      )
      course <- courseService.getBySR(applicationPayload.jobId)
      _ <- srClient
        .sendCandidateEmail(applicationPayload.candidateId, course.taskMessage)
        .whenA(application.status == SRApplicationStatus.IN_REVIEW)
      res <- Accepted()
    } yield res)
      .recoverWith {
        // We will receive events from the whole company's space so we should ignore irrelevant ones
        case _: CourseNotFound      => Accepted()
        case _: ApplicationNotFound => Accepted()
      }
      .handleErrorWith(applicationErrorInterceptor)

    req.headers
      .get(CaseInsensitiveString("X-Hook-Secret"))
      .fold(action)(header => Ok().map(_.withHeaders(header)))
  }

  private val applicationErrorInterceptor: PartialFunction[Throwable, F[Response[F]]] = {
    case e: ApplicationAlreadyExists         => Conflict(e.getMessage)
    case e: ApplicationSolutionAlreadyExists => Conflict(e.getMessage)
    case e: ApplicationNotPending            => BadRequest(e.getMessage)
    case e: ApplicationNotFound              => NotFound(e.getMessage)
    case e: Throwable                        => InternalServerError(e.getMessage)
  }

  def endpoints: HttpRoutes[F] = {
    val allRoles =
      getUserApplicationEndpoint
        .orElse(submitUserApplicationSolutionEndpoint)
        .orElse(listUserApplicationsEndpoint)
    val onlyAdmin =
      listApplicationsEndpoint
        .orElse(getApplicationEndpoint)
        .orElse(createApplicationEndpoint)
        .orElse(updateApplicationEndpoint())
        .orElse(deleteApplicationEndpoint())

    val authEndpoints: AuthService[F, Auth] =
      Auth.allRolesHandler(allRoles)(Auth.adminOnly(onlyAdmin))

    val unAuthEndpoints = hookApplicationEndpoint()

    unAuthEndpoints <+> auth.liftService(authEndpoints)
  }
}

object ApplicationHttpEndpoint {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](
    applicationService: ApplicationService[F],
    auth:               AuthHandler[F, Auth],
    srClient:           SRHttpClient[F],
    courseService:      CourseService[F],
  ): HttpRoutes[F] =
    new ApplicationHttpEndpoint[F, Auth](applicationService, auth, srClient, courseService).endpoints
}
