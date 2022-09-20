package ormy

import derevo.circe.{decoder, encoder}
import derevo.derive
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.numeric.NonNegative
import io.estatico.newtype.macros.newtype
import io.circe.refined._

import java.util.UUID

package object domain {
  @derive(encoder, decoder)
  @newtype
  case class AccountId(value: UUID)

  type Money = Double Refined NonNegative

  object Money extends RefinedTypeOps[Money, Double]

  @derive(encoder, decoder)
  @newtype
  case class Balance(value: Money) {
    def amount = value.value
  }

  @derive(encoder, decoder)
  @newtype
  case class Amount(value: Money) {
    def amount = value.value
  }
}
