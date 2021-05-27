package com.evolutiongaming.bootcamp.users.dto

import com.evolutiongaming.bootcamp.users.Role
import io.circe.generic.JsonCodec

@JsonCodec
final case class CreateUserDto(
  firstName: String,
  lastName:  String,
  email:     String,
  hash:      String,
  role:      Role,
)
