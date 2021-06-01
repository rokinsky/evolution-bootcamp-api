name := "evolution-bootcamp-api"

version := "0.1"

scalaVersion := "2.13.6"

// From https://tpolecat.github.io/2017/04/25/scalac-flags.html
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Ymacro-annotations",
  "-Xfatal-warnings",
  "-Xsource:3"
)

val http4sVersion          = "0.21.24"
val circeVersion           = "0.14.1"
val circeConfigVersion     = "0.8.0"
val enumeratumCirceVersion = "1.6.1"
val doobieVersion          = "0.13.3"
val catsVersion            = "2.6.1"
val catsEffectVersion      = "2.5.1"
val log4catsVersion        = "1.3.1"
val logbackClassicVersion  = "1.2.3"
val flywayVersion          = "6.5.7"
val scalaTestPlusVersion   = "3.2.9.0"
val scalaTestVersion       = "3.2.9"
val tsecVersion            = "0.2.1"

val kindProjectorVersion = "0.13.0"

libraryDependencies ++= Seq(
  "org.typelevel"      %% "cats-core"             % catsVersion,
  "org.typelevel"      %% "cats-effect"           % catsEffectVersion,
  "org.typelevel"      %% "log4cats-slf4j"        % log4catsVersion,
  "org.http4s"         %% "http4s-dsl"            % http4sVersion,
  "org.http4s"         %% "http4s-blaze-server"   % http4sVersion,
  "org.http4s"         %% "http4s-blaze-client"   % http4sVersion,
  "org.http4s"         %% "http4s-circe"          % http4sVersion,
  "org.flywaydb"        % "flyway-core"           % flywayVersion,
  "ch.qos.logback"      % "logback-classic"       % logbackClassicVersion,
  "org.scalatestplus"  %% "scalacheck-1-15"       % scalaTestPlusVersion % Test,
  "org.scalatestplus"  %% "selenium-3-141"        % scalaTestPlusVersion % Test,
  "org.scalatest"      %% "scalatest"             % scalaTestVersion     % Test,
  "io.circe"           %% "circe-config"          % circeConfigVersion,
  "io.circe"           %% "circe-core"            % circeVersion,
  "io.circe"           %% "circe-generic"         % circeVersion,
  "io.circe"           %% "circe-generic-extras"  % circeVersion,
  "io.circe"           %% "circe-optics"          % circeVersion,
  "io.circe"           %% "circe-parser"          % circeVersion,
  "io.circe"           %% "circe-literal"         % circeVersion,
  "com.beachape"       %% "enumeratum-circe"      % enumeratumCirceVersion,
  "org.tpolecat"       %% "doobie-core"           % doobieVersion,
  "org.tpolecat"       %% "doobie-postgres"       % doobieVersion,
  "org.tpolecat"       %% "doobie-postgres-circe" % doobieVersion,
  "org.tpolecat"       %% "doobie-hikari"         % doobieVersion,
  "io.github.jmcardon" %% "tsec-common"           % tsecVersion,
  "io.github.jmcardon" %% "tsec-password"         % tsecVersion,
  "io.github.jmcardon" %% "tsec-mac"              % tsecVersion,
  "io.github.jmcardon" %% "tsec-signatures"       % tsecVersion,
  "io.github.jmcardon" %% "tsec-jwt-mac"          % tsecVersion,
  "io.github.jmcardon" %% "tsec-jwt-sig"          % tsecVersion,
  "io.github.jmcardon" %% "tsec-http4s"           % tsecVersion,
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % kindProjectorVersion cross CrossVersion.full)

run / fork := true
