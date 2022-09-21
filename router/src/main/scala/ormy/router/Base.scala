package ormy.router

import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{Endpoint, endpoint, statusCode}
import sttp.tapir.generic.auto._

object Base {
  val baseErrorEndpoint: Endpoint[Unit, Unit, (StatusCode, ErrorResponse), Unit, Any] =
    endpoint.errorOut(statusCode and jsonBody[ErrorResponse])
}
