package com.evolutiongaming.bootcamp.auth

import cats.Id
import cats.syntax.all._
import doobie.implicits.toSqlInterpolator
import doobie.postgres.Instances
import doobie.util.meta.LegacyInstantMetaInstance
import doobie.{Put, Query0, Update0}
import tsec.authentication.AugmentedJWT
import tsec.common.SecureRandomId
import tsec.jws.JWSSerializer
import tsec.jws.mac.JWSMacHeader

import java.time.Instant
import java.util.UUID

object AuthQuery extends LegacyInstantMetaInstance with Instances {
  implicit val secureRandomIdPut: Put[SecureRandomId] =
    Put[String].contramap((_: Id[SecureRandomId]).widen)

  def insert[A: Lambda[a => JWSSerializer[JWSMacHeader[a]]]](jwt: AugmentedJWT[A, UUID]): Update0 = sql"""
    INSERT INTO auth (id, jwt, identity, expiry, last_touched)
    VALUES (${jwt.id}, ${jwt.jwt.toEncodedString}, ${jwt.identity}, ${jwt.expiry}, ${jwt.lastTouched})
  """.update

  def update[A: Lambda[a => JWSSerializer[JWSMacHeader[a]]]](jwt: AugmentedJWT[A, UUID]): Update0 = sql"""
    UPDATE auth 
    SET jwt = ${jwt.jwt.toEncodedString}, identity = ${jwt.identity}, expiry = ${jwt.expiry}, last_touched = ${jwt.lastTouched} 
    WHERE id = ${jwt.id}
  """.update

  def delete(id: SecureRandomId): Update0 = sql"""
    DELETE 
    FROM auth 
    WHERE id = $id
  """.update

  def select(id: SecureRandomId): Query0[(String, UUID, Instant, Option[Instant])] = sql"""
    SELECT jwt, identity, expiry, last_touched 
    FROM auth 
    WHERE id = $id
  """.query[(String, UUID, Instant, Option[Instant])]
}
