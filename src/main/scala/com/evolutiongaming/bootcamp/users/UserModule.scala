package com.evolutiongaming.bootcamp.users

import cats.effect._
import com.evolutiongaming.bootcamp.shared.HttpCommon.AuthHandler
import doobie.util.transactor.Transactor
import org.http4s.HttpRoutes
import tsec.jwt.algorithms.JWTMacAlgo
import tsec.passwordhashers.jca.BCrypt

trait UserModule[F[_]] {
  def userHttpEndpoint: HttpRoutes[F]
}

object UserModule {
  def of[F[_]: Sync, Auth: JWTMacAlgo](
    xa:   Transactor[F],
    auth: AuthHandler[F, Auth],
  ): UserModule[F] = new UserModule[F] {
    override def userHttpEndpoint: HttpRoutes[F] = {
      val userRepo       = UserDoobieRepository(xa)
      val userValidation = UserValidationInterpreter(userRepo)
      val userService    = UserService(userRepo, userValidation)
      UserHttpEndpoint.endpoints(userService, BCrypt.syncPasswordHasher[F], auth)
    }
  }
}
