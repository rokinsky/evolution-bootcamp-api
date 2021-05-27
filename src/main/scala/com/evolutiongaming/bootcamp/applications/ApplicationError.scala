package com.evolutiongaming.bootcamp.applications

import java.util.UUID

sealed trait ApplicationError extends Error

object ApplicationError {
  final case class ApplicationAlreadyExists(application: Application) extends ApplicationError {
    override def getMessage: String =
      s"The ${application.userId} user's application for course ${application.courseId} already exists"
  }
  final case class ApplicationSolutionAlreadyExists(id: UUID) extends ApplicationError {
    override def getMessage: String = s"The application with $id already has some solution"
  }
  final case class ApplicationNotFound(id: UUID) extends ApplicationError {
    override def getMessage: String = s"The application with id $id was not found"
  }
  final case class ApplicationNotPending(id: UUID) extends ApplicationError {
    override def getMessage: String = s"The application with id $id doesn't have a review status"
  }
}
