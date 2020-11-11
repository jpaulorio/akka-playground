val AkkaVersion = "2.6.10"

lazy val commonSettings = Seq(
  version := "0.1-SNAPSHOT",
  organization := "com.jplfds",
  scalaVersion := "2.13.3",
  test in assembly := {}
)

lazy val akkaBasics = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    mainClass in assembly := Some("com.jplfds.AkkaBasics"),
  )

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % Test
libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5")
libraryDependencies += "org.apache.commons" % "commons-math3" % "3.6.1"



