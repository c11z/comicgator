import play.sbt.routes.RoutesKeys

name := """maestro"""

version := "0.4.0-SNAPSHOT"

maintainer := "Cory Dominguez"

organization := "com.comicgator"

scalaVersion := "2.11.8"

lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(DockerPlugin)

// Docker settings from Native Packager

dockerRepository := Some("comicgator-docker-production.bintray.io")

dockerExposedPorts ++= Seq(9000)

dockerUpdateLatest := true

libraryDependencies ++= Seq(
  ws,
  cache,
  filters,
  "org.mongodb" % "bson" % "3.2.2",
  "com.typesafe.play" %% "play-slick" % "2.0.2",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.86",
  "org.seleniumhq.selenium" % "selenium-java" % "2.35.0",
  "com.eclipsesource" %% "play-json-schema-validator" % "0.8.6",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

resolvers ++= Seq(
  "emueller-bintray" at "http://dl.bintray.com/emueller/maven",
  "Comic Gator Bintray" at "https://dl.bintray.com/comicgator/maven"
)

RoutesKeys.routesImport += "binders.Binders._"
