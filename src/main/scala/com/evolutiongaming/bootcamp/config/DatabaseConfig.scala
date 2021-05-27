package com.evolutiongaming.bootcamp.config

import io.circe.generic.JsonCodec

@JsonCodec
final case class DatabaseConnectionsConfig(poolSize: Int)

@JsonCodec
final case class DatabaseConfig(
  url:         String,
  driver:      String,
  user:        String,
  password:    String,
  migration:   Boolean,
  connections: DatabaseConnectionsConfig,
)
