package com.evolutiongaming.bootcamp.auth

import io.circe.generic.JsonCodec

@JsonCodec
final case class LoginDto(
  email:    String,
  password: String,
)
