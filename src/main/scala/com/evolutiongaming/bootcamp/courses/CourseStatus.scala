package com.evolutiongaming.bootcamp.courses
import enumeratum._
import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveEnumerationCodec

sealed trait CourseStatus extends EnumEntry

case object CourseStatus extends Enum[CourseStatus] with CirceEnum[CourseStatus] {
  case object REGISTRATION extends CourseStatus
  case object INTERVIEW extends CourseStatus
  case object IN_PROGRESS extends CourseStatus
  case object FINISHED extends CourseStatus

  val values: IndexedSeq[CourseStatus] = findValues

  implicit def courseStatusEnumCodec: Codec[CourseStatus] = deriveEnumerationCodec[CourseStatus]
}
