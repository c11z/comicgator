name := "lurker"

scalaVersion := "2.12.3"
version := "0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(JavaAppPackaging, DockerPlugin)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.6.3",
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1",
  "org.seleniumhq.selenium" % "selenium-java" % "3.5.3",
  "org.postgresql" % "postgresql" % "42.1.4",
  "org.mongodb" % "bson" % "3.2.2",
//  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

fork := true
