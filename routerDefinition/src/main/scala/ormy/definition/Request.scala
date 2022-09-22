package ormy.definition

import derevo.circe.{decoder, encoder}
import derevo.derive
import ormy.domain.{AccountId, Amount}

case object Request {
  @derive(encoder, decoder)
  case class Transfer(from: AccountId, to: AccountId, amount: Amount)
}
