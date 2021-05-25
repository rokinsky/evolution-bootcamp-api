package com.evolutiongaming.bootcamp.users

import cats.Monad
import cats.data._
import cats.syntax.functor._
import com.evolutiongaming.bootcamp.shared.ValidationError.{UserAlreadyExistsError, UserNotFoundError}

import java.util.UUID

final class UserService[F[_]: Monad](userRepo: UserRepositoryAlgebra[F], validation: UserValidationAlgebra[F]) {
  def createUser(user: User): EitherT[F, UserAlreadyExistsError, User] =
    for {
      _     <- validation.doesNotExist(user)
      saved <- EitherT.liftF(userRepo.create(user))
    } yield saved

  def getUser(userId: UUID): EitherT[F, UserNotFoundError.type, User] =
    userRepo.get(userId).toRight(UserNotFoundError)

  def getUserByEmail(
    email: String,
  ): EitherT[F, UserNotFoundError.type, User] =
    userRepo.findByEmail(email).toRight(UserNotFoundError)

  def deleteUser(userId: UUID): F[Unit] =
    userRepo.delete(userId).value.void

  def update(user: User): EitherT[F, UserNotFoundError.type, User] =
    for {
      _     <- validation.exists(user.id)
      saved <- userRepo.update(user).toRight(UserNotFoundError)
    } yield saved

  def list(pageSize: Int, offset: Int): F[List[User]] =
    userRepo.list(pageSize, offset)
}

object UserService {
  def apply[F[_]: Monad](
    repository: UserRepositoryAlgebra[F],
    validation: UserValidationAlgebra[F],
  ): UserService[F] =
    new UserService[F](repository, validation)
}
