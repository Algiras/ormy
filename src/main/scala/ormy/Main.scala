package ormy

import cats.effect.{IO, IOApp}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.{CORS, CORSPolicy}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import ormy.router.{Account, Operations}
import ormy.service.InMemoryBank
import sttp.apispec.openapi.circe.yaml._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI
import cats.implicits._

object Main extends IOApp.Simple {
  val corsPolicy: CORSPolicy = CORS.policy

  val name    = "Ormy"
  val version = "0.0.1"

  override def run: IO[Unit] = for {
    log       <- Slf4jLogger.create[IO]
    bankState <- InMemoryBank[IO]
    (exampleAccount, bank) = bankState
    accountService         = new Account[IO](bank)
    operationService       = new Operations[IO](bank, log)
    _ <- log.info(s"Created sample account with id $exampleAccount")
    docs = OpenAPIDocsInterpreter().toOpenAPI(
      List(Account.createAccount, Account.getBalance, Operations.transfer),
      name,
      version
    )
    swaggerRoutes = Http4sServerInterpreter[IO]().toRoutes(
      SwaggerUI[IO](docs.toYaml)
    )
    httpApp = Router(
      "/" -> (swaggerRoutes <+> accountService.routes <+> operationService.routes)
    ).orNotFound
    _ <- EmberServerBuilder
      .default[IO]
      .withHttpApp(corsPolicy(httpApp))
      .build
      .use(server => log.info(s"$name v$version started on ${server.address}") *> IO.never[Unit])
  } yield ()
}
