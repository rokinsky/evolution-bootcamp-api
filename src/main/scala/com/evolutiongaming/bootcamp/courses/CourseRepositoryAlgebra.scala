package com.evolutiongaming.bootcamp.courses

import cats.data.NonEmptyList

import java.util.UUID

trait CourseRepositoryAlgebra[F[_]] {
  def create(course: Course): F[Course]

  def update(course: Course): F[Course]

  def get(id: UUID): F[Option[Course]]

  def delete(id: UUID): F[Option[Course]]

  def list(pageSize: Int, offset: Int): F[List[Course]]

  def findByStatus(status: NonEmptyList[CourseStatus]): F[List[Course]]
}
