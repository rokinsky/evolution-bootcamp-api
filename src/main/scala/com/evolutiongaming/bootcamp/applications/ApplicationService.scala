package com.evolutiongaming.bootcamp.applications

import cats.MonadError
import cats.data.OptionT
import cats.implicits._
import com.evolutiongaming.bootcamp.applications.ApplicationError.ApplicationNotFound
import com.evolutiongaming.bootcamp.sr.SRApplicationStatus

import java.util.UUID

final class ApplicationService[F[_]: MonadError[*[_], Throwable]](applicationRepo: ApplicationRepositoryAlgebra[F]) {
  def placeApplication(application: Application): F[Application] =
    applicationRepo.create(application)

  def get(id: UUID): F[Application] =
    OptionT(applicationRepo.get(id)).cataF(ApplicationNotFound(id).raiseError[F, Application], _.pure[F])

  def getUserApplication(applicationId: UUID, userId: UUID): F[Application] =
    OptionT(applicationRepo.getUserApplication(applicationId, userId))
      .cataF(ApplicationNotFound(applicationId).raiseError[F, Application], _.pure[F])

  def delete(id: UUID): F[Unit] =
    applicationRepo.delete(id)

  def update(application: Application): F[Application] =
    applicationRepo.update(application)

  def updateApplicationStatusBySR(srId: UUID, status: SRApplicationStatus): F[Application] =
    applicationRepo.updateStatusBySR(srId, status)

  def updateApplicationSolution(id: UUID, userId: UUID, solutionMessage: Option[String]): F[Application] =
    applicationRepo.updateSolution(id, userId, solutionMessage)

  def list(pageSize: Int, offset: Int): F[List[Application]] =
    applicationRepo.list(pageSize, offset)

  def listByUserId(userId: UUID, pageSize: Int, offset: Int): F[List[Application]] =
    applicationRepo.listByUserId(userId, pageSize, offset)
}

object ApplicationService {
  def apply[F[_]: MonadError[*[_], Throwable]](
    applicationRepo: ApplicationRepositoryAlgebra[F]
  ): ApplicationService[F] =
    new ApplicationService(applicationRepo)
}
