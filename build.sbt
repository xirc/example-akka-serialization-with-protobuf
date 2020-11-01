name := "example-akka-serialization-with-protobuf"
version := "0.1"
scalaVersion := "2.13.3"

val AkkaVersion = "2.6.10"

lazy val mylib = (project in file("mylib"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.2" % Test,
    ),
    PB.targets in Compile := Seq(
      scalapb.gen(flatPackage = true) -> (sourceManaged in Compile).value / "scalapb",
    ),
  )

lazy val myapp = (project in file("myapp"))
  .disablePlugins(ProtocPlugin)
  .dependsOn(mylib)
