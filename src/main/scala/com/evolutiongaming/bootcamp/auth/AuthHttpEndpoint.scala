package com.evolutiongaming.bootcamp.auth

import cats.effect.Sync
import cats.syntax.all._
import com.evolutiongaming.bootcamp.auth.AuthError.AuthenticationFailed
import com.evolutiongaming.bootcamp.auth.dto.{LoginDto, SignupDto}
import com.evolutiongaming.bootcamp.shared.HttpCommon.AuthHandler
import com.evolutiongaming.bootcamp.users.UserError.{UserAlreadyExists, UserNotFound}
import com.evolutiongaming.bootcamp.users.{User, UserService}
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}
import tsec.authentication.{AugmentedJWT, Authenticator}
import tsec.jwt.algorithms.JWTMacAlgo
import tsec.passwordhashers.PasswordHasher

import java.util.UUID

final class AuthHttpEndpoint[F[_]: Sync, A, Auth: JWTMacAlgo](
  authService:   AuthService[F, A, Auth],
  authenticator: Authenticator[F, UUID, User, AugmentedJWT[Auth, UUID]]
) extends Http4sDsl[F] {
  private def loginEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] { case req @ POST -> Root / "login" =>
      (for {
        loginDto <- req.as[LoginDto]
        payload  <- authService.loginUser(loginDto)
        res      <- Ok(payload.user).map(authenticator.embed(_, payload.token))
      } yield res).handleErrorWith(authErrorInterceptor)
    }

  private def signupEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] { case req @ POST -> Root / "signup" =>
      (for {
        signupDto <- req.as[SignupDto]
        payload   <- authService.signupUser(signupDto)
        res       <- Created(payload.user).map(authenticator.embed(_, payload.token))
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
    new AuthHttpEndpoint[F, A, Auth](
      AuthService(userService, cryptService, auth.authenticator),
      auth.authenticator
    ).endpoints
}
