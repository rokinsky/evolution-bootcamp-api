package com.evolutiongaming.bootcamp.config.app

import io.circe.generic.JsonCodec

@JsonCodec
final case class SRConfig(apiKey: String, apiUri: String)
