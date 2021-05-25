package com.evolutiongaming.bootcamp.sr

import cats.effect.Sync
import cats.syntax.all._
import com.evolutiongaming.bootcamp.config.app.SRConfig
import org.http4s._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.client._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.`Content-Type`

import java.util.UUID

final class SRHttpClient[F[_]: Sync](client: Client[F], uri: Uri, token: String) extends Http4sClientDsl[F] {
  // https://dev.smartrecruiters.com/customer-api/live-docs/application-api/#/Application%20API/getApplyConfigurationForPosting
  def getPostingConfiguration(postingId: UUID): F[String] =
    client.expect[String](
      Method
        .GET(
          uri / "postings" / postingId.toString / "configuration",
          Header("X-SmartToken", token),
        )
    )

  // https://dev.smartrecruiters.com/customer-api/live-docs/application-api/#/Application%20API/createCandidate
  def createPostingCandidate(postingId: UUID, data: String): F[SRApplyApiResponse] =
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
  def sendCandidateEmail(candidateId: UUID, body: String): F[SRMessageDetails] =
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
  def subscribeApplicationStatusWebhook(callbackUrl: String): F[SRSubscription] =
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
  def activateSubscription(subscriptionId: String): F[Unit] =
    client.expect[Unit](
      Method
        .PUT(
          (),
          uri / "subscriptions" / subscriptionId / "activation",
          Header("X-SmartToken", token),
        )
    )

  // https://dev.smartrecruiters.com/customer-api/live-docs/application-api/#/Application%20API/getApplicationStatus
  def getCandidateStatus(postingId: UUID, candidateId: UUID): F[SRApplicationStatusInfo] =
    client.expect[SRApplicationStatusInfo](
      Method
        .GET(
          uri / "postings" / postingId.toString / "candidates" / candidateId.toString / "status",
          Header("X-SmartToken", token),
        )
    )
}

object SRHttpClient {
  def of[F[_]: Sync](conf: SRConfig, client: Client[F]): F[SRHttpClient[F]] = for {
    uri <- Sync[F].fromEither(Uri.fromString(conf.apiUri))
  } yield new SRHttpClient(client, uri, conf.apiKey)
}
