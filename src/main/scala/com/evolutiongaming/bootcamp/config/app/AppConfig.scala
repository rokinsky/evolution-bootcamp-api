package com.evolutiongaming.bootcamp.config.app

import io.circe.generic.JsonCodec

@JsonCodec
final case class AppConfig(publicUri: String, smartRecruiters: SRConfig, defaultAdminUser: DefaultAdminUserConfig)
