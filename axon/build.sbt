name := """axon"""
organization := "com.comicgator"

scalaVersion := "2.12.3"
version := "0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, DockerPlugin)


libraryDependencies ++= Seq(
  guice,
  filters,
  "org.mongodb" % "bson" % "3.5.0",
  "com.eclipsesource" %% "play-json-schema-validator" % "0.9.4",
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "org.postgresql" % "postgresql" % "42.1.4"
)

resolvers ++= Seq(
  "emueller-bintray" at "http://dl.bintray.com/emueller/maven"
)

// DockerPlugin settings
dockerRepository := Some("us.gcr.io/comic-gator")
dockerExposedPorts ++= Seq(9000)
dockerUpdateLatest := true

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.comicgator.binders._"
