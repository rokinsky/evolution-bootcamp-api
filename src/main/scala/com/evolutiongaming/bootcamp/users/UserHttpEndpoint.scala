package com.evolutiongaming.bootcamp.users

import cats.effect.Sync
import cats.syntax.all._
import com.evolutiongaming.bootcamp.auth.{Auth, LoginDto, SignupDto}
import com.evolutiongaming.bootcamp.effects.GenUUID
import com.evolutiongaming.bootcamp.shared.HttpCommon._
import com.evolutiongaming.bootcamp.users.dto.UpdateUserDto
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}
import tsec.authentication._
import tsec.common.Verified
import tsec.jwt.algorithms.JWTMacAlgo
import tsec.passwordhashers.{PasswordHash, PasswordHasher}

final class UserHttpEndpoint[F[_]: Sync, A, Auth: JWTMacAlgo](
  userService:  UserService[F],
  cryptService: PasswordHasher[F, A],
  auth:         AuthHandler[F, Auth]
) extends Http4sDsl[F] {
  private def loginEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] { case req @ POST -> Root / "login" =>
      (for {
        login <- req.as[LoginDto]
        email <- login.email.pure[F]
        user  <- userService.getUserByEmail(email)
        _ <- cryptService
          .checkpw(login.password, PasswordHash[A](user.hash))
          .ensure(UserAuthenticationFailed(email))(_ == Verified)
        token <- auth.authenticator.create(user.id)
        res   <- Ok(user).map(auth.authenticator.embed(_, token))
      } yield res).handleErrorWith(userErrorInterceptor)
    }

  private def signupEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] { case req @ POST -> Root =>
      (for {
        signup      <- req.as[SignupDto]
        hash        <- cryptService.hashpw(signup.password)
        id          <- GenUUID[F].random
        user         = signup.asUser(id, hash)
        createdUser <- userService.createUser(user)
        res         <- Created(createdUser)
      } yield res).handleErrorWith(userErrorInterceptor)
    }

  private def updateEndpoint(): AuthEndpoint[F, Auth] = { case req @ PUT -> Root / UUIDVar(id) asAuthed _ =>
    (for {
      updateUserDto <- req.request.as[UpdateUserDto]
      user           = User.of(id, updateUserDto)
      updatedUser   <- userService.update(user)
      res           <- Ok(updatedUser)
    } yield res).handleErrorWith(userErrorInterceptor)
  }

  private def listEndpoint: AuthEndpoint[F, Auth] = {
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
    case UserAuthenticationFailed(email) => BadRequest(s"Authentication failed for user with email ${email}")
    case UserAlreadyExists(user)         => Conflict(s"The user with email ${user.email} already exists")
    case UserNotFound                    => NotFound("The user was not found")
    case ex: Throwable => InternalServerError(ex.getMessage) // TODO: probably we don't need this
  }

  def endpoints: HttpRoutes[F] = {
    val authEndpoints: AuthService[F, Auth] =
      Auth.adminOnly {
        updateEndpoint()
          .orElse(listEndpoint)
          .orElse(getUserEndpoint)
          .orElse(deleteUserEndpoint())
      }

    val unAuthEndpoints = loginEndpoint <+> signupEndpoint

    unAuthEndpoints <+> auth.liftService(authEndpoints)
  }
}

object UserHttpEndpoint {
  def endpoints[F[_]: Sync, A, Auth: JWTMacAlgo](
    userService:  UserService[F],
    cryptService: PasswordHasher[F, A],
    auth:         AuthHandler[F, Auth],
  ): HttpRoutes[F] =
    new UserHttpEndpoint[F, A, Auth](userService, cryptService, auth).endpoints
}
