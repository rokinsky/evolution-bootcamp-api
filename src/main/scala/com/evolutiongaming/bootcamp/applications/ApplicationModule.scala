package com.evolutiongaming.bootcamp.applications

import cats.effect.{Clock, Sync}
import com.evolutiongaming.bootcamp.courses.CourseService
import com.evolutiongaming.bootcamp.shared.HttpCommon.AuthHandler
import com.evolutiongaming.bootcamp.sr.SRHttpClient
import org.http4s.HttpRoutes
import tsec.jwt.algorithms.JWTMacAlgo

trait ApplicationModule[F[_]] {
  def applicationHttpEndpoint: HttpRoutes[F]
}

object ApplicationModule {
  def of[F[_]: Sync: Clock, Auth: JWTMacAlgo](
    applicationService: ApplicationService[F],
    courseService:      CourseService[F],
    auth:               AuthHandler[F, Auth],
    srClient:           SRHttpClient[F],
  ): ApplicationModule[F] = new ApplicationModule[F] {
    override def applicationHttpEndpoint: HttpRoutes[F] = {
      ApplicationHttpEndpoint.endpoints(applicationService, auth, srClient, courseService)
    }
  }
}
