package com.evolutiongaming.bootcamp.courses.dto

import com.evolutiongaming.bootcamp.courses.CourseStatus
import io.circe.generic.JsonCodec

import java.time.Instant
import java.util.UUID

@JsonCodec
final case class UpdateCourseDto(
  title:              String,
  description:        String,
  taskMessage:        String,
  srId:               UUID,
  submissionDeadline: Instant,
  status:             CourseStatus,
)
