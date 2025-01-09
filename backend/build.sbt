ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

ThisBuild / parallelExecution := false

lazy val root = (project in file("."))
  .settings(
    name := "backend"
  )

libraryDependencies ++= Seq(
  // http4s
  "org.http4s"                   % "http4s-dsl_3"                 % "1.0.0-M43",
  "org.http4s"                   % "http4s-ember-server_3"        % "1.0.0-M43",
  "org.http4s"                   % "http4s-ember-client_3"        % "1.0.0-M43",
  "org.http4s"                   % "http4s-circe_3"               % "1.0.0-M43",

  // Logging
  "org.typelevel"                % "log4cats-slf4j_3"             % "2.7.0",
  "org.typelevel"                % "log4cats-core_3"              % "2.7.0",
  "org.slf4j"                    % "slf4j-simple"                  % "2.0.16",

  // MongoDB
  "org.mongodb.scala"            % "mongo-scala-driver_2.13"      % "5.2.1",

  // Circe
  "io.circe"                     % "circe-core_3"                 % "0.14.10",
  "io.circe"                     % "circe-generic_3"              % "0.14.10",
  "io.circe"                     % "circe-parser_3"               % "0.14.10",

  // Mail + config
  "com.sun.mail"                 % "javax.mail"                    % "1.6.2",
  "com.typesafe"                 % "config"                        % "1.4.3",

  // Test dependencies
  "org.typelevel"                % "munit-cats-effect_3"          % "2.0.0"   % Test,
  "org.scalameta"                % "munit_3"                      % "1.0.4"   % Test,
)

Compile / run / assembly / mainClass := Some("com.just.donate.Server")
Compile / packageOptions +=
  Package.ManifestAttributes("Main-Class" -> "com.just.donate.Server")

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Xmax-inlines",
  "100"
)

assemblyMergeStrategy in assembly := {
  case PathList("jackson-annotations-2.10.3.jar", xs @ _*) => MergeStrategy.last
  case PathList("jackson-core-2.10.3.jar", xs @ _*) => MergeStrategy.last
  case PathList("jackson-databind-2.10.3.jar", xs @ _*) => MergeStrategy.last
  case PathList("jackson-dataformat-cbor-2.10.3.jar", xs @ _*) => MergeStrategy.last
  case PathList("jackson-datatype-jdk8-2.10.3.jar", xs @ _*) => MergeStrategy.last
  case PathList("jackson-datatype-jsr310-2.10.3.jar", xs @ _*) => MergeStrategy.last
  case PathList("jackson-module-parameter-names-2.10.3.jar", xs @ _*) => MergeStrategy.last
  case PathList("jackson-module-paranamer-2.10.3.jar", xs @ _*) => MergeStrategy.last
  case _ => MergeStrategy.first
}
