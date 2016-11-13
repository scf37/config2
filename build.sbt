lazy val config2 = (project in file("."))
    .settings(

    name := "config2",
    organization := "me.scf37.config2",

    scalaVersion := "2.12.0",
    crossScalaVersions := Seq("2.11.8", "2.12.0"),
    releaseCrossBuild := true,

    resolvers += "Scf37" at "https://dl.bintray.com/scf37/maven/",

    libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % "2.11.7"),

    libraryDependencies ++= Seq(
        "me.scf37.expecty" %% "expecty" % "1.0.2" % "test",
        "org.scalatest" %% "scalatest" % "3.0.0" % "test"/*,
        "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"*/),

    releaseTagComment := s"[ci skip]Releasing ${(version in ThisBuild).value}",
    releaseCommitMessage := s"[ci skip]Setting version to ${(version in ThisBuild).value}",
    resourceGenerators in Compile <+= buildProperties,

    bintrayOmitLicense := true,

    bintrayVcsUrl := Some("git@github.com:scf37/config2.git")
)