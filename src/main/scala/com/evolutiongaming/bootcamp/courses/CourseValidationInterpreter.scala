package com.evolutiongaming.bootcamp.courses

import cats.Applicative
import cats.data.EitherT
import cats.syntax.all._
import com.evolutiongaming.bootcamp.shared.ValidationError.{CourseAlreadyExistsError, CourseNotFoundError}

import java.util.UUID

final class CourseValidationInterpreter[F[_]: Applicative](repository: CourseRepositoryAlgebra[F])
  extends CourseValidationAlgebra[F] {
  def doesNotExist(course: Course): EitherT[F, CourseAlreadyExistsError, Unit] =
    exists(course.id).swap.leftMap(_ => CourseAlreadyExistsError(course)).void

  def exists(courseId: UUID): EitherT[F, CourseNotFoundError.type, Unit] =
    EitherT.fromOptionF(repository.get(courseId), CourseNotFoundError).void
}

object CourseValidationInterpreter {
  def apply[F[_]: Applicative](repository: CourseRepositoryAlgebra[F]) =
    new CourseValidationInterpreter[F](repository)
}
