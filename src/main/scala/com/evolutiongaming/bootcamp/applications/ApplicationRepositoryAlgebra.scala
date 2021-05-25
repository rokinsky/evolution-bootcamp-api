package com.evolutiongaming.bootcamp.applications

import com.evolutiongaming.bootcamp.sr.SRApplicationStatus

import java.util.UUID

trait ApplicationRepositoryAlgebra[F[_]] {
  def create(application: Application): F[Application]

  def get(applicationId: UUID): F[Option[Application]]

  def getBySR(srId: UUID): F[Option[Application]]

  def updateStatusBySR(srId: UUID, status: SRApplicationStatus): F[Option[Application]]

  def updateSolution(id: UUID, userId: UUID, solutionMessage: Option[String]): F[Option[Application]]

  def delete(applicationId: UUID): F[Option[Application]]
}
