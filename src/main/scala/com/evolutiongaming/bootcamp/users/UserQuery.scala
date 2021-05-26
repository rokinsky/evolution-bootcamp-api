package com.evolutiongaming.bootcamp.users

import cats.syntax.all._
import doobie.implicits._
import doobie.postgres.Instances
import doobie.util.meta.LegacyInstantMetaInstance
import doobie.{Meta, Query0, Update0}
import io.circe.parser.decode
import io.circe.syntax.EncoderOps

import java.util.UUID

object UserQuery extends LegacyInstantMetaInstance with Instances {
  implicit val roleMeta: Meta[Role] =
    Meta[String].imap(decode[Role](_).leftMap(throw _).merge)(_.asJson.toString)

  def createTable: Update0 = sql"""
    CREATE TABLE IF NOT EXISTS users (
      id UUID PRIMARY KEY,
      firstname VARCHAR NOT NULL,
      lastname VARCHAR NOT NULL,
      email VARCHAR NOT NULL UNIQUE,
      hash VARCHAR NOT NULL,
      role VARCHAR NOT NULL DEFAULT '"Student"'
    )
  """.update

  def populateData(
    id:        UUID,
    firstname: String,
    lastname:  String,
    email:     String,
    password:  String,
  ): Update0 = sql"""
    INSERT INTO users (id, firstname, lastname, email, hash, role)
    VALUES ($id, $firstname, $lastname, $email, $password, ${Role.Admin})
  """.update

  def insert(user: User): Update0 = sql"""
    INSERT INTO users (id, firstname, lastname, email, hash, role)
    VALUES (${user.id}, ${user.firstName}, ${user.lastName}, ${user.email}, ${user.hash}, ${user.role})
  """.update

  def update(user: User, id: UUID): Update0 = sql"""
    UPDATE users
    SET firstname = ${user.firstName}, lastname = ${user.lastName},
        email = ${user.email}, hash = ${user.hash}, role = ${user.role}
    WHERE id = $id
  """.update

  def select(userId: UUID): Query0[User] = sql"""
    SELECT id, firstname, lastname, email, hash, role
    FROM users
    WHERE id = $userId
  """.query

  def byEmail(email: String): Query0[User] = sql"""
    SELECT id, firstname, lastname, email, hash, role
    FROM users
    WHERE email = $email
  """.query[User]

  def delete(userId: UUID): Update0 = sql"""
    DELETE FROM users WHERE id = $userId
  """.update

  val selectAll: Query0[User] = sql"""
    SELECT id, firstname, lastname, email, hash, role
    FROM users
  """.query
}
