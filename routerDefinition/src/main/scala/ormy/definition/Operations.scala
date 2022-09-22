package ormy.definition

import io.circe.generic.auto._
import ormy.definition.Base.baseErrorEndpoint
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.codec.newtype._
import sttp.tapir.codec.refined._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody

object Operations {
  val transfer: Endpoint[Unit, Request.Transfer, (StatusCode, ErrorResponse), Response.BalanceUpdated, Any] =
    baseErrorEndpoint.post
      .in("operations")
      .in("transfer")
      .in(jsonBody[Request.Transfer])
      .out(jsonBody[Response.BalanceUpdated])
      .description("Transfer Money")
}
