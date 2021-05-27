package com.evolutiongaming.bootcamp.applications.dto

import io.circe.generic.JsonCodec

import java.util.UUID

@JsonCodec
final case class CreateApplicationDto(
  userId:   UUID,
  courseId: UUID,
  srId:     UUID,
)
