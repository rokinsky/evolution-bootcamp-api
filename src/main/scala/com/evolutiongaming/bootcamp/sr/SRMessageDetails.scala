package com.evolutiongaming.bootcamp.sr

import io.circe.generic.JsonCodec

@JsonCodec
final case class SRMessageDetails(id: Option[String], shareRequired: Option[Boolean])
