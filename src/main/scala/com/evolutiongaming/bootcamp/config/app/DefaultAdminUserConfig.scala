package com.evolutiongaming.bootcamp.config.app

import io.circe.generic.JsonCodec

@JsonCodec
final case class DefaultAdminUserConfig(
  firstName: String,
  lastName:  String,
  email:     String,
  password:  String,
)
