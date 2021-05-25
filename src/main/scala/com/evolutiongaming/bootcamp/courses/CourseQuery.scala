package com.evolutiongaming.bootcamp.courses

import cats.data.NonEmptyList
import doobie.implicits.toSqlInterpolator
import doobie._
import doobie.util.meta.{MetaConstructors, TimeMetaInstances}

import java.util.UUID

object CourseQuery extends TimeMetaInstances with MetaConstructors {
  implicit val statusMeta: Meta[CourseStatus] =
    Meta[String].imap(CourseStatus.withName)(_.entryName)

  implicit val uuidMeta: Meta[UUID] = Meta[String].timap(UUID.fromString)(_.toString)

  def createTable: Update0 = sql"""
    CREATE TABLE IF NOT EXISTS courses (
      id UUID PRIMARY KEY,
      title VARCHAR(100) NOT NULL,
      description VARCHAR NOT NULL,
      task_message VARCHAR NOT NULL,
      sr_id UUID UNIQUE,
      submission_deadline DATETIME,
      status VARCHAR NOT NULL
    )
  """.update

  def insert(course: Course): Update0 = sql"""
    INSERT INTO courses (id, title, description, task_message, sr_id, submission_deadline, status)
    VALUES (${course.id}, ${course.title}, ${course.description}, ${course.taskMessage}, ${course.srId}, ${course.submissionDeadline}, ${course.status})
  """.update

  def update(course: Course, id: UUID): Update0 = sql"""
    UPDATE courses
    SET title = ${course.title}, description = ${course.description}, task_message = ${course.taskMessage}, sr_id = ${course.srId}, submission_deadline = ${course.submissionDeadline}, status = ${course.status}
    WHERE id = $id
  """.update

  def select(id: UUID): Query0[Course] = sql"""
    SELECT id, title, description, task_message, sr_id, submission_deadline, status
    FROM courses
    WHERE id = $id
  """.query

  def selectBySR(srId: UUID): Query0[Course] = sql"""
    SELECT id, title, description, task_message, sr_id, submission_deadline, status
    FROM courses
    WHERE sr_id = $srId
  """.query

  def delete(id: UUID): Update0 = sql"""
    DELETE FROM courses WHERE ID = $id
  """.update

  def selectAll: Query0[Course] = sql"""
    SELECT id, title, description, task_message, sr_id, submission_deadline, status
    FROM courses
  """.query

  def selectByStatus(statuses: NonEmptyList[CourseStatus]): Query0[Course] = (sql"""
    SELECT id, title, description, task_message, sr_id, submission_deadline, status
    FROM courses
    WHERE
  """ ++ Fragments.in(fr"status", statuses)).query
}
