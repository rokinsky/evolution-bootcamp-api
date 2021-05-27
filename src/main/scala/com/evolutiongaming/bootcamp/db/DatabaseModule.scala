package com.evolutiongaming.bootcamp.db

import cats.effect.{Async, Blocker, ContextShift, Resource, Sync}
import com.evolutiongaming.bootcamp.config.DatabaseConfig
import doobie.{ExecutionContexts, Transactor}
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import cats.syntax.all._

object DatabaseModule {
  def transactor[F[_]: Async: ContextShift](config: DatabaseConfig): Resource[F, Transactor[F]] =
    for {
      ec <- ExecutionContexts.fixedThreadPool[F](config.connections.poolSize)
      be <- Blocker[F]
      xa <- HikariTransactor.newHikariTransactor[F](config.driver, config.url, config.user, config.password, ec, be)
    } yield xa

  def init[F[_]: Sync](config: DatabaseConfig): F[Unit] =
    Sync[F]
      .delay(
        Flyway
          .configure()
          .dataSource(config.url, config.user, config.password)
          .load()
          .migrate()
      )
      .whenA(config.migration)
}
