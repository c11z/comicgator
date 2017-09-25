name := "lurker"

scalaVersion := "2.12.3"
version := "0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(JavaAppPackaging, DockerPlugin)

lazy val akkaVersion = "2.5.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.play" %% "play-json" % "2.6.3",
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1",
  "org.postgresql" % "postgresql" % "42.1.4",
  "org.mongodb" % "bson" % "3.2.2",
  "org.slf4j" % "slf4j-nop" % "1.6.4",

  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
