package com.evolutiongaming.bootcamp.sr.dto

import io.circe.generic.JsonCodec
import io.circe.generic.extras.JsonKey

import java.util.UUID

@JsonCodec
final case class SRApplicationWebhookPayload(
  @JsonKey("job_id") jobId:             UUID,
  @JsonKey("candidate_id") candidateId: UUID
)
