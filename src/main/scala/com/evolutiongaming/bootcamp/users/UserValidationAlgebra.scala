package com.evolutiongaming.bootcamp.users

import cats.data.EitherT
import com.evolutiongaming.bootcamp.shared.ValidationError.{UserAlreadyExistsError, UserNotFoundError}

import java.util.UUID

trait UserValidationAlgebra[F[_]] {
  def doesNotExist(user: User): EitherT[F, UserAlreadyExistsError, Unit]

  def exists(userId: UUID): EitherT[F, UserNotFoundError.type, Unit]
}
