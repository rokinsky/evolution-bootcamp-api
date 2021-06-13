package com.evolutiongaming.bootcamp.courses

import cats.data._
import cats.effect.BracketThrow
import cats.syntax.all._
import com.evolutiongaming.bootcamp.courses.CourseError.{CourseAlreadyExists, CourseNotFound}
import com.evolutiongaming.bootcamp.shared.SqlCommon.paginate
import doobie._
import doobie.implicits._
import doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION

import java.util.UUID

final class CourseDoobieRepository[F[_]: BracketThrow](val xa: Transactor[F]) extends CourseRepositoryAlgebra[F] {
  import CourseQuery._

  def create(course: Course): F[Course] =
    insert(course).run
      .exceptSomeSqlState { case UNIQUE_VIOLATION => CourseAlreadyExists(course).raiseError[ConnectionIO, Int] }
      .as(course)
      .transact(xa)

  def update(course: Course): F[Course] =
    CourseQuery
      .update(course, course.id)
      .run
      .exceptSomeSqlState { case UNIQUE_VIOLATION => CourseAlreadyExists(course).raiseError[ConnectionIO, Int] }
      .ensure(CourseNotFound(course.id))(_ == 1)
      .transact(xa)
      .as(course)

  def get(id: UUID): F[Option[Course]] = select(id).option.transact(xa)

  def getBySR(id: UUID): F[Option[Course]] = selectBySR(id).option.transact(xa)

  def delete(id: UUID): F[Unit] =
    CourseQuery.delete(id).run.ensure(CourseNotFound(id))(_ == 1).transact(xa).void

  def list(pageSize: Int, offset: Int): F[List[Course]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  def findByStatus(statuses: NonEmptyList[CourseStatus]): F[List[Course]] =
    selectByStatus(statuses).to[List].transact(xa)
}

object CourseDoobieRepository {
  def apply[F[_]: BracketThrow](xa: Transactor[F]): CourseDoobieRepository[F] =
    new CourseDoobieRepository(xa)
}
