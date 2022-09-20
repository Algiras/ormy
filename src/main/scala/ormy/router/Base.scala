package ormy.router

import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import sttp.tapir.{endpoint, statusCode}

object Base {
  val baseErrorEndpoint = endpoint.errorOut(statusCode and jsonBody[ErrorResponse])
}
