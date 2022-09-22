lazy val dependencies = new {
  object Version {
    val cats        = "2.8.0"
    val catsEffect  = "3.3.12"
    val circe       = "0.14.2"
    val http4s      = "0.23.15"
    val logback     = "1.4.0"
    val log4Cats    = "2.4.0"
    val tapir       = "1.1.0"
    val derevo      = "0.13.0"
    val sttpApiSpec = "0.2.1"
    val refined     = "0.10.1"
    val newType     = "0.4.4"
    val akka        = "2.6.20"
    val akkaHttp    = "10.2.10"
    val ciris       = "2.4.0"
  }

  val catsCore             = "org.typelevel"                 %% "cats-core"              % Version.cats
  val catsEffect           = "org.typelevel"                 %% "cats-effect"            % Version.catsEffect
  val circeCore            = "io.circe"                      %% "circe-core"             % Version.circe
  val circeGeneric         = "io.circe"                      %% "circe-generic"          % Version.circe
  val circeRefined         = "io.circe"                      %% "circe-refined"          % Version.circe
  val http4sCirce          = "org.http4s"                    %% "http4s-circe"           % Version.http4s
  val http4sDsl            = "org.http4s"                    %% "http4s-dsl"             % Version.http4s
  val http4sEmberServer    = "org.http4s"                    %% "http4s-ember-server"    % Version.http4s
  val http4sEmberClient    = "org.http4s"                    %% "http4s-ember-client"    % Version.http4s
  val log4Cats             = "org.typelevel"                 %% "log4cats-slf4j"         % Version.log4Cats
  val sttpApiSpecCirceYaml = "com.softwaremill.sttp.apispec" %% "openapi-circe-yaml"     % Version.sttpApiSpec
  val tapirCats            = "com.softwaremill.sttp.tapir"   %% "tapir-cats"             % Version.tapir
  val tapirCirce           = "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"       % Version.tapir
  val tapirCore            = "com.softwaremill.sttp.tapir"   %% "tapir-core"             % Version.tapir
  val tapirHttp4s          = "com.softwaremill.sttp.tapir"   %% "tapir-http4s-server"    % Version.tapir
  val tapirOpenApiDocs     = "com.softwaremill.sttp.tapir"   %% "tapir-openapi-docs"     % Version.tapir
  val tapirSwaggerUi       = "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui"       % Version.tapir
  val tapirDerevo          = "com.softwaremill.sttp.tapir"   %% "tapir-derevo"           % Version.tapir
  val tapirRefined         = "com.softwaremill.sttp.tapir"   %% "tapir-refined"          % Version.tapir
  val tapirNewType         = "com.softwaremill.sttp.tapir"   %% "tapir-newtype"          % Version.tapir
  val derevoCirce          = "tf.tofu"                       %% "derevo-circe"           % Version.derevo
  val derevoCats           = "tf.tofu"                       %% "derevo-cats"            % Version.derevo
  val logback              = "ch.qos.logback"                 % "logback-classic"        % Version.logback
  val newType              = "io.estatico"                   %% "newtype"                % Version.newType
  val refinedCore          = "eu.timepit"                    %% "refined"                % Version.refined
  val refinedCats          = "eu.timepit"                    %% "refined-cats"           % Version.refined
  val akkaActor            = "com.typesafe.akka"             %% "akka-actor-typed"       % Version.akka
  val akkaStream           = "com.typesafe.akka"             %% "akka-stream"            % Version.akka
  val akkaHttp             = "com.typesafe.akka"             %% "akka-http"              % Version.akkaHttp
  val akkaTapir            = "com.softwaremill.sttp.tapir"   %% "tapir-akka-http-server" % Version.tapir
  val derevoCatsTagless    = "tf.tofu"                       %% "derevo-cats-tagless"    % Version.derevo
  val ciris                = "is.cir"                        %% "ciris"                  % "2.4.0"
  val cirisRefined         = "is.cir"                        %% "ciris-refined"          % "2.4.0"
}

