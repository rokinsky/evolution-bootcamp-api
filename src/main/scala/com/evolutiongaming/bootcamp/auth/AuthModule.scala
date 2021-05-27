package com.evolutiongaming.bootcamp.auth

import cats.effect.Sync
import com.evolutiongaming.bootcamp.users.{User, UserDoobieRepository, UserService}
import doobie.Transactor
import org.http4s.HttpRoutes
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler}
import tsec.mac.jca.{HMACSHA256, MacSigningKey}
import tsec.passwordhashers.jca.BCrypt

import java.util.UUID

trait AuthModule[F[_]] {
  def routeAuth:        SecuredRequestHandler[F, UUID, User, AugmentedJWT[HMACSHA256, UUID]]
  def authHttpEndpoint: HttpRoutes[F]
}

object AuthModule {
  def of[F[_]: Sync](xa: Transactor[F], key: MacSigningKey[HMACSHA256]): AuthModule[F] = new AuthModule[F] {
    private val userRepo      = UserDoobieRepository(xa)
    private val userService   = UserService(userRepo)
    private val authRepo      = AuthDoobieRepository[F, HMACSHA256](key, xa)
    private val authenticator = Auth.jwtAuthenticator[F, HMACSHA256](key, authRepo, userRepo)

    override def routeAuth: SecuredRequestHandler[F, UUID, User, AugmentedJWT[HMACSHA256, UUID]] =
      SecuredRequestHandler(authenticator)

    override def authHttpEndpoint: HttpRoutes[F] =
      AuthHttpEndpoint.endpoints(userService, BCrypt.syncPasswordHasher[F], routeAuth)
  }
}
