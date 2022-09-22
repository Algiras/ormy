package ormy.router

import cats.Monad
import cats.implicits._
import org.typelevel.log4cats.Logger
import ormy.api.Bank
import ormy.definition.Operations._
import ormy.definition.Response.BalanceReturned
import ormy.definition.{ErrorResponse, Request, Response}
import ormy.domain.Balance
import sttp.model.StatusCode
import sttp.tapir.server.ServerEndpoint.Full

class Operations[F[_]](bank: Bank[F], logger: Logger[F])(implicit F: Monad[F]) {
  private def toError(statusCode: StatusCode, message: String): F[Either[(StatusCode, ErrorResponse), Response.BalanceUpdated]] =
    F.pure(Left((statusCode, ErrorResponse(message))))

  val transferRoute: Full[Unit, Unit, Request.Transfer, (StatusCode, ErrorResponse), Response.BalanceUpdated, Any, F] =
    transfer.serverLogic[F](input =>
      bank.transfer(input.from, input.to, input.amount).flatMap {
        case Left(error) =>
          error match {
            case Bank.ConcurrentUpdate => toError(StatusCode.PreconditionFailed, "Concurrent update of record")
            case Bank.InsufficientBalance(account) =>
              toError(StatusCode.Conflict, s"Insufficient balance in account of ${account.show}]")
            case Bank.AccountDoesNotExist(account) => toError(StatusCode.NotFound, s"Account ${account.show} is not found")
            case Bank.InvalidSelfTransfer(account) =>
              toError(StatusCode.BadRequest, s"Self transfer to Account ${account.show} is impossible")
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
    )
}
