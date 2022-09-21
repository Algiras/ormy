package ormy.router

import cats.effect.Async
import cats.implicits._
import io.circe.generic.auto._
import ormy.api.Bank
import ormy.domain.AccountId
import ormy.router.Account.{createAccount, getBalance}
import ormy.router.Base.baseErrorEndpoint
import ormy.router.Response.BalanceReturned
import sttp.model.StatusCode
import sttp.tapir.codec.newtype._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.path

class Account[F[_]: Async](bank: Bank[F]) {
  val createAccountRoute = createAccount.serverLogicSuccess[F]((_: Unit) =>
    for {
      id <- bank.openAccount
    } yield Response.AccountCreated(id)
  )

  val getBalanceRoute = getBalance.serverLogic[F](id =>
    for {
      result <- bank.getBalance(id)
    } yield result match {
      case Some(balance) => Right(Response.BalanceReturned(id, balance))
      case None          => Left((StatusCode.NotFound, ErrorResponse(s"Account ${id.show} not found")))
    }
  )
}

object Account {
  private val accountEndpoint = baseErrorEndpoint.in("account")

  val createAccount = accountEndpoint.post
    .out(jsonBody[Response.AccountCreated])
    .description("Create Account")

  val getBalance = accountEndpoint.get
    .in(path[AccountId]("accountId"))
    .out(jsonBody[BalanceReturned])
    .description("Get Account Balance")
}
