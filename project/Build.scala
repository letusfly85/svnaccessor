import sbt._
import Keys._

object ProjectBuild extends Build {
  lazy val root = Project(
    id ="svnaccessor",
    base = file("."),
    settings =
      Defaults.defaultSettings
        ++ Seq(
        scalaVersion := "2.9.2",
        organization := "com.jellyfish85",
        version := "1.0-SNAPSHOT",
        scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
        parallelExecution := true,
        crossPaths := false
      )
  )
}
