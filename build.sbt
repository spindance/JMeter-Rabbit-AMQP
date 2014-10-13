SbtGitTags.loadPlugin

organization := "com.spindance"

name := "jmeter-amqp"

version := "6.feature-response-data"

sbtVersion := "0.13.0"

scalaVersion := "2.10.2"

javacOptions ++= Seq("-Xlint", "-Xlint:-path", "-Xlint:-deprecation")

javacOptions in doc := Seq()

autoScalaLibrary := false

crossPaths := false

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-lang3" % "3.0",
  "commons-logging" % "commons-logging" % "1.1.1",
  "org.apache.jmeter" % "jorphan" % "2.6",
  "com.rabbitmq" % "amqp-client" % "3.3.4",
  "org.apache.jmeter" % "ApacheJMeter_core" % "2.11"
)

// Publishing
credentials += Credentials(Path.userHome / ".sbt" / "0.13" / "spindance")

publishMavenStyle := true

publishTo := Some("Artifactory Realm" at "http://spindance.artifactoryonline.com/spindance/spindance.releases-local")

// disable publishing the main sources jar
publishArtifact in (Compile, packageSrc) := false
