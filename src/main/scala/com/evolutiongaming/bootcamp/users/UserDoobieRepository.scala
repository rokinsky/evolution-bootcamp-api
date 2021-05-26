package com.evolutiongaming.bootcamp.users

import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all._
import com.evolutiongaming.bootcamp.shared.SqlCommon.paginate
import doobie._
import doobie.implicits._
import tsec.authentication.IdentityStore

import java.util.UUID

final class UserDoobieRepository[F[_]: Sync](xa: Transactor[F])
  extends UserRepositoryAlgebra[F]
    with IdentityStore[F, UUID, User] { self =>
  import UserQuery._

  def create(user: User): F[User] =
    insert(user).run.as(user).transact(xa)

  def update(user: User): F[User] =
    UserQuery.update(user, user.id).run.transact(xa).as(user)

  def get(userId: UUID): OptionT[F, User] = OptionT(select(userId).option.transact(xa))

  def findByEmail(email: String): OptionT[F, User] =
    OptionT(byEmail(email).option.transact(xa))

  def delete(userId: UUID): F[Unit] =
    UserQuery.delete(userId).run.transact(xa).void

  def list(pageSize: Int, offset: Int): F[List[User]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)
}

object UserDoobieRepository {
  def apply[F[_]: Sync](xa: Transactor[F]): UserDoobieRepository[F] =
    new UserDoobieRepository(xa)
}
