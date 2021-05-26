package com.evolutiongaming.bootcamp.sr.dto

import io.circe.generic.JsonCodec

@JsonCodec
final case class SRShareWith(
  description:  Option[String],
  users:        List[String],
  hiringTeamOf: List[String],
  everyone:     Option[Boolean],
  openNote:     Option[Boolean]
)
