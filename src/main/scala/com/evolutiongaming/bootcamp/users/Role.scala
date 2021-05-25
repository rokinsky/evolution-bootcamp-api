package com.evolutiongaming.bootcamp.users

import cats.Eq
import tsec.authorization.{AuthGroup, SimpleAuthEnum}

final case class Role(roleRepr: String)

object Role extends SimpleAuthEnum[Role, String] {
  val Admin:   Role = Role("Admin")
  val Student: Role = Role("Student")

  override val values: AuthGroup[Role] = AuthGroup(Admin, Student)

  override def getRepr(t: Role): String = t.roleRepr

  implicit val eqRole: Eq[Role] = Eq.fromUniversalEquals[Role]
}
