package com.evolutiongaming.bootcamp.applications

import com.evolutiongaming.bootcamp.sr.SRApplicationStatus
import doobie.implicits.toSqlInterpolator
import doobie.postgres.Instances
import doobie.util.meta.LegacyInstantMetaInstance
import doobie.{Meta, Query0, Update0}

import java.util.UUID

object ApplicationQuery extends LegacyInstantMetaInstance with Instances {
  implicit val statusMeta: Meta[SRApplicationStatus] =
    Meta[String].imap(SRApplicationStatus.withName)(_.entryName)

  def selectAll: Query0[Application] = sql"""
    SELECT id, user_id, course_id, sr_id, solution_message, created_at, submitted_at, status
    FROM applications
  """.query

  def selectByUserId(userId: UUID): Query0[Application] =
    (selectAll.toFragment ++ fr"WHERE user_id = $userId").query

  def select(applicationId: UUID): Query0[Application] =
    (selectAll.toFragment ++ fr"WHERE id = $applicationId").query

  def selectByIdAndUser(applicationId: UUID, userId: UUID): Query0[Application] =
    (selectAll.toFragment ++ fr"WHERE id = $applicationId AND user_id = $userId").query

  def selectBySR(srId: UUID): Query0[Application] =
    (selectAll.toFragment ++ fr"WHERE sr_id = $srId").query

  def insert(application: Application): Update0 = sql"""
    INSERT INTO applications (id, user_id, course_id, sr_id, solution_message, created_at, submitted_at, status)
    VALUES (${application.id}, ${application.userId}, ${application.courseId}, ${application.srId}, ${application.solutionMessage}, ${application.createdAt}, ${application.submittedAt}, ${application.status})
  """.update

  def update(application: Application, id: UUID): Update0 = sql"""
    UPDATE applications
    SET user_id = ${application.userId}, course_id = ${application.courseId}, sr_id = ${application.srId}, solution_message = ${application.solutionMessage}, created_at = ${application.createdAt}, submitted_at = ${application.submittedAt}, status = ${application.status}
    WHERE id = $id
  """.update

  def updateStatusBySr(srId: UUID, status: SRApplicationStatus): Update0 = sql"""
    UPDATE applications
    SET status = $status
    WHERE sr_id = $srId
  """.update

  def updateSolution(id: UUID, solutionMessage: Option[String]): Update0 = sql"""
    UPDATE applications
    SET solution_message = $solutionMessage, submitted_at = NOW()
    WHERE id = $id
  """.update

  def delete(applicationId: UUID): Update0 = sql"""
    DELETE FROM applications
    WHERE id = $applicationId
  """.update
}
