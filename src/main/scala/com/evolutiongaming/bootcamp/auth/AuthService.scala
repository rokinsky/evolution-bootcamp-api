package com.evolutiongaming.bootcamp.auth

import cats.effect.Sync
import com.evolutiongaming.bootcamp.auth.AuthError.AuthenticationFailed
import com.evolutiongaming.bootcamp.auth.dto.{LoginDto, SignupDto}
import com.evolutiongaming.bootcamp.users.{User, UserService}
import tsec.authentication.{AugmentedJWT, Authenticator}
import tsec.jwt.algorithms.JWTMacAlgo
import tsec.passwordhashers.{PasswordHash, PasswordHasher}
import cats.syntax.all._
import com.evolutiongaming.bootcamp.effects.GenUUID
import tsec.common.Verified

import java.util.UUID

final case class AuthService[F[_]: Sync, A, Auth: JWTMacAlgo](
  userService:   UserService[F],
  cryptService:  PasswordHasher[F, A],
  authenticator: Authenticator[F, UUID, User, AugmentedJWT[Auth, UUID]]
) {
  def loginUser(loginDto: LoginDto): F[AuthPayload[Auth]] = for {
    email <- loginDto.email.pure[F]
    user <- userService
      .getUserByEmail(email)
      .attemptTap(_.leftMap(_ => AuthenticationFailed(email)).liftTo[F])

    _ <- cryptService
      .checkpw(loginDto.password, PasswordHash[A](user.hash))
      .ensure(AuthenticationFailed(email))(_ == Verified)
    token <- authenticator.create(user.id)
  } yield AuthPayload(user, token)

  def signupUser(signupDto: SignupDto): F[AuthPayload[Auth]] = for {
    hash        <- cryptService.hashpw(signupDto.password)
    id          <- GenUUID[F].random
    user        <- signupDto.asUser(id, hash).pure[F]
    createdUser <- userService.createUser(user)
    token       <- authenticator.create(user.id)
  } yield AuthPayload(createdUser, token)
}
