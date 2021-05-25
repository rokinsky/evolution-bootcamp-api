package com.evolutiongaming.bootcamp.shared

import com.evolutiongaming.bootcamp.users.User
import org.http4s.{QueryParamDecoder, Response}
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import tsec.authentication.{AugmentedJWT, SecuredRequest, SecuredRequestHandler, TSecAuthService}

import java.util.UUID

object HttpCommon {
  type AuthService[F[_], Auth] = TSecAuthService[User, AugmentedJWT[Auth, UUID], F]
  type AuthEndpoint[F[_], Auth] =
    PartialFunction[SecuredRequest[F, User, AugmentedJWT[Auth, UUID]], F[Response[F]]]

  type AuthHandler[F[_], Auth] = SecuredRequestHandler[F, UUID, User, AugmentedJWT[Auth, UUID]]

  /* Necessary for decoding query parameters */
  import QueryParamDecoder._

  /* Parses out the optional offset and page size params */
  object OptionalPageSizeMatcher extends OptionalQueryParamDecoderMatcher[Int]("pageSize")
  object OptionalOffsetMatcher extends OptionalQueryParamDecoderMatcher[Int]("offset")
}
