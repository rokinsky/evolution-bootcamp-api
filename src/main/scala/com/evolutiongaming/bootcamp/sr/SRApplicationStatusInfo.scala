package com.evolutiongaming.bootcamp.sr

import io.circe.generic.JsonCodec

@JsonCodec
final case class SRApplicationStatusInfo(status: SRApplicationStatus)
