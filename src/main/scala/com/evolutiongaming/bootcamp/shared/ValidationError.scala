package com.evolutiongaming.bootcamp.shared

import com.evolutiongaming.bootcamp.courses.Course
import com.evolutiongaming.bootcamp.users.User

sealed abstract class ValidationError extends Throwable
object ValidationError {
  final case object AuthorNotFoundError extends ValidationError
  final case object BookNotFoundError extends ValidationError
  final case object UserNotFoundError extends ValidationError
  final case class UserAlreadyExistsError(user: User) extends ValidationError
  final case class UserAuthenticationFailedError(email: String) extends ValidationError
  case object CourseNotFoundError extends ValidationError
  case object ApplicationNotFoundError extends ValidationError
  final case class CourseAlreadyExistsError(course: Course) extends ValidationError
}
