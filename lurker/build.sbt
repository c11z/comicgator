name := "lurker"

scalaVersion := "2.12.3"
version := "0.3-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(JavaAppPackaging, DockerPlugin)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.6.3",
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1",
  "org.seleniumhq.selenium" % "selenium-java" % "3.6.0",
  "org.seleniumhq.selenium" % "htmlunit-driver" % "2.27",
  "org.postgresql" % "postgresql" % "42.1.4",
  "org.mongodb" % "bson" % "3.2.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.google.cloud" % "google-cloud-storage" % "1.6.0"
)

fork := true
