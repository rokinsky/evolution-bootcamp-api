package com.evolutiongaming.bootcamp.users

import cats.data.EitherT
import cats.effect.Sync
import cats.syntax.all._
import com.evolutiongaming.bootcamp.auth.{Auth, LoginDto, SignupDto}
import com.evolutiongaming.bootcamp.effects.GenUUID
import com.evolutiongaming.bootcamp.shared.HttpCommon._
import com.evolutiongaming.bootcamp.shared.ValidationError.UserAuthenticationFailedError
import com.evolutiongaming.bootcamp.users.dto.UpdateUserDto
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import tsec.authentication._
import tsec.common.Verified
import tsec.jwt.algorithms.JWTMacAlgo
import tsec.passwordhashers.{PasswordHash, PasswordHasher}

final class UserHttpEndpoint[F[_]: Sync, A, Auth: JWTMacAlgo](
  userService:  UserService[F],
  cryptService: PasswordHasher[F, A],
  auth:         AuthHandler[F, Auth]
) extends Http4sDsl[F] {
  private def loginEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] { case req @ POST -> Root / "login" =>
      val action = for {
        login       <- EitherT.liftF(req.as[LoginDto])
        email        = login.email
        user        <- userService.getUserByEmail(email).leftMap(_ => UserAuthenticationFailedError(email))
        checkResult <- EitherT.liftF(cryptService.checkpw(login.password, PasswordHash[A](user.hash)))
        _           <- EitherT.cond[F](checkResult == Verified, (), UserAuthenticationFailedError(email))
        token       <- EitherT.right[UserAuthenticationFailedError](auth.authenticator.create(user.id))
      } yield (user, token)

      action.foldF(
        e => BadRequest(s"Authentication failed for user with email ${e.email}"),
        userWithToken => Ok(userWithToken._1.asJson).map(auth.authenticator.embed(_, userWithToken._2))
      )
    }

  private def signupEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] { case req @ POST -> Root =>
      val action = for {
        signup <- req.as[SignupDto]
        hash   <- cryptService.hashpw(signup.password)
        id     <- GenUUID[F].random
        user   <- signup.asUser(id, hash).pure[F]
        result <- userService.createUser(user).value
      } yield result

      EitherT(action).foldF(
        e => Conflict(s"The user with email ${e.user.email} already exists"),
        user => Ok(user)
      )
    }

  private def updateEndpoint(): AuthEndpoint[F, Auth] = { case req @ PUT -> Root / UUIDVar(id) asAuthed _ =>
    val action = for {
      updateUserDto <- req.request.as[UpdateUserDto]
      updated       <- User.of(id, updateUserDto).pure[F]
      result        <- userService.update(updated).value
    } yield result

    EitherT(action).foldF(_ => NotFound("User not found"), saved => Ok(saved))
  }

  private def listEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root :? OptionalPageSizeMatcher(pageSize) :? OptionalOffsetMatcher(offset) asAuthed _ =>
      for {
        users <- userService.list(pageSize.getOrElse(10), offset.getOrElse(0))
        res   <- Ok(users)
      } yield res
  }

  private def getUserEndpoint: AuthEndpoint[F, Auth] = { case GET -> Root / UUIDVar(id) asAuthed _ =>
    userService
      .getUser(id)
      .foldF(_ => NotFound("The user was not found"), user => Ok(user))
  }

  private def deleteUserEndpoint(): AuthEndpoint[F, Auth] = { case DELETE -> Root / UUIDVar(id) asAuthed _ =>
    for {
      _   <- userService.deleteUser(id)
      res <- Accepted()
    } yield res
  }

  def endpoints: HttpRoutes[F] = {
    val authEndpoints: AuthService[F, Auth] =
      Auth.adminOnly {
        updateEndpoint()
          .orElse(listEndpoint)
          .orElse(getUserEndpoint)
          .orElse(deleteUserEndpoint())
      }

    val unAuthEndpoints = loginEndpoint <+> signupEndpoint

    unAuthEndpoints <+> auth.liftService(authEndpoints)
  }
}

object UserHttpEndpoint {
  def endpoints[F[_]: Sync, A, Auth: JWTMacAlgo](
    userService:  UserService[F],
    cryptService: PasswordHasher[F, A],
    auth:         AuthHandler[F, Auth],
  ): HttpRoutes[F] =
    new UserHttpEndpoint[F, A, Auth](userService, cryptService, auth).endpoints
}
