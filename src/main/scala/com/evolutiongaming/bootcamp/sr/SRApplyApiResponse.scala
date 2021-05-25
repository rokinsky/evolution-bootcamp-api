package com.evolutiongaming.bootcamp.sr

import io.circe.generic.JsonCodec

@JsonCodec
final case class SRApplyApiResponse(id: String, createdOn: String, candidatePortalUrl: String)
