package com.evolutiongaming.bootcamp.courses

import cats.effect.{Clock, Sync}
import com.evolutiongaming.bootcamp.applications.ApplicationService
import com.evolutiongaming.bootcamp.shared.HttpCommon.AuthHandler
import com.evolutiongaming.bootcamp.sr.SRHttpClient
import org.http4s.HttpRoutes
import tsec.jwt.algorithms.JWTMacAlgo

trait CourseModule[F[_]] {
  def courseHttpEndpoint: HttpRoutes[F]
}

object CourseModule {
  def of[F[_]: Sync: Clock, Auth: JWTMacAlgo](
    courseService:      CourseService[F],
    applicationService: ApplicationService[F],
    auth:               AuthHandler[F, Auth],
    srClient:           SRHttpClient[F],
  ): CourseModule[F] = new CourseModule[F] {
    override def courseHttpEndpoint: HttpRoutes[F] = {
      CourseHttpEndpoint.endpoints(
        courseService,
        auth,
        srClient,
        applicationService
      )
    }
  }
}
