package com.evolutiongaming.bootcamp.courses

import cats.data._
import cats.effect.Bracket
import cats.syntax.all._
import com.evolutiongaming.bootcamp.shared.SqlCommon.paginate
import doobie._
import doobie.implicits._

import java.util.UUID

final class CourseDoobieRepository[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends CourseRepositoryAlgebra[F] {
  import CourseQuery._

  def create(course: Course): F[Course] =
    insert(course).run.as(course).transact(xa)

  def update(course: Course): F[Course] =
    CourseQuery
      .update(course, course.id)
      .run
      .as(course)
      .transact(xa)

  def get(id: UUID): F[Option[Course]] = select(id).option.transact(xa)

  def getBySR(id: UUID): F[Option[Course]] = selectBySR(id).option.transact(xa)

  def delete(id: UUID): F[Option[Course]] =
    OptionT(select(id).option).semiflatMap(course => CourseQuery.delete(id).run.as(course)).value.transact(xa)

  def list(pageSize: Int, offset: Int): F[List[Course]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  def findByStatus(statuses: NonEmptyList[CourseStatus]): F[List[Course]] =
    selectByStatus(statuses).to[List].transact(xa)
}

object CourseDoobieRepository {
  def apply[F[_]: Bracket[*[_], Throwable]](xa: Transactor[F]): CourseDoobieRepository[F] =
    new CourseDoobieRepository(xa)
}
