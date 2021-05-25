package com.evolutiongaming.bootcamp.courses

import cats.data.EitherT
import cats.effect.{Clock, Sync}
import cats.syntax.all._
import com.evolutiongaming.bootcamp.applications.{Application, ApplicationService}
import com.evolutiongaming.bootcamp.auth.Auth
import com.evolutiongaming.bootcamp.courses.dto.CreateCourseDto
import com.evolutiongaming.bootcamp.effects.GenUUID
import com.evolutiongaming.bootcamp.shared.HttpCommon._
import com.evolutiongaming.bootcamp.sr.SRHttpClient
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, QueryParamDecoder}
import tsec.authentication._
import tsec.jwt.algorithms.JWTMacAlgo

final class CourseEndpoints[F[_]: Sync: Clock, Auth: JWTMacAlgo](
  courseService:      CourseService[F],
  auth:               AuthHandler[F, Auth],
  srClient:           SRHttpClient[F],
  applicationService: ApplicationService[F]
) extends Http4sDsl[F] {
  implicit val statusQueryParamDecoder: QueryParamDecoder[CourseStatus] =
    QueryParamDecoder[String].map(CourseStatus.withName)

  object StatusMatcher extends OptionalMultiQueryParamDecoderMatcher[CourseStatus]("status")

  private def createCourseEndpoint: AuthEndpoint[F, Auth] = { case req @ POST -> Root asAuthed _ =>
    val action = for {
      createCourseDto <- req.request.as[CreateCourseDto]
      id              <- GenUUID[F].random
      course          <- Course.of(id, createCourseDto).pure[F]
      result          <- courseService.create(course).value
    } yield result

    EitherT(action).foldF(
      e => Conflict(s"The course ${e.course.title} already exists"),
      course => Ok(course)
    )
  }

  private def applyCourseEndpoint: AuthEndpoint[F, Auth] = {
    case req @ POST -> Root / UUIDVar(id) / "apply" asAuthed user =>
      val action = for {
        data   <- req.request.as[String]
        course <- courseService.get(id).value
        application <- course.traverse(course =>
          for {
            applyResponse <- srClient.createPostingCandidate(course.srId, data)
            id            <- GenUUID[F].random
            time          <- Clock[F].instantNow
            application <- applicationService.placeApplication(
              Application(id, user.id, course.id, applyResponse.id, None, time)
            )
          } yield application
        )
      } yield application

      EitherT(action).foldF(
        _ => BadRequest(s"The course with id $id was not found"),
        application => Accepted(application)
      )
  }

  private def getCourseConfigurationEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root / UUIDVar(id) / "configuration" asAuthed _ =>
      val action = for {
        course <- courseService.get(id).value
        _      <- course.traverseTap(course => srClient.getPostingConfiguration(course.srId))
      } yield course

      EitherT(action).foldF(
        _ => BadRequest(s"The course with id $id was not found"),
        course => Accepted(course)
      )
  }

  private def updateCourseEndpoint(): AuthEndpoint[F, Auth] = { case req @ PUT -> Root / UUIDVar(_) asAuthed _ =>
    val action = for {
      course <- req.request.as[Course]
      result <- courseService.update(course).value
    } yield result

    EitherT(action).foldF(
      _ => NotFound("The course was not found"),
      course => Ok(course)
    )
  }

  private def getCourseEndpoint: AuthEndpoint[F, Auth] = { case GET -> Root / UUIDVar(id) asAuthed _ =>
    courseService.get(id).foldF(_ => NotFound("The course was not found"), course => Ok(course))
  }

  private def deleteCourseEndpoint(): AuthEndpoint[F, Auth] = { case DELETE -> Root / UUIDVar(id) asAuthed _ =>
    for {
      _   <- courseService.delete(id)
      res <- Ok()
    } yield res
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

object CourseEndpoints {
  def endpoints[F[_]: Sync: Clock, Auth: JWTMacAlgo](
    courseService:      CourseService[F],
    auth:               AuthHandler[F, Auth],
    srClient:           SRHttpClient[F],
    applicationService: ApplicationService[F]
  ): HttpRoutes[F] =
    new CourseEndpoints[F, Auth](courseService, auth, srClient, applicationService).endpoints
}
