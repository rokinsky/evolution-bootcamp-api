package com.evolutiongaming.bootcamp.courses

import cats.Monad
import cats.data._
import cats.syntax.all._
import com.evolutiongaming.bootcamp.shared.ValidationError.{CourseAlreadyExistsError, CourseNotFoundError}

import java.util.UUID

final class CourseService[F[_]: Monad](
  repository: CourseRepositoryAlgebra[F],
  validation: CourseValidationAlgebra[F],
) {
  def create(course: Course): EitherT[F, CourseAlreadyExistsError, Course] =
    for {
      _             <- validation.doesNotExist(course)
      createdCourse <- EitherT.liftF(repository.create(course))
    } yield createdCourse

  def update(course: Course): EitherT[F, CourseNotFoundError.type, Course] =
    for {
      _             <- validation.exists(course.id)
      updatedCourse <- EitherT.liftF(repository.update(course))
    } yield updatedCourse

  def get(id: UUID): EitherT[F, CourseNotFoundError.type, Course] =
    EitherT.fromOptionF(repository.get(id), CourseNotFoundError)

  def getBySR(srId: UUID): EitherT[F, CourseNotFoundError.type, Course] =
    EitherT.fromOptionF(repository.getBySR(srId), CourseNotFoundError)

  def delete(id: UUID): F[Unit] =
    repository.delete(id).as(())

  def list(pageSize: Int, offset: Int): F[List[Course]] =
    repository.list(pageSize, offset)

  def findByStatus(statuses: NonEmptyList[CourseStatus]): F[List[Course]] =
    repository.findByStatus(statuses)
}

object CourseService {
  def apply[F[_]: Monad](
    repository: CourseRepositoryAlgebra[F],
    validation: CourseValidationAlgebra[F],
  ): CourseService[F] =
    new CourseService[F](repository, validation)
}
