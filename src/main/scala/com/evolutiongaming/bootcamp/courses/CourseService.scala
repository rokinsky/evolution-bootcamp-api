package com.evolutiongaming.bootcamp.courses

import cats.MonadThrow
import cats.data._
import cats.effect.Clock
import cats.syntax.all._
import com.evolutiongaming.bootcamp.courses.CourseError.{CourseNotFound, CourseRegistrationClosed}

import java.util.UUID

final class CourseService[F[_]: MonadThrow: Clock](
  repository: CourseRepositoryAlgebra[F],
) {
  def create(course: Course): F[Course] =
    repository.create(course)

  def update(course: Course): F[Course] =
    repository.update(course)

  def get(id: UUID): F[Course] =
    OptionT(repository.get(id)).cataF(CourseNotFound(id).raiseError[F, Course], _.pure[F])

  def getEnrolling(id: UUID): F[Course] = for {
    time <- Clock[F].instantNow
    course <- get(id)
      .ensure(CourseRegistrationClosed)(course =>
        course.status == CourseStatus.REGISTRATION && time.isBefore(course.submissionDeadline)
      )
  } yield course

  def getBySR(srId: UUID): F[Course] =
    OptionT(repository.getBySR(srId)).cataF(CourseNotFound(srId).raiseError[F, Course], _.pure[F])

  def delete(id: UUID): F[Unit] =
    repository.delete(id)

  def list(pageSize: Int, offset: Int): F[List[Course]] =
    repository.list(pageSize, offset)

  def findByStatus(statuses: NonEmptyList[CourseStatus]): F[List[Course]] =
    repository.findByStatus(statuses)
}

object CourseService {
  def apply[F[_]: MonadThrow: Clock](
    repository: CourseRepositoryAlgebra[F],
  ): CourseService[F] =
    new CourseService[F](repository)
}
