package com.evolutiongaming.bootcamp.courses

import java.util.UUID

sealed trait CourseError extends Error

object CourseError {
  final case class CourseAlreadyExists(course: Course) extends CourseError {
    override def getMessage: String = s"The course with Smart Recruiters Id ${course.srId} already exists"
  }
  final case class CourseNotFound(id: UUID) extends CourseError {
    override def getMessage: String = s"The course with id $id was not found"
  }
}
