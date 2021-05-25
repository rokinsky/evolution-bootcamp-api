package com.evolutiongaming.bootcamp.users

import cats.Applicative
import cats.data.EitherT
import cats.syntax.all._
import com.evolutiongaming.bootcamp.shared.ValidationError.{UserAlreadyExistsError, UserNotFoundError}

import java.util.UUID

final class UserValidationInterpreter[F[_]: Applicative](userRepo: UserRepositoryAlgebra[F])
  extends UserValidationAlgebra[F] {
  def doesNotExist(user: User): EitherT[F, UserAlreadyExistsError, Unit] =
    userRepo
      .findByEmail(user.email)
      .map(UserAlreadyExistsError)
      .toLeft(())

  def exists(userId: UUID): EitherT[F, UserNotFoundError.type, Unit] =
    userRepo
      .get(userId)
      .toRight(UserNotFoundError)
      .void
}

object UserValidationInterpreter {
  def apply[F[_]: Applicative](repo: UserRepositoryAlgebra[F]): UserValidationAlgebra[F] =
    new UserValidationInterpreter[F](repo)
}
