package com.evolutiongaming.bootcamp.sr

import cats.effect.Sync
import com.evolutiongaming.bootcamp.sr.dto.{
  SRApplicationStatusInfo,
  SRApplyApiResponse,
  SRMessageDetails,
  SRSubscription
}

import java.util.UUID

final class SRHttpClientMock[F[_]: Sync] extends SRHttpClient[F] {
  override def getPostingConfiguration(postingId: UUID): F[String] = ???

  override def createPostingCandidate(postingId: UUID, data: String): F[SRApplyApiResponse] = ???

  override def sendCandidateEmail(candidateId: UUID, body: String): F[SRMessageDetails] = ???

  override def subscribeApplicationStatusWebhook(callbackUrl: String): F[SRSubscription] = ???

  override def activateSubscription(subscriptionId: String): F[Unit] = ???

  override def getCandidateStatus(postingId: UUID, candidateId: UUID): F[SRApplicationStatusInfo] = ???
}
