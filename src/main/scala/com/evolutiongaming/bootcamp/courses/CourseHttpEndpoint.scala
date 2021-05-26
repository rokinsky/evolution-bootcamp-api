package com.evolutiongaming.bootcamp.courses

import cats.effect.{Clock, Sync}
import cats.syntax.all._
import com.evolutiongaming.bootcamp.applications.{Application, ApplicationService}
import com.evolutiongaming.bootcamp.auth.Auth
import com.evolutiongaming.bootcamp.courses.CourseError.{CourseAlreadyExists, CourseNotFound}
import com.evolutiongaming.bootcamp.courses.dto.{CreateCourseDto, UpdateCourseDto}
import com.evolutiongaming.bootcamp.effects.GenUUID
import com.evolutiongaming.bootcamp.shared.HttpCommon._
import com.evolutiongaming.bootcamp.sr.SRHttpClient
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, QueryParamDecoder, Response}
import tsec.authentication._
import tsec.jwt.algorithms.JWTMacAlgo

final class CourseHttpEndpoint[F[_]: Sync: Clock, Auth: JWTMacAlgo](
  courseService:      CourseService[F],
  auth:               AuthHandler[F, Auth],
  srClient:           SRHttpClient[F],
  applicationService: ApplicationService[F]
) extends Http4sDsl[F] {
  implicit val statusQueryParamDecoder: QueryParamDecoder[CourseStatus] =
    QueryParamDecoder[String].map(CourseStatus.withName)

  object StatusMatcher extends OptionalMultiQueryParamDecoderMatcher[CourseStatus]("status")

  private def createCourseEndpoint: AuthEndpoint[F, Auth] = { case req @ POST -> Root asAuthed _ =>
    (for {
      createCourseDto <- req.request.as[CreateCourseDto]
      id              <- GenUUID[F].random
      course          <- Course.of(id, createCourseDto).pure[F]
      createdCourse   <- courseService.create(course)
      res             <- Ok(createdCourse)
    } yield res).handleErrorWith(courseErrorInterceptor)
  }

  private def applyCourseEndpoint: AuthEndpoint[F, Auth] = {
    case req @ POST -> Root / UUIDVar(id) / "apply" asAuthed user =>
      (for {
        data          <- req.request.as[String]
        course        <- courseService.get(id)
        applyResponse <- srClient.createPostingCandidate(course.srId, data)
        id            <- GenUUID[F].random
        time          <- Clock[F].instantNow
        application <- applicationService.placeApplication(
          Application(id, user.id, course.id, applyResponse.id, None, time)
        )
        res <- Accepted(application)
      } yield res).handleErrorWith(courseErrorInterceptor)
  }

  private def getCourseConfigurationEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root / UUIDVar(id) / "configuration" asAuthed _ =>
      (for {
        course <- courseService.get(id)
        _      <- srClient.getPostingConfiguration(course.srId)
        res    <- Ok(course)
      } yield res).handleErrorWith(courseErrorInterceptor)
  }

  private def updateCourseEndpoint(): AuthEndpoint[F, Auth] = { case req @ PUT -> Root / UUIDVar(id) asAuthed _ =>
    (for {
      updateCourseDto <- req.request.as[UpdateCourseDto]
      course          <- Course.of(id, updateCourseDto).pure[F]
      updatedCourse   <- courseService.update(course)
      res             <- Accepted(updatedCourse)
    } yield res).handleErrorWith(courseErrorInterceptor)
  }

  private def getCourseEndpoint: AuthEndpoint[F, Auth] = { case GET -> Root / UUIDVar(id) asAuthed _ =>
    (for {
      course <- courseService.get(id)
      res    <- Ok(course)
    } yield res).handleErrorWith(courseErrorInterceptor)
  }

  private def deleteCourseEndpoint(): AuthEndpoint[F, Auth] = { case DELETE -> Root / UUIDVar(id) asAuthed _ =>
    (for {
      _   <- courseService.delete(id)
      res <- Accepted()
    } yield res).handleErrorWith(courseErrorInterceptor)
  }

  private def listCoursesEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root :? OptionalPageSizeMatcher(pageSize) :? OptionalOffsetMatcher(
          offset,
        ) asAuthed _ =>
      for {
        courses <- courseService.list(pageSize.getOrElse(10), offset.getOrElse(0))
        res     <- Ok(courses)
      } yield res
  }

  private val courseErrorInterceptor: PartialFunction[Throwable, F[Response[F]]] = {
    case e: CourseNotFound      => NotFound(e.getMessage)
    case e: CourseAlreadyExists => BadRequest(e.getMessage)
    case e: Throwable           => InternalServerError(e.getMessage)
  }

  def endpoints: HttpRoutes[F] = {
    val authEndpoints: AuthService[F, Auth] = {
      val allRoles =
        getCourseEndpoint
          .orElse(listCoursesEndpoint)
          .orElse(applyCourseEndpoint)
          .orElse(getCourseConfigurationEndpoint)
      val onlyAdmin =
        createCourseEndpoint
          .orElse(deleteCourseEndpoint())
          .orElse(updateCourseEndpoint())

      Auth.allRolesHandler(allRoles)(Auth.adminOnly(onlyAdmin))
    }

    auth.liftService(authEndpoints)
  }
}

object CourseHttpEndpoint {
  def endpoints[F[_]: Sync: Clock, Auth: JWTMacAlgo](
    courseService:      CourseService[F],
    auth:               AuthHandler[F, Auth],
    srClient:           SRHttpClient[F],
    applicationService: ApplicationService[F]
  ): HttpRoutes[F] =
    new CourseHttpEndpoint[F, Auth](courseService, auth, srClient, applicationService).endpoints
}
