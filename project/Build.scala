import sbt._
import Keys._

object BuildSettings {
  val buildSettings = Defaults.defaultSettings ++ Seq(
    name := "dss-data-import",
    organization := "com.calabs",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.10.3"
  )
}

object MyBuild extends Build {
  import BuildSettings._

  lazy val dssDataImport: Project = Project(
    "dss-data-import",
    file("."),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
        "io.gatling" %% "jsonpath" % "0.6.1",
        "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.2",
        "org.scalatest" %% "scalatest" % "2.2.1" % "test"
      )
    )
  )
}
