name := "example-akka-serialization-with-protobuf"
ThisBuild / scalaVersion := "2.13.7"
ThisBuild / scalacOptions := Seq(
  "-encoding",
  "utf8",
  "-deprecation",
  "unchecked",
  "-feature",
  "-Xlint",
)

lazy val AkkaVersion = "2.6.17"
lazy val AkkaHttpVersion = "10.2.7"
lazy val LogbackVersion = "1.2.7"
lazy val ScalaTestVersion = "3.2.10"

lazy val mylib = (project in file("mylib"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
      "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
    ),
    Compile / PB.targets := Seq(
      scalapb.gen(flatPackage = true) -> (Compile / sourceManaged).value / "scalapb",
    ),
  )

lazy val myapp = (project in file("myapp"))
  .disablePlugins(ProtocPlugin)
  .dependsOn(mylib)
  .settings(
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    ),
  )

addCommandAlias(
  "ciFormat",
  Seq(
    "scalafmtSbt",
    "scalafmtAll",
  ).mkString(";"),
)

addCommandAlias(
  "ciCheck",
  Seq(
    "clean",
    "scalafmtSbtCheck",
    "scalafmtCheckAll",
    "test:compile",
    "test",
  ).mkString(";"),
)
