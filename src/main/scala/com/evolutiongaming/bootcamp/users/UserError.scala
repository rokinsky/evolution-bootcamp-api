package com.evolutiongaming.bootcamp.users

sealed trait UserError extends Throwable with Serializable
case class UserAlreadyExists(user: User) extends UserError
case object UserNotFound extends UserError
final case class UserAuthenticationFailed(email: String) extends UserError
