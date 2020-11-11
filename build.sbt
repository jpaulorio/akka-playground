scalaVersion := "2.13.3"

organization := "com.jplfds"

val AkkaVersion = "2.6.10"

mainClass := Some("AkkaBasics")

resolvers += "Maven Central" at "https://repo1.maven.org/maven2/"

name := "AkkaBasics"

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % Test
libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5")
libraryDependencies += "org.apache.commons" % "commons-math3" % "3.6.1"

