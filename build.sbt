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

val http4sVersion          = "0.21.7"
val circeVersion           = "0.13.0"
val circeConfigVersion     = "0.8.0"
val enumeratumCirceVersion = "1.6.1"
val playVersion            = "2.8.2"
val doobieVersion          = "0.13.3"
val catsVersion            = "2.6.0"
val catsTaglessVersion     = "0.11"
val catsEffectVersion      = "2.2.0"
val log4catsVersion        = "1.3.1"
val epimetheusVersion      = "0.4.2"

val log4CatsVersion = "1.1.1"

val scalaTestVersion     = "3.2.7.0"
val h2Version            = "1.4.200"
val slickVersion         = "3.3.3"
val tsecVersion          = "0.2.1"
val kindProjectorVersion = "0.13.0"

libraryDependencies ++= Seq(
  "org.typelevel"            %% "cats-core"                     % catsVersion,
  "org.typelevel"            %% "cats-effect"                   % catsEffectVersion,
  "org.typelevel"            %% "log4cats-slf4j"                % log4catsVersion,
  "org.http4s"               %% "http4s-dsl"                    % http4sVersion,
  "org.http4s"               %% "http4s-blaze-server"           % http4sVersion,
  "org.http4s"               %% "http4s-blaze-client"           % http4sVersion,
  "org.http4s"               %% "http4s-circe"                  % http4sVersion,
  "org.http4s"               %% "http4s-jdk-http-client"        % "0.3.6",
  "io.chrisdavenport"        %% "log4cats-slf4j"                % log4CatsVersion,
  "ch.qos.logback"            % "logback-classic"               % "1.2.3",
  "com.codecommit"           %% "cats-effect-testing-scalatest" % "0.4.1"          % Test,
  "io.chrisdavenport"        %% "epimetheus-http4s"             % epimetheusVersion,
  "org.scalatestplus"        %% "scalacheck-1-15"               % scalaTestVersion % Test,
  "org.scalatestplus"        %% "selenium-3-141"                % scalaTestVersion % Test,
  "org.typelevel"            %% "simulacrum"                    % "1.0.0",
  "org.tpolecat"             %% "atto-core"                     % "0.8.0",
  "io.circe"                 %% "circe-config"                  % circeConfigVersion,
  "io.circe"                 %% "circe-core"                    % circeVersion,
  "io.circe"                 %% "circe-generic"                 % circeVersion,
  "io.circe"                 %% "circe-generic-extras"          % circeVersion,
  "io.circe"                 %% "circe-optics"                  % circeVersion,
  "io.circe"                 %% "circe-parser"                  % circeVersion,
  "com.beachape"             %% "enumeratum-circe"              % enumeratumCirceVersion,
  "org.fusesource.leveldbjni" % "leveldbjni-all"                % "1.8",
  "org.tpolecat"             %% "doobie-core"                   % doobieVersion,
  "org.tpolecat"             %% "doobie-postgres"               % doobieVersion,
  "org.tpolecat"             %% "doobie-postgres-circe"         % doobieVersion,
  "org.tpolecat"             %% "doobie-hikari"                 % doobieVersion,
  "org.mockito"              %% "mockito-scala"                 % "1.16.32"        % Test,
  "org.scalaj"               %% "scalaj-http"                   % "2.4.2"          % Test,
  "org.tpolecat"             %% "doobie-scalatest"              % doobieVersion    % Test,
  "org.typelevel"            %% "cats-tagless-macros"           % catsTaglessVersion,
  "com.h2database"            % "h2"                            % h2Version,
  "eu.timepit"               %% "refined"                       % "0.9.17",
  "com.typesafe.slick"       %% "slick"                         % slickVersion,
  "org.slf4j"                 % "slf4j-nop"                     % "1.6.4",
  "com.typesafe.slick"       %% "slick-hikaricp"                % slickVersion,
  "io.github.jmcardon"       %% "tsec-common"                   % tsecVersion,
  "io.github.jmcardon"       %% "tsec-password"                 % tsecVersion,
  "io.github.jmcardon"       %% "tsec-mac"                      % tsecVersion,
  "io.github.jmcardon"       %% "tsec-signatures"               % tsecVersion,
  "io.github.jmcardon"       %% "tsec-jwt-mac"                  % tsecVersion,
  "io.github.jmcardon"       %% "tsec-jwt-sig"                  % tsecVersion,
  "io.github.jmcardon"       %% "tsec-http4s"                   % tsecVersion,
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % kindProjectorVersion cross CrossVersion.full)

run / fork := true
