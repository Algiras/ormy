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

object Http4sApp extends IOApp.Simple {
  val corsPolicy: CORSPolicy                           = CORS.policy
  val name                                             = "Ormy"
  val version                                          = "0.0.1"
  private val interpreter: Http4sServerInterpreter[IO] = Http4sServerInterpreter[IO]()

  override def run: IO[Unit] = for {
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
      name,
      version
    )

    httpApp = (
      interpreter.toRoutes(SwaggerUI[IO](docs.toYaml)) <+>
        interpreter.toRoutes(accountService.createAccountRoute) <+>
        interpreter.toRoutes(accountService.getBalanceRoute) <+>
        interpreter.toRoutes(operationService.transferRoute)
    ).orNotFound
    _ <- EmberServerBuilder
      .default[IO]
      .withHttpApp(corsPolicy(httpApp))
      .build
      .use(server => log.info(s"$name v$version started on ${server.address.getHostString}") *> IO.readLine)
  } yield ()
}
