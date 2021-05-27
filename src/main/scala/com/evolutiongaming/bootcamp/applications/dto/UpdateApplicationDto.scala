package com.evolutiongaming.bootcamp.applications.dto

import com.evolutiongaming.bootcamp.sr.SRApplicationStatus
import io.circe.generic.JsonCodec

import java.time.Instant
import java.util.UUID

@JsonCodec
final case class UpdateApplicationDto(
  userId:          UUID,
  courseId:        UUID,
  srId:            UUID,
  solutionMessage: Option[String],
  createdAt:       Instant,
  submittedAt:     Option[Instant],
  status:          SRApplicationStatus,
)
