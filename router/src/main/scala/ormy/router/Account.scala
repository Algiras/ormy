package ormy.router

import cats.Functor
import cats.implicits._
import ormy.api.Bank
import ormy.definition.Account._
import ormy.definition.{ErrorResponse, Response}
import ormy.domain
import sttp.model.StatusCode
import sttp.tapir.server.ServerEndpoint.Full

class Account[F[_]: Functor](bank: Bank[F]) {
  val createAccountRoute: Full[Unit, Unit, Unit, (StatusCode, ErrorResponse), Response.AccountCreated, Any, F] =
    createAccount.serverLogicSuccess[F]((_: Unit) => bank.openAccount.map(Response.AccountCreated(_)))

  val getBalanceRoute: Full[Unit, Unit, domain.AccountId, (StatusCode, ErrorResponse), Response.BalanceReturned, Any, F] =
    getBalance.serverLogic[F](id =>
      bank.getBalance(id).map {
        case Some(balance) => Right(Response.BalanceReturned(id, balance))
        case None          => Left((StatusCode.NotFound, ErrorResponse(s"Account ${id.show} not found")))
      }
    )
}
