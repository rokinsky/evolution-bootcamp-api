package com.evolutiongaming.bootcamp.auth

import cats.effect._
import com.evolutiongaming.bootcamp.users.{Role, User}
import org.http4s.Response
import tsec.authentication.{
  AugmentedJWT,
  BackingStore,
  IdentityStore,
  JWTAuthenticator,
  SecuredRequest,
  TSecAuthService
}
import tsec.authorization.BasicRBAC
import tsec.common.SecureRandomId
import tsec.jws.mac.JWSMacCV
import tsec.jwt.algorithms.JWTMacAlgo
import tsec.mac.jca.MacSigningKey

import java.util.UUID
import scala.concurrent.duration._

object Auth {
  def jwtAuthenticator[F[_]: JWSMacCV[*[_], Auth]: Sync, Auth: JWTMacAlgo](
    key:      MacSigningKey[Auth],
    authRepo: BackingStore[F, SecureRandomId, AugmentedJWT[Auth, UUID]],
    userRepo: IdentityStore[F, UUID, User],
  ): JWTAuthenticator[F, UUID, User, Auth] =
    JWTAuthenticator.backed.inBearerToken(
      expiryDuration = 1.hour,
      maxIdle        = None,
      tokenStore     = authRepo,
      identityStore  = userRepo,
      signingKey     = key,
    )

  def allRoles[F[_]: MonadThrow, Auth](
    pf: PartialFunction[SecuredRequest[F, User, AugmentedJWT[Auth, UUID]], F[Response[F]]],
  ): TSecAuthService[User, AugmentedJWT[Auth, UUID], F] =
    TSecAuthService.withAuthorization(BasicRBAC.all[F, Role, User, AugmentedJWT[Auth, UUID]])(pf)

  def allRolesHandler[F[_]: MonadThrow, Auth](
    pf: PartialFunction[SecuredRequest[F, User, AugmentedJWT[Auth, UUID]], F[Response[F]]],
  )(
    onNotAuthorized: TSecAuthService[User, AugmentedJWT[Auth, UUID], F],
  ): TSecAuthService[User, AugmentedJWT[Auth, UUID], F] =
    TSecAuthService.withAuthorizationHandler(BasicRBAC.all[F, Role, User, AugmentedJWT[Auth, UUID]])(
      pf,
      onNotAuthorized.run,
    )

  def adminOnly[F[_]: MonadThrow, Auth](
    pf: PartialFunction[SecuredRequest[F, User, AugmentedJWT[Auth, UUID]], F[Response[F]]],
  ): TSecAuthService[User, AugmentedJWT[Auth, UUID], F] =
    TSecAuthService.withAuthorization(BasicRBAC[F, Role, User, AugmentedJWT[Auth, UUID]](Role.Admin))(pf)
}
