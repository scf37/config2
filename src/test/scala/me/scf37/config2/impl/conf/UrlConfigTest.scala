package me.scf37.config2.impl.conf

import java.nio.file.Paths

import me.scf37.config2.impl.ConfigLogger
import me.scf37.config2.impl.UrlReader
import org.expecty.Expecty
import org.scalatest.FunSuite

import scala.io.Source


class UrlConfigTest extends FunSuite  {
  val test = new Expecty()

  test("read classpath resource - from root") {
    val conf = new UrlReader("classpath:", ConfigLoggerStub)
    
    test {
      conf.readProperties("test/a/1.properties").isDefined
      conf.readProperties("/test/a/1.properties").isDefined
      conf.readProperties("x/test/a/1.properties") == None
    }
  }
  
  test("read classpath resource - from root2") {
    val conf = new UrlReader("classpath:/", ConfigLoggerStub)
    
    test {
      conf.readProperties("test/a/1.properties").isDefined
      conf.readProperties("/test/a/1.properties").isDefined
      conf.readProperties("x/test/a/1.properties") == None
    }
  }
  
  test("read classpath resource - from folder") {
    val conf = new UrlReader("classpath:test", ConfigLoggerStub)
    
    test {
      conf.readProperties("a/1.properties").isDefined
      conf.readProperties("/a/1.properties").isDefined
      conf.readProperties("x/test/a/1.properties") == None
    }
  }
  
  test("read classpath resource - from folder2") {
    val conf = new UrlReader("classpath:test/", ConfigLoggerStub)
    
    test {
      conf.readProperties("a/1.properties").isDefined
      conf.readProperties("/a/1.properties").isDefined
      conf.readProperties("x/test/a/1.properties") == None
    }
  }
  
  test("read classpath resource - from folder3") {
    val conf = new UrlReader("classpath:/test/", ConfigLoggerStub)
    
    test {
      conf.readProperties("a/1.properties").isDefined
      conf.readProperties("/a/1.properties").isDefined
    }
  }
  
  test("read classpath resource - from folder4") {
    val conf = new UrlReader("classpath:/test/a", ConfigLoggerStub)
    
    test {
      conf.readProperties("1.properties").isDefined
      conf.readProperties("/1.properties").isDefined
    }
  }
  
  test("Reading of existing classpath resource returns expected body") {
    val conf = new UrlReader("classpath:/test/a", ConfigLoggerStub)
    
    test {
      conf.readProperties("1.properties").get == Map("key1" -> "value1")
      conf.readText("1.properties", "UTF-8").get == "key1=value1"
      Source.fromInputStream(conf.readFile("1.properties").get).getLines().mkString == "key1=value1"
    }
  }
  
  test("Reading of non-existing classpath resource returns None") {
    val conf = new UrlReader("classpath:/test/a", ConfigLoggerStub)
    
    test {
      conf.readProperties("x1.properties") == None
      conf.readText("x1.properties", "UTF-8") == None
      conf.readFile("x1.properties") == None
    }
  }
  
  test("read file resource - absolute path") {
    val conf = new UrlReader(Paths.get("src/test/resources").toAbsolutePath().toString, ConfigLoggerStub)
    
    test {
      conf.readProperties("test/a/1.properties").isDefined
      conf.readProperties("/test/a/1.properties").isDefined
      conf.readProperties("x/test/a/1.properties") == None
    }
  }
  
  test("read file resource - absolute path2") {
    val conf = new UrlReader(Paths.get("src/test/resources").toAbsolutePath().toString + "/", ConfigLoggerStub)
    
    test {
      conf.readProperties("test/a/1.properties").isDefined
      conf.readProperties("/test/a/1.properties").isDefined
      conf.readProperties("x/test/a/1.properties") == None
    }
  }
  
  test("read file resource - relative path") {
    val conf = new UrlReader(Paths.get("src/test/resources").toString, ConfigLoggerStub)
    
    test {
      conf.readProperties("test/a/1.properties").isDefined
      conf.readProperties("/test/a/1.properties").isDefined
      conf.readProperties("x/test/a/1.properties") == None
    }
  }
  
  test("Reading of existing file resource returns expected body") {
    val conf = new UrlReader(Paths.get("src/test/resources/test/a").toString, ConfigLoggerStub)
    
    test {
      conf.readProperties("1.properties").get == Map("key1" -> "value1")
      conf.readText("1.properties", "UTF-8").get == "key1=value1"
      Source.fromInputStream(conf.readFile("1.properties").get).getLines().mkString == "key1=value1"
    }
  }
  
  test("Reading of non-existing file resource returns None") {
    val conf = new UrlReader(Paths.get("src/test/resources/test/a").toString, ConfigLoggerStub)
    
    test {
      conf.readProperties("x1.properties") == None
      conf.readText("x1.properties", "UTF-8") == None
      conf.readFile("x1.properties") == None
    }
  }
  
  test("read file resource - relative path with explicit file: prefix") {
    val conf = new UrlReader("file:" + Paths.get("src/test/resources/test/a").toString, ConfigLoggerStub)
    
    test {
      conf.readProperties("1.properties").get == Map("key1" -> "value1")
      conf.readText("1.properties", "UTF-8").get == "key1=value1"
      Source.fromInputStream(conf.readFile("1.properties").get).getLines().mkString == "key1=value1"
    }
  }
  
  test("read resource - unknown protocol should throw exception") {
    val conf = new UrlReader("xxx:" + Paths.get("src/test/resources/test/a").toString, ConfigLoggerStub)
    
    intercept[Exception] {
      conf.readProperties("1.properties")
    }
    
    intercept[Exception] {
      conf.readText("1.properties", "UTF-8")
    }
    
    intercept[Exception] {
      conf.readFile("1.properties")
    }
  }
}

private object ConfigLoggerStub extends ConfigLogger {
  override def info(msg: String): Unit = {}
  override def debug(msg: String): Unit = {}
}
