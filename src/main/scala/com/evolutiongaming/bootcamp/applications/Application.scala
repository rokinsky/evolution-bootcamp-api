package com.evolutiongaming.bootcamp.applications

import com.evolutiongaming.bootcamp.sr.SRApplicationStatus
import io.circe.generic.JsonCodec

import java.time.Instant
import java.util.UUID

@JsonCodec
final case class Application(
  id:              UUID,
  userId:          UUID,
  courseId:        UUID,
  srId:            UUID,
  solutionMessage: Option[String]      = None,
  createdAt:       Instant,
  submittedAt:     Option[Instant]     = None,
  status:          SRApplicationStatus = SRApplicationStatus.NEW,
)
