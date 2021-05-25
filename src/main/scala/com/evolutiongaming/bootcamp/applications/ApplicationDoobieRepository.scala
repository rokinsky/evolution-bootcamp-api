package com.evolutiongaming.bootcamp.applications

import cats.data.OptionT
import cats.effect.Bracket
import cats.syntax.all._
import com.evolutiongaming.bootcamp.sr.SRApplicationStatus
import doobie._
import doobie.implicits._

import java.util.UUID

final class ApplicationDoobieRepository[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends ApplicationRepositoryAlgebra[F] {
  import ApplicationQuery._

  override def create(application: Application): F[Application] =
    insert(application).run.as(application).transact(xa)

  override def get(applicationId: UUID): F[Option[Application]] =
    ApplicationQuery.select(applicationId).option.transact(xa)

  override def getBySR(srId: UUID): F[Option[Application]] =
    ApplicationQuery.selectBySR(srId).option.transact(xa)

  override def delete(applicationId: UUID): F[Option[Application]] =
    OptionT(get(applicationId))
      .semiflatMap(application => ApplicationQuery.delete(applicationId).run.transact(xa).as(application))
      .value

  override def updateStatusBySR(srId: UUID, status: SRApplicationStatus): F[Option[Application]] =
    (for {
      _           <- ApplicationQuery.updateStatusBySr(srId, status).run
      application <- ApplicationQuery.selectBySR(srId).option
    } yield application).transact(xa)

  override def updateSolution(id: UUID, userId: UUID, solutionMessage: Option[String]): F[Option[Application]] =
    (for {
      application <- ApplicationQuery.selectByIdAndUser(id, userId).option.fmap {
        case Some(value) => Option.when(value.solutionMessage.isEmpty)(value.copy(solutionMessage = solutionMessage))
        case _           => None
      }
      _ <- application.traverseTap(_ => ApplicationQuery.updateSolution(id, solutionMessage).run)
    } yield application).transact(xa)
}

object ApplicationDoobieRepository {
  def apply[F[_]: Bracket[*[_], Throwable]](
    xa: Transactor[F],
  ): ApplicationDoobieRepository[F] =
    new ApplicationDoobieRepository(xa)
}
