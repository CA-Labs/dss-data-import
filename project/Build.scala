import sbt._
import Keys._
import xerial.sbt.Pack._

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
    settings = buildSettings 
    ++ packAutoSettings
    ++ Seq(
      libraryDependencies ++= Seq(
        "jaxen" % "jaxen" % "1.1.6",
        "dom4j" % "dom4j" % "1.6.1",
        "io.gatling" %% "jsonpath" % "0.6.1",
        "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.2",
        "org.scalatest" %% "scalatest" % "2.2.1",
        "com.github.scopt" %% "scopt" % "3.2.0"
      ),
      resolvers += Resolver.sonatypeRepo("public")
    )
  )
}
