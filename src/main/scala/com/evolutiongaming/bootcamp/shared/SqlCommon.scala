package com.evolutiongaming.bootcamp.shared

import doobie.implicits._
import doobie.{Query0, Read}

object SqlCommon {
  def paginate[A: Read](lim: Int, offset: Int)(q: Query0[A]): Query0[A] =
    (q.toFragment ++ fr"LIMIT $lim OFFSET $offset").query
}
