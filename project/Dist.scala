import java.io.StringWriter
import java.util.Properties

import sbt.Keys._
import sbt._

object Dist {
  lazy val incrementVersion = taskKey[Unit]("Increment application version")
  lazy val dist = taskKey[Unit]("Build versioned application distribution")
//  lazy val writeBuildProperties = taskKey[File]("Write build.properties file to resources")

  lazy val settings = Seq(
    dist <<= distTask,
    resourceGenerators in Compile <+= writeBuildPropertiesTask
  )

  private[this] lazy val distTask = Def.sequential(incrementVersionTask, publish, Def.task {
    val log = streams.value.log
    log.info("Release complete, distribution is available at " + (target.value / "dist"))
  })

  private[this] lazy val writeBuildPropertiesTask = Def.task[Seq[File]] {
    val props = Map(
    "name" -> name.value,
    "version" -> version.value,
    "build_revision" -> ("git rev-parse HEAD"!!).trim,
    "scm_repository" -> ("git config --get remote.origin.url"!!).trim,
    "build_last_few_commits" -> (Seq("git", "log", "-n", "5", "--pretty=%h %ad %an %s")!!))

    val propString = {
      val p = new Properties()
      props.foreach { case (k, v) =>
        p.setProperty(k, v)
      }
      val sw = new StringWriter()
      p.store(sw, "")
      sw.toString
    }

    val file = resourceManaged.value / "build.properties"

    IO.write(file, propString.getBytes("UTF-8"))

    Seq(file)
  }

  private[this] lazy val incrementVersionTask = Def.task[File] {
    val log = streams.value.log

    if (("git diff-index --quiet HEAD --"!) != 0) {
      log.error("There are uncommited files")
      throw new IllegalStateException("There are uncommited files")
    }

    val nextVersion = (version.value.toInt + 1).toString
    val versionFile = baseDirectory.value / "version.sbt"
    val q = "\""
    IO.write(versionFile, IO.read(versionFile).replace(s"$q${version.value}$q", "\"" + nextVersion + "\""))

    log.info(s"git add $versionFile")
    s"git add $versionFile"!!

    log.info(s"git commit -m $q[ci skip]Releasing version ${version.value}$q")
    Seq("git", "commit", "-m", s"$q[ci skip]Releasing version ${version.value}$q")!!

    versionFile
  }

}