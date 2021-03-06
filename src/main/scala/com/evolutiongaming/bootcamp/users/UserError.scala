package com.evolutiongaming.bootcamp.users

sealed trait UserError extends Error

object UserError {
  final case class UserAlreadyExists(user: User) extends UserError {
    override def getMessage: String = s"The user with email ${user.email} already exists"
  }
  case object UserNotFound extends UserError {
    override def getMessage: String = s"The user was not found"
  }
}
