name := "lurker"

scalaVersion := "2.12.3"
version := "0.3.5-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(JavaAppPackaging, DockerPlugin)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.6.8",
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1",
  "org.seleniumhq.selenium" % "selenium-java" % "3.8.1",
  "org.seleniumhq.selenium" % "htmlunit-driver" % "2.29.0",
  "org.postgresql" % "postgresql" % "42.1.4",
  "org.mongodb" % "bson" % "3.6.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.google.cloud" % "google-cloud-storage" % "1.14.0"
)

fork := true
