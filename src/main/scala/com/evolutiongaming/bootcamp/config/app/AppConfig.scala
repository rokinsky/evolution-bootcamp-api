package com.evolutiongaming.bootcamp.config.app

import io.circe.generic.JsonCodec

@JsonCodec
final case class AppConfig(
  secretKey:        String,
  publicUri:        String,
  smartRecruiters:  SRConfig,
  defaultAdminUser: DefaultAdminUserConfig
)
