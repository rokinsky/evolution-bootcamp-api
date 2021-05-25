package com.evolutiongaming.bootcamp.courses

import cats.effect.Sync
import com.evolutiongaming.bootcamp.applications.ApplicationService
import com.evolutiongaming.bootcamp.shared.HttpCommon.AuthHandler
import com.evolutiongaming.bootcamp.sr.SRHttpClient
import doobie.Transactor
import org.http4s.HttpRoutes
import tsec.jwt.algorithms.JWTMacAlgo

trait CourseModule[F[_]] {
  def courseHttpEndpoint: HttpRoutes[F]
}

object CourseModule {
  def of[F[_]: Sync, Auth: JWTMacAlgo](
    xa:                 Transactor[F],
    auth:               AuthHandler[F, Auth],
    srClient:           SRHttpClient[F],
    applicationService: ApplicationService[F]
  ): CourseModule[F] = new CourseModule[F] {
    override def courseHttpEndpoint: HttpRoutes[F] = {
      val courseRepo = CourseDoobieRepository(xa)
      CourseEndpoints.endpoints(
        CourseService(courseRepo, CourseValidationInterpreter(courseRepo)),
        auth,
        srClient,
        applicationService
      )
    }
  }
}
