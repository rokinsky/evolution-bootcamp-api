package com.evolutiongaming.bootcamp.auth

import cats.data._
import cats.effect.Bracket
import cats.syntax.all._
import doobie._
import doobie.implicits._
import tsec.authentication.{AugmentedJWT, BackingStore}
import tsec.common.SecureRandomId
import tsec.jws.JWSSerializer
import tsec.jws.mac.{JWSMacCV, JWSMacHeader, JWTMacImpure}
import tsec.mac.jca.{MacErrorM, MacSigningKey}

import java.util.UUID

final class AuthDoobieRepository[
  F[_]: Bracket[*[_], Throwable],
  A:    Lambda[a => JWSSerializer[JWSMacHeader[a]]]: JWSMacCV[MacErrorM, *]
](
  key: MacSigningKey[A],
  xa:  Transactor[F],
) extends BackingStore[F, SecureRandomId, AugmentedJWT[A, UUID]] {
  override def put(jwt: AugmentedJWT[A, UUID]): F[AugmentedJWT[A, UUID]] =
    AuthQuery.insert(jwt).run.transact(xa).as(jwt)

  override def update(jwt: AugmentedJWT[A, UUID]): F[AugmentedJWT[A, UUID]] =
    AuthQuery.update(jwt).run.transact(xa).as(jwt)

  override def delete(id: SecureRandomId): F[Unit] =
    AuthQuery.delete(id).run.transact(xa).void

  override def get(id: SecureRandomId): OptionT[F, AugmentedJWT[A, UUID]] =
    OptionT(AuthQuery.select(id).option.transact(xa)).semiflatMap {
      case (jwtStringify, identity, expiry, lastTouched) =>
        JWTMacImpure
          .verifyAndParse(jwtStringify, key)
          .fold(_.raiseError[F, AugmentedJWT[A, UUID]], AugmentedJWT(id, _, identity, expiry, lastTouched).pure[F])
    }
}

object AuthDoobieRepository {
  def apply[F[_]: Bracket[*[_], Throwable], A: Lambda[a => JWSSerializer[JWSMacHeader[a]]]: JWSMacCV[MacErrorM, *]](
    key: MacSigningKey[A],
    xa:  Transactor[F]
  ): AuthDoobieRepository[F, A] =
    new AuthDoobieRepository(key, xa)
}
