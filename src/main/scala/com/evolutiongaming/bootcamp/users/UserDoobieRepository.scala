package com.evolutiongaming.bootcamp.users

import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all._
import com.evolutiongaming.bootcamp.shared.SqlCommon.paginate
import com.evolutiongaming.bootcamp.users.UserError._
import doobie._
import doobie.implicits._
import doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION
import tsec.authentication.IdentityStore

import java.util.UUID

final class UserDoobieRepository[F[_]: Sync](xa: Transactor[F])
  extends UserRepositoryAlgebra[F]
    with IdentityStore[F, UUID, User] { self =>
  import UserQuery._

  def create(user: User): F[User] =
    insert(user).run
      .exceptSomeSqlState { case UNIQUE_VIOLATION => UserAlreadyExists(user).raiseError[ConnectionIO, Int] }
      .as(user)
      .transact(xa)

  def update(user: User): F[User] =
    UserQuery
      .update(user, user.id)
      .run
      .exceptSomeSqlState { case UNIQUE_VIOLATION => UserAlreadyExists(user).raiseError[ConnectionIO, Int] }
      .ensure(UserNotFound)(_ == 1)
      .transact(xa)
      .as(user)

  def get(userId: UUID): OptionT[F, User] = OptionT(select(userId).option.transact(xa))

  def findByEmail(email: String): OptionT[F, User] =
    OptionT(byEmail(email).option.transact(xa))

  def delete(userId: UUID): F[Unit] =
    UserQuery.delete(userId).run.ensure(UserNotFound)(_ == 1).transact(xa).void

  def list(pageSize: Int, offset: Int): F[List[User]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)
}

object UserDoobieRepository {
  def apply[F[_]: Sync](xa: Transactor[F]): UserDoobieRepository[F] =
    new UserDoobieRepository(xa)
}
