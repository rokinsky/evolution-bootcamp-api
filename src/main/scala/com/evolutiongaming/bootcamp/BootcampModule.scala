package com.evolutiongaming.bootcamp

import cats.effect._
import cats.implicits._
import com.evolutiongaming.bootcamp.applications.{
  ApplicationDoobieRepository,
  ApplicationModule,
  ApplicationQuery,
  ApplicationService
}
import com.evolutiongaming.bootcamp.auth.{AuthModule, AuthQuery}
import com.evolutiongaming.bootcamp.config.app.AppConfig
import com.evolutiongaming.bootcamp.courses.{
  CourseDoobieRepository,
  CourseModule,
  CourseQuery,
  CourseService,
  CourseValidationInterpreter
}
import com.evolutiongaming.bootcamp.effects.GenUUID
import com.evolutiongaming.bootcamp.sr.SRHttpClient
import com.evolutiongaming.bootcamp.users.{Role, User, UserModule, UserQuery}
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.http4s.HttpRoutes
import org.http4s.client.Client
import tsec.mac.jca.{HMACSHA256, MacSigningKey}
import tsec.passwordhashers.jca.BCrypt

trait BootcampModule[F[_]] extends UserModule[F] with CourseModule[F] with ApplicationModule[F]

object BootcampModule {
  final private class BootcampModuleImpl[F[_]: Sync: Clock](
    xa:       Transactor[F],
    srClient: SRHttpClient[F],
    key:      MacSigningKey[HMACSHA256]
  ) extends BootcampModule[F] {
    private val authModule         = AuthModule.of(xa, key)
    private val applicationService = ApplicationService(ApplicationDoobieRepository(xa))
    private val courseRepo         = CourseDoobieRepository(xa)
    private val courseService      = CourseService(courseRepo, CourseValidationInterpreter(courseRepo))

    override def userHttpEndpoint: HttpRoutes[F] =
      UserModule.of(xa, authModule.routeAuth).userHttpEndpoint

    override def courseHttpEndpoint: HttpRoutes[F] =
      CourseModule.of(xa, authModule.routeAuth, srClient, applicationService).courseHttpEndpoint

    override def applicationHttpEndpoint: HttpRoutes[F] =
      ApplicationModule.of(xa, authModule.routeAuth, srClient, courseService).applicationHttpEndpoint
  }

  private def bootstrap[F[_]: Sync](
    xa:       Transactor[F],
    conf:     AppConfig,
    srClient: SRHttpClient[F]
  ): F[Unit] = for {
    // TODO: test real integration
    //    subscription <- srClient.subscribeApplicationStatusWebhook(s"${conf.publicUri}/applications/hook")
    //    _            <- srClient.activateSubscription(subscription.id)
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
    _ <- (
      UserQuery.createTable.run >>
        UserQuery.insert(user).run >>
        AuthQuery.createTable.run >>
        CourseQuery.createTable.run >>
        ApplicationQuery.createTable.run
    ).transact(xa)
  } yield ()

  def of[F[_]: Sync: Clock](conf: AppConfig, xa: Transactor[F], httpClient: Client[F]): F[BootcampModule[F]] = for {
    srClient <- SRHttpClient.of(conf.smartRecruiters, httpClient)
    _        <- bootstrap(xa, conf, srClient)
    key      <- HMACSHA256.generateKey[F]
    module   <- new BootcampModuleImpl(xa, srClient, key).pure[F]
  } yield module
}
