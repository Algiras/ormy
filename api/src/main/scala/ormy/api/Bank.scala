package ormy.api

import ormy.api.Bank.{BankError, LatestBalance}
import ormy.domain.{AccountId, Amount, Balance}

trait Bank[F[_]] {
  def openAccount: F[AccountId]
  def getBalance(account: AccountId): F[Option[Balance]]
  def transfer(from: AccountId, to: AccountId, amount: Amount): F[Either[BankError, LatestBalance]]
}

object Bank {
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
