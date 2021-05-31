package com.evolutiongaming.bootcamp.applications

import cats.data.OptionT
import cats.effect.Bracket
import cats.syntax.all._
import com.evolutiongaming.bootcamp.applications.ApplicationError.{
  ApplicationAlreadyExists,
  ApplicationNotFound,
  ApplicationNotPending,
  ApplicationSolutionAlreadyExists
}
import com.evolutiongaming.bootcamp.shared.SqlCommon.paginate
import com.evolutiongaming.bootcamp.sr.SRApplicationStatus
import doobie._
import doobie.implicits.{toConnectionIOOps, toDoobieApplicativeErrorOps}
import doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION

import java.util.UUID

final class ApplicationDoobieRepository[F[_]: Bracket[*[_], Throwable]](xa: Transactor[F])
  extends ApplicationRepositoryAlgebra[F] {
  import ApplicationQuery._

  override def create(application: Application): F[Application] =
    ApplicationQuery
      .insert(application)
      .run
      .exceptSomeSqlState { case UNIQUE_VIOLATION =>
        ApplicationAlreadyExists(application).raiseError[ConnectionIO, Int]
      }
      .as(application)
      .transact(xa)

  override def get(applicationId: UUID): F[Option[Application]] =
    ApplicationQuery.select(applicationId).option.transact(xa)

  override def getUserApplication(applicationId: UUID, userId: UUID): F[Option[Application]] =
    ApplicationQuery.selectByIdAndUser(applicationId, userId).option.transact(xa)

  override def getBySR(srId: UUID): F[Option[Application]] =
    ApplicationQuery.selectBySR(srId).option.transact(xa)

  override def delete(applicationId: UUID): F[Unit] =
    ApplicationQuery.delete(applicationId).run.ensure(ApplicationNotFound(applicationId))(_ == 1).transact(xa).void

  override def update(application: Application): F[Application] =
    ApplicationQuery
      .update(application, application.id)
      .run
      .exceptSomeSqlState { case UNIQUE_VIOLATION =>
        ApplicationAlreadyExists(application).raiseError[ConnectionIO, Int]
      }
      .ensure(ApplicationNotFound(application.id))(_ == 1)
      .transact(xa)
      .as(application)

  override def updateStatusBySR(srId: UUID, status: SRApplicationStatus): F[Application] =
    (for {
      _ <- ApplicationQuery.updateStatusBySr(srId, status).run
      application <- OptionT(ApplicationQuery.selectBySR(srId).option)
        .cataF(ApplicationNotFound(srId).raiseError[ConnectionIO, Application], _.pure[ConnectionIO])
    } yield application).transact(xa)

  override def updateSolution(id: UUID, userId: UUID, solutionMessage: Option[String]): F[Application] =
    (for {
      application <- OptionT(ApplicationQuery.selectByIdAndUser(id, userId).option)
        .cataF(ApplicationNotFound(id).raiseError[ConnectionIO, Application], _.pure[ConnectionIO])
        .ensure(ApplicationSolutionAlreadyExists(id))(_.solutionMessage.isEmpty)
        .ensure(ApplicationNotPending(id))(_.status == SRApplicationStatus.IN_REVIEW)
      _                  <- ApplicationQuery.updateSolution(id, solutionMessage).run
      updatedApplication <- application.copy(solutionMessage = solutionMessage).pure[ConnectionIO]
    } yield updatedApplication).transact(xa)

  override def list(pageSize: Int, offset: Int): F[List[Application]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  override def listByUserId(userId: UUID, pageSize: Int, offset: Int): F[List[Application]] =
    paginate(pageSize, offset)(ApplicationQuery.selectByUserId(userId)).to[List].transact(xa)

}

object ApplicationDoobieRepository {
  def apply[F[_]: Bracket[*[_], Throwable]](
    xa: Transactor[F],
  ): ApplicationDoobieRepository[F] =
    new ApplicationDoobieRepository(xa)
}
