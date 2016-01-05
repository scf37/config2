package me.scf37.config

import java.util.Properties

import me.scf37.config2.Config
import org.expecty.Expecty
import org.scalatest.FunSuite

class ConfigTest extends FunSuite {
  val test = new Expecty
  
  test("test Config override by url") {
    val c = Config.from("classpath:test/a")
        .overrideWith("classpath:/test/a/b")
        .log(println, true)

    val props = c.properties("1.properties").get
    
    test { //merge with override
      props("key1") == "value11"
      props("key2") == "2"
    }
    
    val file = c.text("1.properties")
    
    test { //taking highest-precedence file
      file.get == "key1=value11\nkey2=2"
    }
  }
  
  test("test Config override by Properties") {
    val p = new Properties
    p.put("key1", "true")
    
    val c = Config.from("classpath:test/a")
        .overrideWith(p)
        .log(println, true)

    val props = c.properties("1.properties").get
    
    test {
      props("key1") == "true"
    }
  }
}