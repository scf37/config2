lazy val config2 = (project in file("."))
    .settings(

    name := "config2",
    organization := "me.scf37.config2",

    scalaVersion := "2.11.8",

    resolvers += "Scf37" at "https://dl.bintray.com/scf37/maven/",

    libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % "2.11.7"),

    libraryDependencies ++= Seq(
        "me.scf37.expecty" % "expecty" % "0.11" % "test",
        "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
        "org.scalamock" % "scalamock-scalatest-support_2.11" % "3.2" % "test"),

    publish := {},
    releaseTagComment := s"[ci skip]Releasing ${(version in ThisBuild).value}",
    releaseCommitMessage := s"[ci skip]Setting version to ${(version in ThisBuild).value}",

    bintrayOmitLicense := true,

    bintrayVcsUrl := Some("git@github.com:scf37/config2.git")
).settings(Dist.settings)