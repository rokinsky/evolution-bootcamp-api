package com.evolutiongaming.bootcamp.config

import com.evolutiongaming.bootcamp.config.app.AppConfig
import io.circe.generic.JsonCodec

@JsonCodec
final case class BootcampConfig(app: AppConfig, db: DatabaseConfig, server: ServerConfig)
