package com.evolutiongaming.bootcamp.sr

import io.circe.generic.JsonCodec

import java.util.UUID

@JsonCodec
final case class SRApplyApiResponse(id: UUID, createdOn: String, candidatePortalUrl: String)
