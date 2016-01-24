name := "phillps-hue-go"

version := "1.0"

scalaVersion := "2.11.7"

retrieveManaged := true

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.3",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.3",
  "io.spray" %% "spray-routing" % "1.3.2",
  "io.spray" %% "spray-client" % "1.3.2",
  "io.spray" %% "spray-can" % "1.3.2",
  "io.spray" %% "spray-json" % "1.3.1",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test",
  "io.spray" %% "spray-testkit" % "1.3.2" % "test",
  "org.java-websocket" % "Java-WebSocket" % "1.3.0"
)

