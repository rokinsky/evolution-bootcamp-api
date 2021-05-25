package com.evolutiongaming.bootcamp.sr

import io.circe.generic.JsonCodec

@JsonCodec
final case class SRSubscription(id: String, callbackUrl: String, events: List[SREvent], status: String)
