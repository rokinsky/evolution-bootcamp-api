package com.evolutiongaming.bootcamp.sr

import enumeratum.{CirceEnum, Enum, EnumEntry}
import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveEnumerationCodec

sealed trait SRApplicationStatus extends EnumEntry
object SRApplicationStatus extends Enum[SRApplicationStatus] with CirceEnum[SRApplicationStatus] {
  case object NEW extends SRApplicationStatus
  case object IN_REVIEW extends SRApplicationStatus
  case object INTERVIEW extends SRApplicationStatus
  case object OFFER extends SRApplicationStatus
  case object HIRE extends SRApplicationStatus
  case object REJECTED extends SRApplicationStatus
  case object WITHDRAWN extends SRApplicationStatus
  case object LEAD extends SRApplicationStatus
  case object TRANSFERRED extends SRApplicationStatus
  case object OTHER extends SRApplicationStatus

  val values: IndexedSeq[SRApplicationStatus] = findValues

  implicit def srApplicationStatusEnumCodec: Codec[SRApplicationStatus] = deriveEnumerationCodec[SRApplicationStatus]
}
