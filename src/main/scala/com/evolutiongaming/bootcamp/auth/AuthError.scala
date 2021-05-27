package com.evolutiongaming.bootcamp.auth

sealed trait AuthError extends Error

object AuthError {
  final case class AuthenticationFailed(email: String) extends AuthError {
    override def getMessage: String = s"Authentication failed for user with email $email"
  }
}
