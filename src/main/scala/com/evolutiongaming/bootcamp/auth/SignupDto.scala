package com.evolutiongaming.bootcamp.auth

import com.evolutiongaming.bootcamp.users.{Role, User}
import io.circe.generic.JsonCodec
import tsec.passwordhashers.PasswordHash

import java.util.UUID

@JsonCodec
final case class SignupDto(
  firstName: String,
  lastName:  String,
  email:     String,
  password:  String,
  role:      Role,
) {
  def asUser[A](id: UUID, hashedPassword: PasswordHash[A]): User = User(
    id,
    firstName,
    lastName,
    email,
    hashedPassword,
    role,
  )
}
