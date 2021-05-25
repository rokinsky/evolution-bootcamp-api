package com.evolutiongaming.bootcamp.applications

import cats.Functor
import cats.data.EitherT
import cats.syntax.all._
import com.evolutiongaming.bootcamp.shared.ValidationError.ApplicationNotFoundError
import com.evolutiongaming.bootcamp.sr.SRApplicationStatus

import java.util.UUID

final class ApplicationService[F[_]: Functor](applicationRepo: ApplicationRepositoryAlgebra[F]) {
  def placeApplication(application: Application): F[Application] =
    applicationRepo.create(application)

  def get(id: UUID): EitherT[F, ApplicationNotFoundError.type, Application] =
    EitherT.fromOptionF(applicationRepo.get(id), ApplicationNotFoundError)

  def delete(id: UUID): F[Unit] =
    applicationRepo.delete(id).as(())

  def findUserApplications(userId: UUID): F[List[Application]] = ???

  def updateApplicationStatusBySR(srId: UUID, status: SRApplicationStatus): F[Option[Application]] =
    applicationRepo.updateStatusBySR(srId, status)

  def updateApplicationSolution(id: UUID, userId: UUID, solutionMessage: Option[String]): F[Option[Application]] =
    applicationRepo.updateSolution(id, userId, solutionMessage)
}

object ApplicationService {
  def apply[F[_]: Functor](applicationRepo: ApplicationRepositoryAlgebra[F]): ApplicationService[F] =
    new ApplicationService(applicationRepo)
}
