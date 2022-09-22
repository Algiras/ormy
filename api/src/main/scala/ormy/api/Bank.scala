package ormy.api

import cats.tagless.FunctorK
import cats.~>
import ormy.api.Bank.{BankError, LatestBalance}
import ormy.domain.{AccountId, Amount, Balance}

trait Bank[F[_]] {
  def openAccount: F[AccountId]
  def getBalance(account: AccountId): F[Option[Balance]]
  def transfer(from: AccountId, to: AccountId, amount: Amount): F[Either[BankError, LatestBalance]]
}

object Bank {
  implicit val bankFunctorK: FunctorK[Bank] = new FunctorK[Bank] {
    override def mapK[F[_], G[_]](af: Bank[F])(fk: F ~> G): Bank[G] = new Bank[G] {
      override def openAccount: G[AccountId] = fk(af.openAccount)

      override def getBalance(account: AccountId): G[Option[Balance]] = fk(af.getBalance(account))

      override def transfer(from: AccountId, to: AccountId, amount: Amount): G[Either[BankError, LatestBalance]] = fk(
        af.transfer(from, to, amount)
      )
    }
  }

  case class AccountBalance(id: AccountId, balance: Balance)
  case class LatestBalance(recipient: AccountBalance, subsidiary: AccountBalance)

  sealed trait BankError

  case object ConcurrentUpdate extends BankError

  val concurrentUpdate: BankError = ConcurrentUpdate

  case class InvalidSelfTransfer(accountId: AccountId) extends BankError

  case class InsufficientBalance(account: AccountId) extends BankError

  case class AccountDoesNotExist(account: AccountId) extends BankError

  case class Unexpected(error: Throwable) extends BankError
}
