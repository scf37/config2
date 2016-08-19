# config2
![Build status](https://travis-ci.org/scf37/config2.svg?branch=master)
[![Coverage Status](https://coveralls.io/repos/github/scf37/config2/badge.svg?branch=master&42)](https://coveralls.io/github/scf37/config2?branch=master)

Config2 library

Usage:

```
resolvers += "Scf37" at "https://dl.bintray.com/scf37/maven/"
libraryDependencies += "me.scf37.config2" %% "config2" % "1.0.1"
```

Example:
```scala

class MongovConfig private (file: String) {

  def title = "MongoV - MongoDB viewer and editor"

  val env: String = Option(System.getenv("env"))
    .orElse(Option(System.getenv("HOSTNAME")))
    .getOrElse(InetAddress.getLocalHost.getHostName)

  println(s"Using environment $env")

  //look for config properties file in root classpath,
  //overriden by /$env/ folder on classpath,
  //overriden by /data/conf folder
  //additionally, override all values loaded from config file by env variables
  protected val source = Config.from(s"classpath:/")
    .overrideWith(s"classpath:/$env/")
    .overrideWith("/data/conf/")
    .overrideWith(System.getenv())

  val flags = new Flags(source.properties(file).get)

  val port = flags("web.port", "HTTP port to listen on", Some("8888"))(_.toInt)

  val bindAddress = flags("web.bindAddr", "IP interface to bind to", Some("0.0.0.0"))(v => v)

  val dev = flags("web.dev", "Development mode - includes /api endpoint, reloader and more", Some("true"))(_.toBoolean)

  val logRoot = flags("web.logRoot", "Root directory for log files", Some("."))(v => v)

  val logFileName = source.url("log4j2.xml").getOrElse {
    println("WARN: no log4j2.xml found")
    ""
  }
  println("Using log4j2 config at " + logFileName)
  System.setProperty("log4j.configurationFile", logFileName)
}

object Main {

  def main(args: Array[String]): Unit = {
    //load configuration
    val config = new MongovConfig("mongov")

    if (config.flags.errors.nonEmpty) {
      //we have config errors, print them, then print usage help, then exit.
      config.flags.errors.foreach { e =>
        println(e)
      }
      println(config.title)
      println(config.flags.usageString)
      System.exit(1)
    }

    //print configuration params in use
    println(config.title)
    println("Configuration:")
    config.flags.flags.foreach {f =>
      println(f.name + "=" + f.value)
    }
  }
  
  startApp(config)
}

```
