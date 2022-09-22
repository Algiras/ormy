package ormy.definition

import io.circe.generic.auto._
import ormy.definition.Base.baseErrorEndpoint
import ormy.definition.Response.BalanceReturned
import ormy.domain.AccountId
import sttp.model.StatusCode
import sttp.tapir.codec.newtype._
import sttp.tapir.codec.refined._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{Endpoint, path}

object Account {
  private val accountEndpoint = baseErrorEndpoint.in("account")

  val createAccount: Endpoint[Unit, Unit, (StatusCode, ErrorResponse), Response.AccountCreated, Any] = accountEndpoint.post
    .out(jsonBody[Response.AccountCreated])
    .description("Create Account")

  val getBalance: Endpoint[Unit, AccountId, (StatusCode, ErrorResponse), BalanceReturned, Any] = accountEndpoint.get
    .in(path[AccountId]("accountId"))
    .out(jsonBody[BalanceReturned])
    .description("Get Account Balance")
}
