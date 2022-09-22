package ormy.definition

import derevo.circe.{decoder, encoder}
import derevo.derive
import ormy.domain.{AccountId, Balance}

case object Response {
  @derive(encoder, decoder)
  case class AccountCreated(id: AccountId)

  @derive(encoder, decoder)
  case class BalanceReturned(id: AccountId, amount: Balance)

  @derive(encoder, decoder)
  case class BalanceUpdated(subsidiary: BalanceReturned, recipient: BalanceReturned)
}
