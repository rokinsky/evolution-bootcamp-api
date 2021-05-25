package com.evolutiongaming.bootcamp.users.dto

import com.evolutiongaming.bootcamp.users.Role
import io.circe.generic.JsonCodec

@JsonCodec
final case class UpdateUserDto(
  firstName: String,
  lastName:  String,
  email:     String,
  hash:      String,
  role:      Role,
)

object UpdateUserDto {}
