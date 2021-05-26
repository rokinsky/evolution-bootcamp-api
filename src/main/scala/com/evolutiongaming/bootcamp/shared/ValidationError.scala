package com.evolutiongaming.bootcamp.shared

import com.evolutiongaming.bootcamp.courses.Course

sealed abstract class ValidationError extends Throwable
object ValidationError {
  case object CourseNotFoundError extends ValidationError
  case object ApplicationNotFoundError extends ValidationError
  final case class CourseAlreadyExistsError(course: Course) extends ValidationError
}
