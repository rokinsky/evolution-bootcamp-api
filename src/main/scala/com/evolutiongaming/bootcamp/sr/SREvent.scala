package com.evolutiongaming.bootcamp.sr

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveEnumerationCodec

sealed trait SREvent
object SREvent {
  case object `job.created` extends SREvent
  case object `job.updated` extends SREvent
  case object `job.status.updated` extends SREvent
  case object `job.hiring-team.updated` extends SREvent
  case object `job.ad.created` extends SREvent
  case object `job.ad.updated` extends SREvent
  case object `job.ad.postings.updated` extends SREvent
  case object `position.created` extends SREvent
  case object `position.updated` extends SREvent
  case object `position.deleted` extends SREvent
  case object `application.created` extends SREvent
  case object `application.status.updated` extends SREvent
  case object `application.fields.updated` extends SREvent
  case object `application.onboarding-status.updated` extends SREvent
  case object `application.attachment.created` extends SREvent
  case object `application.screening-answers.updated` extends SREvent
  case object `application.source.updated` extends SREvent
  case object `candidate.created` extends SREvent
  case object `candidate.updated` extends SREvent
  case object `candidate.deleted` extends SREvent
  case object `offer.created` extends SREvent
  case object `offer.updated` extends SREvent
  case object `offer.approval.created` extends SREvent
  case object `offer.approval.approved` extends SREvent
  case object `offer.approval.rejected` extends SREvent
  case object `job.approval.created` extends SREvent
  case object `job.approval.approved` extends SREvent
  case object `job.approval.rejected` extends SREvent

  implicit def srEventEnumCodec: Codec[SREvent] = deriveEnumerationCodec[SREvent]
}
