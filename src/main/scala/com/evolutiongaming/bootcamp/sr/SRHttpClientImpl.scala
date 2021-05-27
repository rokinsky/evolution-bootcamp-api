package com.evolutiongaming.bootcamp.sr

import cats.effect.Sync
import com.evolutiongaming.bootcamp.sr.dto.{
  SRApplicationStatusInfo,
  SRApplyApiResponse,
  SRMessage,
  SRMessageDetails,
  SRSubscription,
  SRSubscriptionRequest
}
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.{Header, MediaType, Method, Uri}
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.`Content-Type`

import java.util.UUID

final class SRHttpClientImpl[F[_]: Sync](client: Client[F], uri: Uri, token: String)
  extends SRHttpClient[F]
    with Http4sClientDsl[F] {
  override def getPostingConfiguration(postingId: UUID): F[String] =
    client.expect[String](
      Method
        .GET(
          uri / "postings" / postingId.toString / "configuration",
          Header("X-SmartToken", token),
        )
    )

  override def createPostingCandidate(postingId: UUID, data: String): F[SRApplyApiResponse] =
    client.expect[SRApplyApiResponse](
      Method
        .POST(
          data,
          uri / "postings" / postingId.toString / "candidates",
          Header("X-SmartToken", token),
          `Content-Type`(MediaType.application.json),
        )
    )

  override def sendCandidateEmail(candidateId: UUID, body: String): F[SRMessageDetails] =
    // TODO: should be checked if it sends emails correctly, otherwise 3rd party service should be used like Amazon SES
    client.expect[SRMessageDetails](
      Method
        .POST(
          SRMessage(s"@[CANDIDATE:$candidateId] #[CANDIDATE:$candidateId] $body"),
          uri / "messages" / "shares",
          Header("X-SmartToken", token),
          `Content-Type`(MediaType.application.json),
        )
    )

  override def subscribeApplicationStatusWebhook(callbackUrl: String): F[SRSubscription] =
    client.expect[SRSubscription](
      Method
        .POST(
          SRSubscriptionRequest(callbackUrl, List(SREvent.`application.status.updated`)),
          uri / "subscriptions",
          Header("X-SmartToken", token),
          `Content-Type`(MediaType.application.json),
        )
    )

  override def activateSubscription(subscriptionId: UUID): F[Unit] =
    client.expect[Unit](
      Method
        .PUT(
          (),
          uri / "subscriptions" / subscriptionId.toString / "activation",
          Header("X-SmartToken", token),
        )
    )

  override def getCandidateStatus(postingId: UUID, candidateId: UUID): F[SRApplicationStatusInfo] =
    client.expect[SRApplicationStatusInfo](
      Method
        .GET(
          uri / "postings" / postingId.toString / "candidates" / candidateId.toString / "status",
          Header("X-SmartToken", token),
        )
    )
}
