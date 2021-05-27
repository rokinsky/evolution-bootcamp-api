package com.evolutiongaming.bootcamp.courses

import com.evolutiongaming.bootcamp.courses.dto.{CreateCourseDto, UpdateCourseDto}
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

  def of(id: UUID, updateCourseDto: UpdateCourseDto): Course =
    Course(
      id,
      updateCourseDto.title,
      updateCourseDto.description,
      updateCourseDto.taskMessage,
      updateCourseDto.srId,
      updateCourseDto.submissionDeadline,
      updateCourseDto.status
    )
}
