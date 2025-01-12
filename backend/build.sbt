import com.github.sbt.jacoco.JacocoPlugin.autoImport.{JacocoReportSettings, jacocoReportSettings}
import sbtassembly.AssemblyPlugin.autoImport.*
import sbtassembly.{MergeStrategy, PathList}

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

ThisBuild / parallelExecution := false

lazy val root = (project in file("."))
  .enablePlugins(JacocoPlugin)
  .settings(
    name := "backend",
  )
  .settings(
    jacocoReportSettings := JacocoReportSettings()
//    .withThresholds(
//      TODO: Change thresholds
//      JacocoThresholds(
//        instruction = 80,
//        method = 100,
//        branch = 100,
//        complexity = 100,
//        line = 90,
//        clazz = 100)
//    )
    .withFormats(
      JacocoReportFormats.XML
    )
    .withTitle("jacoco"))

// Fix assembly merge strategy to avoid native-image issues
ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", "native-image", _*) => MergeStrategy.discard
  case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.discard
  case x => (assembly / assemblyMergeStrategy).value(x)
}

libraryDependencies ++= Seq(
  // http4s
  "org.http4s"                   % "http4s-dsl_3"                 % "1.0.0-M43",
  "org.http4s"                   % "http4s-ember-server_3"        % "1.0.0-M43",
  "org.http4s"                   % "http4s-ember-client_3"        % "1.0.0-M43",
  "org.http4s"                   % "http4s-circe_3"               % "1.0.0-M43",
  "org.scalaj" %% "scalaj-http" % "2.4.2",

  // Logging
  "org.typelevel"                % "log4cats-slf4j_3"             % "2.7.0",
  "org.typelevel"                % "log4cats-core_3"              % "2.7.0",
  "org.slf4j"                    % "slf4j-simple"                  % "2.0.16",

  // MongoDB
  "org.mongodb.scala"            % "mongo-scala-driver_2.13"      % "5.2.1",

  // Encryption
  "com.github.jwt-scala" %% "jwt-core" % "10.0.1",
  "de.mkammerer" % "argon2-jvm" % "2.1",




// Testcontainers for Mongo
  "com.dimafeng" %% "testcontainers-scala-mongodb" % "0.41.5" % Test,
  "com.dimafeng" %% "testcontainers-scala-munit" % "0.41.5",

  // Circe
  "io.circe"                     % "circe-core_3"                 % "0.14.10",
  "io.circe"                     % "circe-generic_3"              % "0.14.10",
  "io.circe"                     % "circe-parser_3"               % "0.14.10",

  // Mail + config
  "com.sun.mail"                 % "javax.mail"                    % "1.6.2",
  "com.typesafe"                 % "config"                        % "1.4.3",

  // Test dependencies
  "org.typelevel" % "munit-cats-effect_3" % "2.0.0" % Test,
  "org.scalameta" % "munit_3" % "1.0.4" % Test,
  "org.mockito" % "mockito-scala_2.13" % "1.17.37" % Test,
)

Compile / run / mainClass := Some("com.just.donate.Server")
Compile / packageOptions +=
  Package.ManifestAttributes("Main-Class" -> "com.just.donate.Server")

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Xmax-inlines",
  "100"
)