lazy val commonDependencies = Seq(
  dependencies.catsCore,
  dependencies.newType,
  dependencies.derevoCats,
  dependencies.derevoCirce,
  dependencies.refinedCore,
  dependencies.refinedCats,
  dependencies.circeGeneric,
  dependencies.circeRefined,
  dependencies.derevoCatsTagless
)

lazy val global = project
  .in(file("."))
  .settings(settings)
  .aggregate(
    common,
    api,
    service,
    routerDefinition,
    router,
    http4sApp,
    akkaApp
  )

lazy val common = project.settings(
  name := "core",
  settings,
  libraryDependencies ++= commonDependencies
)

lazy val config = project.settings(
  name := "config",
  settings,
  libraryDependencies ++= Seq(
    dependencies.catsCore,
    dependencies.ciris,
    dependencies.cirisRefined
  )
)

lazy val api = project
  .settings(
    name := "api",
    settings,
    libraryDependencies ++= commonDependencies
  )
  .dependsOn(common)

lazy val service = project
  .settings(
    name := "service",
    settings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.catsEffect
    )
  )
  .dependsOn(common, api)

lazy val routerDefinition = project
  .settings(
    name := "routerDefinition",
    settings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.circeCore,
      dependencies.circeGeneric,
      dependencies.circeRefined,
      dependencies.tapirCats,
      dependencies.tapirCirce,
      dependencies.tapirCore,
      dependencies.tapirDerevo,
      dependencies.tapirRefined,
      dependencies.tapirNewType,
      dependencies.derevoCirce
    )
  )
  .dependsOn(common)

lazy val router = project
  .settings(
    name := "router",
    settings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.circeCore,
      dependencies.circeGeneric,
      dependencies.circeRefined,
      dependencies.tapirCats,
      dependencies.tapirCirce,
      dependencies.tapirCore,
      dependencies.tapirDerevo,
      dependencies.tapirRefined,
      dependencies.tapirNewType,
      dependencies.derevoCirce,
      dependencies.log4Cats,
      dependencies.logback
    )
  )
  .dependsOn(common, api, routerDefinition)

lazy val http4sApp = project
  .settings(
    name := "http4sApp",
    settings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.http4sCirce,
      dependencies.http4sDsl,
      dependencies.http4sEmberServer,
      dependencies.sttpApiSpecCirceYaml,
      dependencies.tapirCore,
      dependencies.tapirHttp4s,
      dependencies.tapirOpenApiDocs,
      dependencies.tapirSwaggerUi,
      dependencies.log4Cats,
      dependencies.logback
    )
  )
  .dependsOn(common, api, router, routerDefinition, service, config)

lazy val akkaApp = project
  .settings(
    name := "akkaApp",
    settings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.sttpApiSpecCirceYaml,
      dependencies.tapirCore,
      dependencies.tapirOpenApiDocs,
      dependencies.tapirSwaggerUi,
      dependencies.akkaTapir,
      dependencies.akkaHttp,
      dependencies.akkaActor,
      dependencies.akkaStream
    )
  )
  .dependsOn(common, api, router, routerDefinition, service, config)

lazy val settings = Seq(
  scalaVersion := "2.13.6",
  scalacOptions ++= Seq(
    "-Ymacro-annotations"
  ),
  Compile / compile / wartremoverWarnings ++= Warts.unsafe.filterNot(
    _ == Wart.Any
  ), // Disable the "Any" wart due to too many false positives.
  Test / console / scalacOptions --= Seq(
    "-Xfatal-warnings",
    "-Ywarn-unused-import",
    "-Ywarn-unused:implicits",
    "-Ywarn-unused:imports",
    "-Ywarn-unused:locals",
    "-Ywarn-unused:params",
    "-Ywarn-unused:patvars",
    "-Ywarn-unused:privates"
  ),
  libraryDependencies ++= (
    if (scalaVersion.value.startsWith("2")) {
      Seq(
        compilerPlugin("com.olegpy"   %% "better-monadic-for" % "0.3.1"),
        compilerPlugin("org.typelevel" % "kind-projector"     % "0.13.2" cross CrossVersion.full)
      )
    } else {
      Seq()
    }
  ),
  scalafmtOnCompile := true
)
