name    := "ormy"
version := "0.0.1-SNAPSHOT"

scalaVersion := "2.13.6"

scalacOptions ++= Seq(
  "-Ymacro-annotations"
)

val libraries = new {
  object Version {
    val cats        = "2.8.0"
    val circe       = "0.14.2"
    val http4s      = "0.23.15"
    val logback     = "1.4.0"
    val log4Cats    = "2.3.2"
    val tapir       = "1.1.0"
    val derevo      = "0.13.0"
    val sttpApiSpec = "0.2.1"
    val refined     = "0.10.1"
    val newType     = "0.4.4"
  }

  val catsCore = "org.typelevel" %% "cats-core" % Version.cats

  val circeCore            = "io.circe"                      %% "circe-core"          % Version.circe
  val circeGeneric         = "io.circe"                      %% "circe-generic"       % Version.circe
  val circeRefined         = "io.circe"                      %% "circe-refined"       % Version.circe
  val http4sCirce          = "org.http4s"                    %% "http4s-circe"        % Version.http4s
  val http4sDsl            = "org.http4s"                    %% "http4s-dsl"          % Version.http4s
  val http4sEmberServer    = "org.http4s"                    %% "http4s-ember-server" % Version.http4s
  val http4sEmberClient    = "org.http4s"                    %% "http4s-ember-client" % Version.http4s
  val log4Cats             = "org.typelevel"                 %% "log4cats-slf4j"      % Version.log4Cats
  val sttpApiSpecCirceYaml = "com.softwaremill.sttp.apispec" %% "openapi-circe-yaml"  % Version.sttpApiSpec
  val tapirCats            = "com.softwaremill.sttp.tapir"   %% "tapir-cats"          % Version.tapir
  val tapirCirce           = "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"    % Version.tapir
  val tapirCore            = "com.softwaremill.sttp.tapir"   %% "tapir-core"          % Version.tapir
  val tapirHttp4s          = "com.softwaremill.sttp.tapir"   %% "tapir-http4s-server" % Version.tapir
  val tapirOpenApiDocs     = "com.softwaremill.sttp.tapir"   %% "tapir-openapi-docs"  % Version.tapir
  val tapirSwaggerUi       = "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui"    % Version.tapir
  val tapirDerevo          = "com.softwaremill.sttp.tapir"   %% "tapir-derevo"        % Version.tapir
  val tapirRefined         = "com.softwaremill.sttp.tapir"   %% "tapir-refined"       % Version.tapir
  val tapirNewType         = "com.softwaremill.sttp.tapir"   %% "tapir-newtype"       % Version.tapir
  val derevoCirce          = "tf.tofu"                       %% "derevo-circe"        % Version.derevo
  val derevoCats           = "tf.tofu"                       %% "derevo-cats"         % Version.derevo
  val logback              = "ch.qos.logback"                 % "logback-classic"     % Version.logback
  val newType              = "io.estatico"                   %% "newtype"             % Version.newType
  val refinedCore          = "eu.timepit"                    %% "refined"             % Version.refined
  val refinedCats          = "eu.timepit"                    %% "refined-cats"        % Version.refined

}

libraryDependencies ++= Seq(
  libraries.catsCore,
  libraries.circeCore,
  libraries.circeGeneric,
  libraries.circeRefined,
  libraries.newType,
  libraries.refinedCore,
  libraries.refinedCats,
  libraries.http4sCirce,
  libraries.http4sDsl,
  libraries.http4sEmberServer,
  libraries.http4sEmberClient,
  libraries.log4Cats,
  libraries.sttpApiSpecCirceYaml,
  libraries.tapirCats,
  libraries.tapirCirce,
  libraries.tapirCore,
  libraries.tapirHttp4s,
  libraries.tapirOpenApiDocs,
  libraries.tapirSwaggerUi,
  libraries.tapirDerevo,
  libraries.tapirRefined,
  libraries.tapirNewType,
  libraries.derevoCirce,
  libraries.logback
)

addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full)
addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
