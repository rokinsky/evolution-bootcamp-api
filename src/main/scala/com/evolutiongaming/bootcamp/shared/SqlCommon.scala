package com.evolutiongaming.bootcamp.shared

import doobie.implicits._
import doobie.{Meta, Query0, Read}

import java.time.{LocalDate, Year}
import java.util.UUID

object SqlCommon {
  implicit val uuidMeta:      Meta[UUID]      = Meta[String].timap(UUID.fromString)(_.toString)
  implicit val yearMeta:      Meta[Year]      = Meta[Int].timap(Year.of)(_.getValue)
  implicit val localDateMeta: Meta[LocalDate] = Meta[String].timap(LocalDate.parse)(_.toString)

  def paginate[A: Read](lim: Int, offset: Int)(q: Query0[A]): Query0[A] =
    (q.toFragment ++ fr"LIMIT $lim OFFSET $offset").query
}
