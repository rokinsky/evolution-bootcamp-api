name := "evolution-bootcamp-api"

version := "0.1"

scalaVersion := "2.13.8"

// From https://tpolecat.github.io/2017/04/25/scalac-flags.html
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Xfatal-warnings",
  "-explaintypes"
)

scalacOptions ++= (
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 13)) => Seq("-Xsource:3", "-Ymacro-annotations")
    case _             => Seq("-Ykind-projector", "-source:3.0-migration", "-rewrite", "-indent", "-Xignore-scala2-macros")
  }
)

val http4sVersion          = "0.23.7"
val circeVersion           = "0.14.1"
val circeConfigVersion     = "0.8.0"
val enumeratumCirceVersion = "1.6.1"
val doobieVersion          = "1.0.0-RC2"
val catsVersion            = "2.7.0"
val catsEffectVersion      = "3.3.4"
val log4catsVersion        = "2.1.1"
val logbackClassicVersion  = "1.2.10"
val flywayVersion          = "6.5.7"
val scalaTestPlusVersion   = "3.2.9.0"
val scalaTestVersion       = "3.2.9"
val tsecVersion            = "0.4.0"

val kindProjectorVersion = "0.13.2"

libraryDependencies ++= Seq(
  "org.typelevel"      %% "cats-core"             % catsVersion,
  "org.typelevel"      %% "cats-effect"           % catsEffectVersion,
  "org.typelevel"      %% "log4cats-slf4j"        % log4catsVersion,
  "org.http4s"         %% "http4s-dsl"            % http4sVersion,
  "org.http4s"         %% "http4s-blaze-server"   % http4sVersion,
  "org.http4s"         %% "http4s-blaze-client"   % http4sVersion,
  "org.http4s"         %% "http4s-circe"          % http4sVersion,
  "org.flywaydb"        % "flyway-core"           % flywayVersion cross CrossVersion.disabled,
  "ch.qos.logback"      % "logback-classic"       % logbackClassicVersion cross CrossVersion.disabled,
  "org.scalatestplus"  %% "scalacheck-1-15"       % scalaTestPlusVersion % Test,
  "org.scalatestplus"  %% "selenium-3-141"        % scalaTestPlusVersion % Test,
  "org.scalatest"      %% "scalatest"             % scalaTestVersion     % Test,
  "io.circe"           %% "circe-config"          % circeConfigVersion cross CrossVersion.for3Use2_13,
  "io.circe"           %% "circe-core"            % circeVersion,
  "io.circe"           %% "circe-generic"         % circeVersion,
  "io.circe"           %% "circe-generic-extras"  % circeVersion cross CrossVersion.for3Use2_13,
  "io.circe"           %% "circe-optics"          % circeVersion cross CrossVersion.for3Use2_13,
  "io.circe"           %% "circe-parser"          % circeVersion,
  "io.circe"           %% "circe-literal"         % circeVersion cross CrossVersion.for3Use2_13, // scala 2 macro
  "com.beachape"       %% "enumeratum-circe"      % enumeratumCirceVersion cross CrossVersion.for3Use2_13,
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

libraryDependencies ++= (
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 13)) =>
      Seq(compilerPlugin("org.typelevel" %% "kind-projector" % kindProjectorVersion cross CrossVersion.full))
    case _ => Seq()
  }
)

run / fork := true
