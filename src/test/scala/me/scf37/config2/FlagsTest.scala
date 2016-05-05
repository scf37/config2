package me.scf37.config2

import org.scalatest.FunSuite

class FlagsTest extends FunSuite {
  test("test Flags") {
    val source = Map("pInt" -> "1", "pString" -> "hello", "pBadInt" -> "1r2")
    val flags = new Flags(source)

    assert(flags("pInt", "desc1", Some("42"))(_.toInt) == 1)
    assert(flags("pString", "desc2", None)(identity) == "hello")
    assert(flags("pMissing", "desc3", Some("world"))(identity) == "world")
    assert(flags("pBadInt", "desc4", None)(_.toInt) == 0)
    assert(flags("pMissing2", "desc5", None)(identity) == null)

    assert(flags.errors.length == 2)
    assert(flags.flags.length == 5)
    Seq("desc1", "desc2", "desc5", "world").foreach {s =>
      assert(flags.usageString.contains(s))
    }

  }

}
