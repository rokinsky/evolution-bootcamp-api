package com.evolutiongaming.bootcamp

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Sync, Timer}
import com.evolutiongaming.bootcamp.config.BootcampConfig
import com.evolutiongaming.bootcamp.db.DatabaseModule
import io.circe.config.parser
import org.http4s.HttpApp
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server}

import scala.concurrent.ExecutionContext.global

object BootcampApp extends IOApp {
  private def httpApp[F[_]: Sync](ctx: BootcampModule[F]): HttpApp[F] =
    Router(
      "/auth"         -> ctx.authHttpEndpoint,
      "/users"        -> ctx.userHttpEndpoint,
      "/courses"      -> ctx.courseHttpEndpoint,
      "/applications" -> ctx.applicationHttpEndpoint
    ).orNotFound

  private def resource[F[_]: Sync: ConcurrentEffect: ContextShift: Timer]: Resource[F, Server[F]] =
    for {
      conf       <- Resource.eval(parser.decodePathF[F, BootcampConfig]("bootcamp"))
      transactor <- DatabaseModule.transactor[F](conf.db)
      _          <- Resource.eval(DatabaseModule.init(conf.db))
      client     <- BlazeClientBuilder[F](global).resource
      ctx        <- Resource.eval(BootcampModule.of(conf.app, transactor, client))
      server <- BlazeServerBuilder[F](global)
        .bindHttp(conf.server.port, conf.server.host)
        .withHttpApp(httpApp(ctx))
        .resource
    } yield server

  override def run(args: List[String]): IO[ExitCode] =
    resource[IO].use(_ => IO.never).as(ExitCode.Success)
}
