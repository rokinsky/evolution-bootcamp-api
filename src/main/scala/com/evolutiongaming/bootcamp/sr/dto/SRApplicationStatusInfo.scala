package com.evolutiongaming.bootcamp.sr.dto

import com.evolutiongaming.bootcamp.sr.SRApplicationStatus
import io.circe.generic.JsonCodec

@JsonCodec
final case class SRApplicationStatusInfo(status: SRApplicationStatus)
