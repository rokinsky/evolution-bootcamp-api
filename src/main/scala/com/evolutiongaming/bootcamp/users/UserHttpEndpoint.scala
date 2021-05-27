package com.evolutiongaming.bootcamp.users

import cats.effect.Sync
import cats.syntax.all._
import com.evolutiongaming.bootcamp.auth.Auth
import com.evolutiongaming.bootcamp.effects.GenUUID
import com.evolutiongaming.bootcamp.shared.HttpCommon._
import com.evolutiongaming.bootcamp.users.UserError.{UserAlreadyExists, UserNotFound}
import com.evolutiongaming.bootcamp.users.dto.{CreateUserDto, UpdateUserDto}
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}
import tsec.authentication._
import tsec.jwt.algorithms.JWTMacAlgo

final class UserHttpEndpoint[F[_]: Sync, A, Auth: JWTMacAlgo](
  userService: UserService[F],
  auth:        AuthHandler[F, Auth]
) extends Http4sDsl[F] {
  private def createUserEndpoint: AuthEndpoint[F, Auth] = { case req @ POST -> Root asAuthed _ =>
    (for {
      createUserDto <- req.request.as[CreateUserDto]
      id            <- GenUUID[F].random
      user          <- User.of(id, createUserDto).pure[F]
      createdUser   <- userService.createUser(user)
      res           <- Created(createdUser)
    } yield res).handleErrorWith(userErrorInterceptor)
  }

  private def updateUserEndpoint(): AuthEndpoint[F, Auth] = { case req @ PUT -> Root / UUIDVar(id) asAuthed _ =>
    (for {
      updateUserDto <- req.request.as[UpdateUserDto]
      user           = User.of(id, updateUserDto)
      updatedUser   <- userService.update(user)
      res           <- Ok(updatedUser)
    } yield res).handleErrorWith(userErrorInterceptor)
  }

  private def listUsersEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root :? OptionalPageSizeMatcher(pageSize) :? OptionalOffsetMatcher(offset) asAuthed _ =>
      for {
        users <- userService.list(pageSize.getOrElse(10), offset.getOrElse(0))
        res   <- Ok(users)
      } yield res
  }

  private def getUserEndpoint: AuthEndpoint[F, Auth] = { case GET -> Root / UUIDVar(id) asAuthed _ =>
    (for {
      user <- userService.getUser(id)
      res  <- Ok(user)
    } yield res).handleErrorWith(userErrorInterceptor)
  }

  private def deleteUserEndpoint(): AuthEndpoint[F, Auth] = { case DELETE -> Root / UUIDVar(id) asAuthed _ =>
    (for {
      _   <- userService.deleteUser(id)
      res <- Accepted()
    } yield res).handleErrorWith(userErrorInterceptor)
  }

  private val userErrorInterceptor: PartialFunction[Throwable, F[Response[F]]] = {
    case e @ UserNotFound => NotFound(e.getMessage)
    case e: UserAlreadyExists => Conflict(e.getMessage)
    case e: Throwable         => InternalServerError(e.getMessage)
  }

  def endpoints: HttpRoutes[F] = {
    val authEndpoints: AuthService[F, Auth] =
      Auth.adminOnly {
        createUserEndpoint
          .orElse(updateUserEndpoint())
          .orElse(listUsersEndpoint)
          .orElse(getUserEndpoint)
          .orElse(deleteUserEndpoint())
      }

    auth.liftService(authEndpoints)
  }
}

object UserHttpEndpoint {
  def endpoints[F[_]: Sync, A, Auth: JWTMacAlgo](
    userService: UserService[F],
    auth:        AuthHandler[F, Auth],
  ): HttpRoutes[F] =
    new UserHttpEndpoint[F, A, Auth](userService, auth).endpoints
}
