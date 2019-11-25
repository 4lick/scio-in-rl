import sbt._
import Keys._

val scioVersion = "0.8.0-beta2"
val beamVersion = "2.16.0"
val scalaMacrosVersion = "2.1.1"

lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "me.a4lick.beam",
  // Semantic versioning http://semver.org/
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.8",
  scalacOptions ++= Seq("-target:jvm-1.8",
                        "-deprecation",
                        "-feature",
                        "-unchecked"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
)

lazy val paradiseDependency =
  "org.scalamacros" % "paradise" % scalaMacrosVersion cross CrossVersion.full
lazy val macroSettings = Seq(
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  addCompilerPlugin(paradiseDependency)
)

lazy val root: Project = project
  .in(file("."))
  .settings(commonSettings)
  .settings(macroSettings)
  .settings(
    name := "color-beam",
    description := "color-beam",
    publish / skip := true,
    libraryDependencies ++= Seq(
      "com.spotify" %% "scio-core" % scioVersion,
      "com.spotify" %% "scio-test" % scioVersion % Test,
      "com.spotify" %% "scio-bigquery" % scioVersion,
      "com.spotify" %% "scio-sql" % scioVersion,
      "org.apache.beam" % "beam-runners-direct-java" % beamVersion,
      "org.apache.beam" % "beam-runners-google-cloud-dataflow-java" % beamVersion,
      "org.slf4j" % "slf4j-simple" % "1.7.25",
      "com.github.mpilquist" %% "simulacrum" % "0.13.0",
      "org.scalaz"           %% "scalaz-core" % "7.2.26",
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "com.typesafe.play" %% "play-json-joda" % "2.6.10",
      "com.typesafe.play" %% "play-json" % "2.6.10",
      "org.apache.commons" % "commons-lang3" % "3.7",
      "org.elasticsearch.client" % "rest" % "5.5.3",
      "com.google.auto.value" % "auto-value" % "1.3",
      "com.typesafe.play" %% "play-ws" % "2.6.10",
      "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.1",
      "com.typesafe.play" %% "play-ws-standalone-json" % "1.1.1"
    )
  )
  .enablePlugins(PackPlugin)

lazy val repl: Project = project
  .in(file(".repl"))
  .settings(commonSettings)
  .settings(macroSettings)
  .settings(
    name := "repl",
    description := "Scio REPL for color-beam",
    libraryDependencies ++= Seq(
      "com.spotify" %% "scio-repl" % scioVersion
    ),
    Compile / mainClass := Some("com.spotify.scio.repl.ScioShell"),
    publish / skip := true
  )
  .dependsOn(root)
