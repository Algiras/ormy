package ormy

import derevo.cats.show
import derevo.circe.{decoder, encoder}
import derevo.derive
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.numeric.NonNegative
import io.circe.refined._
import io.estatico.newtype.macros.newtype
import java.util.UUID

package object domain {
  @derive(encoder, decoder, show)
  @newtype
  case class AccountId(value: UUID)

  type Money = Double Refined NonNegative

  @derive(encoder, decoder, show)
  object Money extends RefinedTypeOps[Money, Double]

  @derive(encoder, decoder)
  @newtype
  case class Balance(value: Money) {
    def amount: Double = value.value
  }

  @derive(encoder, decoder)
  @newtype
  case class Amount(value: Money) {
    def amount: Double = value.value
  }
}
