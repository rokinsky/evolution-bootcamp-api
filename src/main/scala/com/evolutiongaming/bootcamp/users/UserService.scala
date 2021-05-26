package com.evolutiongaming.bootcamp.users

import cats.MonadError
import cats.implicits._
import com.evolutiongaming.bootcamp.users.UserError.UserNotFound

import java.util.UUID

final class UserService[F[_]: MonadError[*[_], Throwable]](
  userRepo: UserRepositoryAlgebra[F],
) {
  def createUser(user: User): F[User] =
    userRepo.create(user)

  def getUser(userId: UUID): F[User] =
    userRepo.get(userId).cataF(UserNotFound.raiseError[F, User], _.pure[F])

  def getUserByEmail(email: String): F[User] =
    userRepo.findByEmail(email).cataF(UserNotFound.raiseError[F, User], _.pure[F])

  def deleteUser(userId: UUID): F[Unit] =
    userRepo.delete(userId)

  def update(user: User): F[User] =
    userRepo.update(user)

  def list(pageSize: Int, offset: Int): F[List[User]] =
    userRepo.list(pageSize, offset)
}

object UserService {
  def apply[F[_]: MonadError[*[_], Throwable]](
    repository: UserRepositoryAlgebra[F],
  ): UserService[F] =
    new UserService[F](repository)
}
