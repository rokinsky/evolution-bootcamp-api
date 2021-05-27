package com.evolutiongaming.bootcamp.users

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import com.evolutiongaming.bootcamp.users.dto.{CreateUserDto, UpdateUserDto}
import io.circe.generic.JsonCodec
import tsec.authorization.AuthorizationInfo

import java.util.UUID

@JsonCodec
final case class User(
  id:        UUID,
  firstName: String,
  lastName:  String,
  email:     String,
  hash:      String,
  role:      Role,
)

object User {
  implicit def authRole[F[_]: Applicative]: AuthorizationInfo[F, Role, User] = _.role.pure[F]

  def of(id: UUID, updateUserDto: UpdateUserDto): User = User(
    id,
    updateUserDto.firstName,
    updateUserDto.lastName,
    updateUserDto.email,
    updateUserDto.hash,
    updateUserDto.role
  )

  def of(id: UUID, createUserDto: CreateUserDto): User = User(
    id,
    createUserDto.firstName,
    createUserDto.lastName,
    createUserDto.email,
    createUserDto.hash,
    createUserDto.role
  )
}
