package ormy.router

import cats.effect.Async
import ormy.router.Base.baseErrorEndpoint
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.codec.newtype._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import ormy.api.Bank
import sttp.tapir.codec.refined._
import Operations._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import cats.implicits._
import ormy.domain.Balance
import ormy.router.Response.BalanceReturned
import sttp.model.StatusCode
import org.typelevel.log4cats.Logger

class Operations[F[_]](bank: Bank[F], logger: Logger[F])(implicit F: Async[F]) {
  def toError(statusCode: StatusCode, message: String) = F.pure(Left((statusCode, ErrorResponse(message))))

  val transferRoute = transfer.serverLogic[F](input =>
    for {
      res <- bank.transfer(input.from, input.to, input.amount)
      response <- res match {
        case Left(error) =>
          error match {
            case Bank.ConcurrentUpdate => toError(StatusCode.PreconditionFailed, "Concurrent update of record")
            case Bank.InsufficientBalance(account) =>
              toError(StatusCode.Conflict, s"Insufficient balance in account of ${account.value}]")
            case Bank.AccountDoesNotExist(account) => toError(StatusCode.NotFound, s"Account ${account.value} is not found")
            case Bank.InvalidSelfTransfer(account) =>
              toError(StatusCode.BadRequest, s"Self transfer to Account ${account.value} is impossible")
            case Bank.Unexpected(error) =>
              logger.error(error.getMessage) *> toError(StatusCode.InternalServerError, "System failure")
          }
        case Right(value) =>
          F.pure(
            Right(
              Response.BalanceUpdated(
                BalanceReturned(value.subsidiary.id, Balance(value.subsidiary.balance.value)),
                BalanceReturned(value.recipient.id, Balance(value.recipient.balance.value))
              )
            )
          )
      }
    } yield response
  )

  val routes = Http4sServerInterpreter[F]().toRoutes(transferRoute)
}

object Operations {
  val transfer = baseErrorEndpoint.post
    .in("operations")
    .in(jsonBody[Request.Transfer])
    .out(jsonBody[Response.BalanceUpdated])
    .description("Transfer Money")
}
