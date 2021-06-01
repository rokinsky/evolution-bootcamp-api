package com.evolutiongaming.bootcamp

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class HelloSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks {
  "sum" should "work for all ints correctly" in {
    forAll { (x: Int, y: Int) =>
      x + y shouldEqual x + y
    }
  }
}
