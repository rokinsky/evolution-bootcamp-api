package com.evolutiongaming.bootcamp.config

import cats.effect.{Async, Blocker, ContextShift, Resource}
import doobie.hikari.HikariTransactor
import doobie.{ExecutionContexts, Transactor}
import io.circe.generic.JsonCodec

@JsonCodec
final case class DatabaseConnectionsConfig(poolSize: Int)

@JsonCodec
final case class DatabaseConfig(
  url:         String,
  driver:      String,
  user:        String,
  password:    String,
  connections: DatabaseConnectionsConfig,
)

object DatabaseConfig {
  def transactor[F[_]: Async: ContextShift](config: DatabaseConfig): Resource[F, Transactor[F]] =
    for {
      ec <- ExecutionContexts.fixedThreadPool[F](config.connections.poolSize)
      be <- Blocker[F]
      xa <- HikariTransactor.newHikariTransactor[F](config.driver, config.url, config.user, config.password, ec, be)
    } yield xa
}
