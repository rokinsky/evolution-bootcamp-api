package com.evolutiongaming.bootcamp.applications

import com.evolutiongaming.bootcamp.applications.dto.{CreateApplicationDto, UpdateApplicationDto}
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

object Application {
  def of(id: UUID, createdAt: Instant, createApplicationDto: CreateApplicationDto): Application = Application(
    id        = id,
    userId    = createApplicationDto.userId,
    courseId  = createApplicationDto.courseId,
    srId      = createApplicationDto.srId,
    createdAt = createdAt
  )

  def of(id: UUID, updateApplicationDto: UpdateApplicationDto): Application = Application(
    id              = id,
    userId          = updateApplicationDto.userId,
    courseId        = updateApplicationDto.courseId,
    srId            = updateApplicationDto.srId,
    solutionMessage = updateApplicationDto.solutionMessage,
    createdAt       = updateApplicationDto.createdAt,
    submittedAt     = updateApplicationDto.submittedAt,
    status          = updateApplicationDto.status,
  )
}
