package com.evolutiongaming.bootcamp.auth

import cats.effect.Sync
import cats.syntax.all._
import com.evolutiongaming.bootcamp.auth.AuthError.AuthenticationFailed
import com.evolutiongaming.bootcamp.auth.dto.{LoginDto, SignupDto}
import com.evolutiongaming.bootcamp.effects.GenUUID
import com.evolutiongaming.bootcamp.shared.HttpCommon._
import com.evolutiongaming.bootcamp.users.UserError.{UserAlreadyExists, UserNotFound}
import com.evolutiongaming.bootcamp.users.UserService
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}
import tsec.common.Verified
import tsec.jwt.algorithms.JWTMacAlgo
import tsec.passwordhashers.{PasswordHash, PasswordHasher}

final class AuthHttpEndpoint[F[_]: Sync, A, Auth: JWTMacAlgo](
  userService:  UserService[F],
  cryptService: PasswordHasher[F, A],
  auth:         AuthHandler[F, Auth]
) extends Http4sDsl[F] {
  private def loginEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] { case req @ POST -> Root / "login" =>
      (for {
        login <- req.as[LoginDto]
        email  = login.email
        user <- userService
          .getUserByEmail(email)
          .attemptTap(_.leftMap(_ => AuthenticationFailed(email)).liftTo[F])
        _ <- cryptService
          .checkpw(login.password, PasswordHash[A](user.hash))
          .ensure(AuthenticationFailed(email))(_ == Verified)
        token <- auth.authenticator.create(user.id)
        res   <- Ok(user).map(auth.authenticator.embed(_, token))
      } yield res).handleErrorWith(authErrorInterceptor)
    }

  private def signupEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] { case req @ POST -> Root / "signup" =>
      (for {
        signup      <- req.as[SignupDto]
        hash        <- cryptService.hashpw(signup.password)
        id          <- GenUUID[F].random
        user        <- signup.asUser(id, hash).pure[F]
        createdUser <- userService.createUser(user)
        token       <- auth.authenticator.create(user.id)
        res         <- Created(createdUser).map(auth.authenticator.embed(_, token))
      } yield res).handleErrorWith(authErrorInterceptor)
    }

  private val authErrorInterceptor: PartialFunction[Throwable, F[Response[F]]] = {
    case e @ UserNotFound => NotFound(e.getMessage)
    case e: AuthenticationFailed => BadRequest(e.getMessage)
    case e: UserAlreadyExists    => Conflict(e.getMessage)
    case e: Throwable            => InternalServerError(e.getMessage)
  }

  def endpoints: HttpRoutes[F] = {
    val unAuthEndpoints = loginEndpoint <+> signupEndpoint

    unAuthEndpoints
  }
}

object AuthHttpEndpoint {
  def endpoints[F[_]: Sync, A, Auth: JWTMacAlgo](
    userService:  UserService[F],
    cryptService: PasswordHasher[F, A],
    auth:         AuthHandler[F, Auth],
  ): HttpRoutes[F] =
    new AuthHttpEndpoint[F, A, Auth](userService, cryptService, auth).endpoints
}
