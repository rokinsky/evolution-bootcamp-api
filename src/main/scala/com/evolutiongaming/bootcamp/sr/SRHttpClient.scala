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
  def getPostingConfiguration(postingId: UUID): F[String]

  def createPostingCandidate(postingId: UUID, data: String): F[SRApplyApiResponse]

  def sendCandidateEmail(candidateId: UUID, body: String): F[SRMessageDetails]

  def subscribeApplicationStatusWebhook(callbackUrl: String): F[SRSubscription]

  def activateSubscription(subscriptionId: String): F[Unit]

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
