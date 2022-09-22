package ormy

import cats.effect.{IO, IOApp}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.{CORS, CORSPolicy}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import ormy.router.{Account, Operations}
import ormy.service.InMemoryBank
import sttp.apispec.openapi.circe.yaml._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI
import cats.implicits._
import com.comcast.ip4s.{Host, Port}
import ormy.config.Config

object Http4sApp extends IOApp.Simple {
  val corsPolicy: CORSPolicy                           = CORS.policy
  private val interpreter: Http4sServerInterpreter[IO] = Http4sServerInterpreter[IO]()

  override def run: IO[Unit] = for {
    Config(app, server)    <- Config.config.load[IO]
    log                    <- Slf4jLogger.create[IO]
    (exampleAccount, bank) <- InMemoryBank[IO]
    accountService   = new Account[IO](bank)
    operationService = new Operations[IO](bank, log)
    _ <- log.info(s"Created sample account with id ${exampleAccount.show}")
    docs = OpenAPIDocsInterpreter().toOpenAPI(
      List(
        definition.Account.createAccount,
        definition.Account.getBalance,
        definition.Operations.transfer
      ),
      app.name.value,
      app.version.value
    )

    httpApp = (
      interpreter.toRoutes(SwaggerUI[IO](docs.toYaml)) <+>
        interpreter.toRoutes(accountService.createAccountRoute) <+>
        interpreter.toRoutes(accountService.getBalanceRoute) <+>
        interpreter.toRoutes(operationService.transferRoute)
    ).orNotFound
    port <- IO.fromOption(Port.fromInt(server.port.value))(new RuntimeException("Invalid Port"))
    _ <- EmberServerBuilder
      .default[IO]
      .withHostOption(Host.fromString(server.host.value))
      .withPort(port)
      .withHttpApp(corsPolicy(httpApp))
      .build
      .use(server => log.info(s"${app.name.value} v${app.version.value} started on ${server.address.getHostString}") *> IO.readLine)
  } yield ()
}
