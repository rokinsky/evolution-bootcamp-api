package com.evolutiongaming.bootcamp.sr.dto

import io.circe.generic.JsonCodec

import java.util.UUID

@JsonCodec
final case class SRSubscription(id: UUID, callbackUrl: String, events: List[SRVersionedEvent], status: String)
