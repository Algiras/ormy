package ormy.router

import derevo.circe.{decoder, encoder}
import derevo.derive

@derive(encoder, decoder)
case class ErrorResponse(message: String)
