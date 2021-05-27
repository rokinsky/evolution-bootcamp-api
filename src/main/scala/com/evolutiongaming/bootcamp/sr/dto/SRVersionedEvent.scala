package com.evolutiongaming.bootcamp.sr.dto

import com.evolutiongaming.bootcamp.sr.SREvent
import io.circe.generic.JsonCodec

@JsonCodec
final case class SRVersionedEvent(name: SREvent, version: String)
