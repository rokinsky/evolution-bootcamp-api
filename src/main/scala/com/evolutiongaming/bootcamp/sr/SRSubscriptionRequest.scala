package com.evolutiongaming.bootcamp.sr

import io.circe.generic.JsonCodec

@JsonCodec
final case class SRSubscriptionRequest(
  callbackUrl:            String,
  events:                 List[SREvent],
  alertingEmailAddress:   Option[String]                   = None,
  callbackAuthentication: Option[SRCallbackAuthentication] = None,
)
