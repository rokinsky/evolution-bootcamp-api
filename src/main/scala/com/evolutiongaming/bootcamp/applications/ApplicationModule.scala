package com.evolutiongaming.bootcamp.applications

import cats.effect.Sync
import com.evolutiongaming.bootcamp.shared.HttpCommon.AuthHandler
import com.evolutiongaming.bootcamp.sr.SRHttpClient
import doobie.Transactor
import org.http4s.HttpRoutes
import tsec.jwt.algorithms.JWTMacAlgo

trait ApplicationModule[F[_]] {
  def applicationHttpEndpoint: HttpRoutes[F]
}

object ApplicationModule {
  def of[F[_]: Sync, Auth: JWTMacAlgo](
    xa:       Transactor[F],
    auth:     AuthHandler[F, Auth],
    srClient: SRHttpClient[F],
  ): ApplicationModule[F] = of[F, Auth](auth, srClient, ApplicationService(ApplicationDoobieRepository(xa)))

  def of[F[_]: Sync, Auth: JWTMacAlgo](
    auth:               AuthHandler[F, Auth],
    srClient:           SRHttpClient[F],
    applicationService: ApplicationService[F]
  ): ApplicationModule[F] = new ApplicationModule[F] {
    override def applicationHttpEndpoint: HttpRoutes[F] = {
      ApplicationEndpoints.endpoints(applicationService, auth, srClient)
    }
  }
}
