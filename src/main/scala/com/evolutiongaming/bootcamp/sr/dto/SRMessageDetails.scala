package com.evolutiongaming.bootcamp.sr.dto

import io.circe.generic.JsonCodec

@JsonCodec
final case class SRMessageDetails(id: Option[String], shareRequired: Option[Boolean])
