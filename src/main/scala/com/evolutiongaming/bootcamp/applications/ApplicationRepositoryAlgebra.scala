package com.evolutiongaming.bootcamp.applications

import com.evolutiongaming.bootcamp.sr.SRApplicationStatus

import java.util.UUID

trait ApplicationRepositoryAlgebra[F[_]] {
  def create(application: Application): F[Application]

  def get(applicationId: UUID): F[Option[Application]]

  def getUserApplication(applicationId: UUID, userId: UUID): F[Option[Application]]

  def getBySR(srId: UUID): F[Option[Application]]

  def update(application: Application): F[Application]

  def updateStatusBySR(srId: UUID, status: SRApplicationStatus): F[Application]

  def updateSolution(id: UUID, userId: UUID, solutionMessage: Option[String]): F[Application]

  def delete(applicationId: UUID): F[Unit]

  def list(pageSize: Int, offset: Int): F[List[Application]]

  def listByUserId(userId: UUID, pageSize: Int, offset: Int): F[List[Application]]
}
