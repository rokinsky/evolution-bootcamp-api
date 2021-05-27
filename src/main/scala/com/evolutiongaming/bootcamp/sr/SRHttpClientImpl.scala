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
  // https://dev.smartrecruiters.com/customer-api/live-docs/application-api/#/Application%20API/getApplyConfigurationForPosting
  override def getPostingConfiguration(postingId: UUID): F[String] =
    client.expect[String](
      Method
        .GET(
          uri / "postings" / postingId.toString / "configuration",
          Header("X-SmartToken", token),
        )
    )

  // https://dev.smartrecruiters.com/customer-api/live-docs/application-api/#/Application%20API/createCandidate
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

  // https://dev.smartrecruiters.com/customer-api/live-docs/message-api/#/messages/messages.shares.create
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

  // https://dev.smartrecruiters.com/customer-api/live-docs/webhooks-subscriptions-api/#/subscriptions/subscriptions.create
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

  // https://dev.smartrecruiters.com/customer-api/live-docs/webhooks-subscriptions-api/#/subscriptions/subscriptions.activate
  override def activateSubscription(subscriptionId: String): F[Unit] =
    client.expect[Unit](
      Method
        .PUT(
          (),
          uri / "subscriptions" / subscriptionId / "activation",
          Header("X-SmartToken", token),
        )
    )

  // https://dev.smartrecruiters.com/customer-api/live-docs/application-api/#/Application%20API/getApplicationStatus
  override def getCandidateStatus(postingId: UUID, candidateId: UUID): F[SRApplicationStatusInfo] =
    client.expect[SRApplicationStatusInfo](
      Method
        .GET(
          uri / "postings" / postingId.toString / "candidates" / candidateId.toString / "status",
          Header("X-SmartToken", token),
        )
    )
}
