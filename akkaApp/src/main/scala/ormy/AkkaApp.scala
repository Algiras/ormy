package ormy

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import cats.tagless.FunctorK
import cats.~>
import org.typelevel.log4cats.slf4j.Slf4jLogger
import ormy.api.Bank
import ormy.router.{Account, Operations}
import ormy.service.InMemoryBank
import sttp.apispec.openapi.circe.yaml._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.SwaggerUI
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AkkaApp extends IOApp {
  val name    = "Ormy"
  val version = "0.0.1"

  val ioToFuture: ~>[IO, Future] = new ~>[IO, Future] {
    override def apply[A](fa: IO[A]): Future[A] = fa.unsafeToFuture()(runtime)
  }

  override def run(args: List[String]): IO[ExitCode] = for {
    log                    <- Slf4jLogger.create[IO]
    (exampleAccount, bank) <- InMemoryBank[IO]
    futureBank       = FunctorK[Bank].mapK[IO, Future](bank)(ioToFuture)
    futureLogger     = log.mapK(ioToFuture)
    accountService   = new Account[Future](futureBank)
    operationService = new Operations[Future](futureBank, futureLogger)
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
    interpreter: AkkaHttpServerInterpreter = AkkaHttpServerInterpreter()
    httpApp: Route = concat(
      interpreter.toRoute(SwaggerUI[Future](docs.toYaml)),
      interpreter.toRoute(accountService.createAccountRoute),
      interpreter.toRoute(accountService.getBalanceRoute),
      interpreter.toRoute(operationService.transferRoute)
    )
    system = ActorSystem()
    server <- IO.fromFuture(IO {
      implicit val ctx: ActorSystem = system
      Http()(system).newServerAt("localhost", 8080).bindFlow(httpApp)
    })
    _ <- log.info(s"$name v$version started on ${server.localAddress.getAddress.toString}")
    _ <- IO.readLine
    _ <- IO.fromFuture(IO(server.unbind()))
    _ <- IO.fromFuture(IO(system.terminate()))
  } yield ExitCode.Success
}
