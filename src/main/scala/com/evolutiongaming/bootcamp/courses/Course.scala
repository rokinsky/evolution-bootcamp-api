package com.evolutiongaming.bootcamp.courses

import com.evolutiongaming.bootcamp.courses.dto.CreateCourseDto
import io.circe.generic.JsonCodec

import java.time.Instant
import java.util.UUID

@JsonCodec
final case class Course(
  id:                 UUID,
  title:              String,
  description:        String,
  taskMessage:        String,
  srId:               UUID,
  submissionDeadline: Instant,
  status:             CourseStatus = CourseStatus.REGISTRATION,
)

object Course {
  def of(id: UUID, createCourseDto: CreateCourseDto): Course =
    Course(
      id,
      createCourseDto.title,
      createCourseDto.description,
      createCourseDto.taskMessage,
      createCourseDto.srId,
      createCourseDto.submissionDeadline
    )
}
