package com.evolutiongaming.bootcamp.sr

import cats.effect.Sync
import com.evolutiongaming.bootcamp.effects.GenUUID
import com.evolutiongaming.bootcamp.sr.dto.{
  SRApplicationStatusInfo,
  SRApplyApiResponse,
  SRMessageDetails,
  SRSubscription,
  SRVersionedEvent
}
import cats.syntax.all._
import io.circe.Json
import io.circe.literal.JsonStringContext

import java.util.UUID

final class SRHttpClientMock[F[_]: Sync] extends SRHttpClient[F] {
  override def getPostingConfiguration(postingId: UUID): F[Json] =
    json"""{"questions":[{"id":"string","label":"string","repeatable":true,"fields":[{"id":"string","label":"string","type":"INPUT_TEXT","required":true,"complianceType":"DIVERSITY","values":[{"id":"string","label":"string"}]}]}],"settings":{"avatarUploadAvailable":true},"privacyPolicies":[{"url":"string","orgName":"string"}]}"""
      .pure[F]

  override def createPostingCandidate(postingId: UUID, data: Json): F[SRApplyApiResponse] = for {
    id  <- GenUUID[F].random
    res <- SRApplyApiResponse(id, "2021-04-30T15:54:36.240320Z", "https://google.com").pure[F]
  } yield res

  override def sendCandidateEmail(candidateId: UUID, body: String): F[SRMessageDetails] =
    SRMessageDetails(None, None).pure[F]

  override def subscribeApplicationStatusWebhook(callbackUrl: String): F[SRSubscription] = for {
    id <- GenUUID[F].random
    subscription <- SRSubscription(
      id,
      callbackUrl,
      List(SRVersionedEvent(SREvent.`application.status.updated`, "1")),
      "inactive"
    ).pure[F]
  } yield subscription

  override def activateSubscription(subscriptionId: UUID): F[Unit] = ().pure[F]

  override def getCandidateStatus(postingId: UUID, candidateId: UUID): F[SRApplicationStatusInfo] =
    SRApplicationStatusInfo(SRApplicationStatus.IN_REVIEW).pure[F]
}
