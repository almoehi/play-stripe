name := "play-stripe"

version := "1.0-SNAPSHOT"

organization := "org.almoehi"

scalaVersion := "2.11.5"

lazy val root = (project in file(".")) //.addPlugins(PlayScala).addPlugins(SbtWeb)

// Common.settings

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.3.11" % "test",
  "com.typesafe.play" % "play-ws_2.11" % "2.3.9",
  "com.typesafe.play" % "play-json_2.11" % "2.3.9"
)

