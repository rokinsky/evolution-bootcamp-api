package com.evolutiongaming.bootcamp.sr

import io.circe.generic.JsonCodec

@JsonCodec
final case class SRMessage(
  content:       String,
  correlationId: Option[String]      = None,
  shareWith:     Option[SRShareWith] = None,
)
