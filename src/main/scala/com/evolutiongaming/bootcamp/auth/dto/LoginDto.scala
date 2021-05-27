package com.evolutiongaming.bootcamp.auth.dto

import io.circe.generic.JsonCodec

@JsonCodec
final case class LoginDto(
  email:    String,
  password: String,
)
