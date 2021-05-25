package com.evolutiongaming.bootcamp.users

import cats.data.OptionT

import java.util.UUID

trait UserRepositoryAlgebra[F[_]] {
  def create(user: User): F[User]

  def update(user: User): OptionT[F, User]

  def get(userId: UUID): OptionT[F, User]

  def delete(userId: UUID): OptionT[F, User]

  def findByEmail(email: String): OptionT[F, User]

  def list(pageSize: Int, offset: Int): F[List[User]]
}
