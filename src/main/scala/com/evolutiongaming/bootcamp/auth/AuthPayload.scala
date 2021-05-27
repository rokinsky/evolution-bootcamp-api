package com.evolutiongaming.bootcamp.auth

import com.evolutiongaming.bootcamp.users.User
import tsec.authentication.AugmentedJWT
import tsec.jwt.algorithms.JWTMacAlgo

import java.util.UUID

final case class AuthPayload[Auth: JWTMacAlgo](user: User, token: AugmentedJWT[Auth, UUID])
