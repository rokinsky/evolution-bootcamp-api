package com.evolutiongaming.bootcamp.courses.dto

import io.circe.generic.JsonCodec

import java.time.Instant
import java.util.UUID

@JsonCodec
final case class CreateCourseDto(
  title:              String,
  description:        String,
  taskMessage:        String,
  srId:               UUID,
  submissionDeadline: Instant
)
