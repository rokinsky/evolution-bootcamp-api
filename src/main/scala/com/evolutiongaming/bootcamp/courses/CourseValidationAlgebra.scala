package com.evolutiongaming.bootcamp.courses

import cats.data.EitherT
import com.evolutiongaming.bootcamp.shared.ValidationError.{CourseAlreadyExistsError, CourseNotFoundError}

import java.util.UUID

trait CourseValidationAlgebra[F[_]] {
  def doesNotExist(course: Course): EitherT[F, CourseAlreadyExistsError, Unit]

  def exists(courseId: UUID): EitherT[F, CourseNotFoundError.type, Unit]
}
