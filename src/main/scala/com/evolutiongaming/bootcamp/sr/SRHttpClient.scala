package com.evolutiongaming.bootcamp.sr

import cats.effect.Sync
import cats.syntax.all._
import com.evolutiongaming.bootcamp.config.app.SRConfig
import com.evolutiongaming.bootcamp.sr.dto.{
  SRApplicationStatusInfo,
  SRApplyApiResponse,
  SRMessageDetails,
  SRSubscription
}
import org.http4s._
import org.http4s.client._

import java.util.UUID

trait SRHttpClient[F[_]] {
  // https://dev.smartrecruiters.com/customer-api/live-docs/application-api/#/Application%20API/getApplyConfigurationForPosting
  // At the moment we don't need any model, so just the string data is returned
  def getPostingConfiguration(postingId: UUID): F[String]

  // https://dev.smartrecruiters.com/customer-api/live-docs/application-api/#/Application%20API/createCandidate
  // At the moment we don't need any model, so just the string data is expected
  def createPostingCandidate(postingId: UUID, data: String): F[SRApplyApiResponse]

  // https://dev.smartrecruiters.com/customer-api/live-docs/message-api/#/messages/messages.shares.create
  def sendCandidateEmail(candidateId: UUID, body: String): F[SRMessageDetails]

  // https://dev.smartrecruiters.com/customer-api/live-docs/webhooks-subscriptions-api/#/subscriptions/subscriptions.create
  def subscribeApplicationStatusWebhook(callbackUrl: String): F[SRSubscription]

  // https://dev.smartrecruiters.com/customer-api/live-docs/webhooks-subscriptions-api/#/subscriptions/subscriptions.activate
  def activateSubscription(subscriptionId: UUID): F[Unit]

  // https://dev.smartrecruiters.com/customer-api/live-docs/application-api/#/Application%20API/getApplicationStatus
  def getCandidateStatus(postingId: UUID, candidateId: UUID): F[SRApplicationStatusInfo]
}

object SRHttpClient {
  def of[F[_]: Sync](conf: SRConfig, client: Client[F]): F[SRHttpClient[F]] = for {
    uri <- Sync[F].fromEither(Uri.fromString(conf.apiUri))
  } yield new SRHttpClientImpl(client, uri, conf.apiKey)

  def mock[F[_]: Sync](conf: SRConfig, client: Client[F]): F[SRHttpClient[F]] = {
    val client: SRHttpClient[F] = new SRHttpClientMock()
    client.pure[F]
  }
}
