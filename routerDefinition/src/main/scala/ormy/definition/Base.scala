package ormy.definition

import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.codec.newtype._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{Endpoint, endpoint, statusCode}

object Base {
  val baseErrorEndpoint: Endpoint[Unit, Unit, (StatusCode, ErrorResponse), Unit, Any] =
    endpoint.errorOut(statusCode and jsonBody[ErrorResponse])
}
