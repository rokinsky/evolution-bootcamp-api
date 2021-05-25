package com.evolutiongaming.bootcamp.applications

import cats.Monad
import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all._
import com.evolutiongaming.bootcamp.applications.dto.ApplicationSubmitDto
import com.evolutiongaming.bootcamp.auth.Auth
import com.evolutiongaming.bootcamp.courses.CourseService
import com.evolutiongaming.bootcamp.shared.HttpCommon.{AuthEndpoint, AuthHandler, AuthService}
import com.evolutiongaming.bootcamp.sr.{SRApplicationStatus, SRApplicationWebhookPayload, SRHttpClient}
import org.http4s._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.util.CaseInsensitiveString
import tsec.authentication.asAuthed
import tsec.jwt.algorithms.JWTMacAlgo

final class ApplicationEndpoints[F[_]: Sync, Auth: JWTMacAlgo](
  applicationService: ApplicationService[F],
  auth:               AuthHandler[F, Auth],
  srClient:           SRHttpClient[F],
  courseService:      CourseService[F],
) extends Http4sDsl[F] {
  private def placeApplicationEndpoint: AuthEndpoint[F, Auth] = { case req @ POST -> Root asAuthed user =>
    for {
      application <- req.request
        .as[Application]
        .map(_.copy(userId = user.id))
      savedApplication <- applicationService.placeApplication(application)
      res              <- Ok(savedApplication)
    } yield res
  }

  private def submitApplicationSolutionEndpoint: AuthEndpoint[F, Auth] = {
    case req @ PATCH -> Root / UUIDVar(id) / "submit" asAuthed user =>
      val action = for {
        applicationSubmitDto <- req.request.as[ApplicationSubmitDto]
        savedApplication <- applicationService.updateApplicationSolution(
          id,
          user.id,
          applicationSubmitDto.solutionMessage.some
        )
      } yield savedApplication

      OptionT(action).foldF(BadRequest())(application => Accepted(application))
  }

  private def getApplicationEndpoint: AuthEndpoint[F, Auth] = { case GET -> Root / UUIDVar(id) asAuthed _ =>
    applicationService
      .get(id)
      .foldF(
        _ => NotFound("The application was not found"),
        application => Ok(application)
      )
  }

  private def deleteApplicationEndpoint(): AuthEndpoint[F, Auth] = { case DELETE -> Root / UUIDVar(id) asAuthed _ =>
    for {
      _   <- applicationService.delete(id)
      res <- Ok()
    } yield res
  }

  // https://dev.smartrecruiters.com/customer-api/live-docs/webhooks-subscriptions-api/#/subscriptions/subscriptions.activate
  private def hookApplicationEndpoint(): HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root / "hook" =>
    val action = for {
      applicationPayload <- req.as[SRApplicationWebhookPayload]
      candidateStatus    <- srClient.getCandidateStatus(applicationPayload.jobId, applicationPayload.candidateId)
      application <- applicationService.updateApplicationStatusBySR(
        applicationPayload.candidateId,
        candidateStatus.status
      )
      course <- courseService.getBySR(applicationPayload.jobId).value
      _ <- application.traverseTap {
        case Application(_, _, _, _, _, _, _, SRApplicationStatus.IN_REVIEW) =>
          srClient.sendCandidateEmail(applicationPayload.candidateId, "").void
        case _ => Monad[F].unit
      }
      res <- Accepted()
    } yield res

    req.headers
      .get(CaseInsensitiveString("X-Hook-Secret"))
      .fold(action)(header => Ok().map(_.withHeaders(header)))
  }

  def endpoints: HttpRoutes[F] = {
    val authEndpoints: AuthService[F, Auth] =
      Auth.allRolesHandler(
        placeApplicationEndpoint.orElse(getApplicationEndpoint).orElse(submitApplicationSolutionEndpoint),
      )(
        Auth.adminOnly(deleteApplicationEndpoint())
      )

    auth.liftService(authEndpoints) <+> hookApplicationEndpoint()
  }
}

object ApplicationEndpoints {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](
    applicationService: ApplicationService[F],
    auth:               AuthHandler[F, Auth],
    srClient:           SRHttpClient[F],
    courseService:      CourseService[F],
  ): HttpRoutes[F] =
    new ApplicationEndpoints[F, Auth](applicationService, auth, srClient, courseService).endpoints
}
