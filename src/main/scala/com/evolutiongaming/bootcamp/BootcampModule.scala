package com.evolutiongaming.bootcamp

import cats.effect._
import cats.implicits._
import com.evolutiongaming.bootcamp.applications.{ApplicationDoobieRepository, ApplicationModule, ApplicationService}
import com.evolutiongaming.bootcamp.auth.AuthModule
import com.evolutiongaming.bootcamp.config.app.AppConfig
import com.evolutiongaming.bootcamp.courses._
import com.evolutiongaming.bootcamp.effects.GenUUID
import com.evolutiongaming.bootcamp.sr.SRHttpClient
import com.evolutiongaming.bootcamp.users.{Role, User, UserModule, UserQuery}
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.http4s.HttpRoutes
import org.http4s.client.Client
import tsec.mac.jca.{HMACSHA256, MacSigningKey}
import tsec.passwordhashers.jca.BCrypt

trait BootcampModule[F[_]] {
  def authHttpEndpoint:        HttpRoutes[F]
  def userHttpEndpoint:        HttpRoutes[F]
  def courseHttpEndpoint:      HttpRoutes[F]
  def applicationHttpEndpoint: HttpRoutes[F]
}

object BootcampModule {
  final private class BootcampModuleImpl[F[_]: Sync: Clock](
    xa:       Transactor[F],
    srClient: SRHttpClient[F],
    key:      MacSigningKey[HMACSHA256]
  ) extends BootcampModule[F] {
    private val authModule         = AuthModule.of(xa, key)
    private val applicationService = ApplicationService(ApplicationDoobieRepository(xa))
    private val courseService      = CourseService(CourseDoobieRepository(xa))

    override def authHttpEndpoint: HttpRoutes[F] = authModule.authHttpEndpoint

    override def userHttpEndpoint: HttpRoutes[F] =
      UserModule.of(xa, authModule.routeAuth).userHttpEndpoint

    override def courseHttpEndpoint: HttpRoutes[F] =
      CourseModule.of(courseService, applicationService, authModule.routeAuth, srClient).courseHttpEndpoint

    override def applicationHttpEndpoint: HttpRoutes[F] =
      ApplicationModule.of(applicationService, courseService, authModule.routeAuth, srClient).applicationHttpEndpoint
  }

  private def bootstrap[F[_]: Sync](
    xa:       Transactor[F],
    conf:     AppConfig,
    srClient: SRHttpClient[F]
  ): F[Unit] = for {
    subscription <- srClient.subscribeApplicationStatusWebhook(s"${conf.publicUri}/applications/hook")
    _            <- srClient.activateSubscription(subscription.id)
    // TODO: move admin creation to db-module
    adminUserId  <- GenUUID[F].random
    cryptService <- BCrypt.syncPasswordHasher[F].pure[F]
    hash         <- cryptService.hashpw(conf.defaultAdminUser.password)
    user <- User(
      adminUserId,
      conf.defaultAdminUser.firstName,
      conf.defaultAdminUser.lastName,
      conf.defaultAdminUser.email,
      hash,
      Role.Admin
    ).pure[F]
    _ <- UserQuery.insert(user).run.attemptSqlState.transact(xa)
  } yield ()

  def of[F[_]: Sync: Clock](conf: AppConfig, xa: Transactor[F], httpClient: Client[F]): F[BootcampModule[F]] = for {
    srClient <- SRHttpClient.mock(conf.smartRecruiters, httpClient) // TODO: test real implementation instead of mock
    _        <- bootstrap(xa, conf, srClient)
    key      <- HMACSHA256.buildKey[F](conf.secretKey.getBytes)
    module   <- new BootcampModuleImpl(xa, srClient, key).pure[F]
  } yield module
}
