package ormy.service

import cats.data.EitherT
import cats.effect.Ref
import cats.effect.std.UUIDGen
import cats.implicits._
import cats.{Functor, Monad}
import ormy.api.Bank
import ormy.api.Bank.{AccountBalance, LatestBalance, concurrentUpdate}
import ormy.domain
import ormy.domain.{AccountId, Amount, Balance, Money}

object InMemoryBank {
  private def genAccountId[F[_]: UUIDGen: Functor]: F[AccountId] = UUIDGen.randomUUID.map(AccountId(_))

  private def unsafeBalance(amount: Double): Balance = Balance(Money.unsafeFrom(amount))

  private def addToBalance(balance: Balance, amount: Amount): Balance = Balance(Money.unsafeFrom(balance.amount + amount.amount))

  private def subtractToBalance(balance: Balance, amount: Amount): Balance = Balance(Money.unsafeFrom(balance.amount - amount.amount))

  def apply[F[_]: Ref.Make: Monad: UUIDGen]: F[(AccountId, Bank[F])] = for {
    id    <- genAccountId
    state <- Ref.of[F, Map[AccountId, Balance]](Map(id -> unsafeBalance(1000000)))
  } yield (
    id,
    new Bank[F] {
      override def openAccount: F[AccountId] = for {
        id <- genAccountId
        _  <- state.update(_ + (id -> unsafeBalance(0)))
      } yield id

      override def getBalance(account: AccountId): F[Option[Balance]] = state.get.map(_.get(account))

      private def accountExists(accountId: AccountId): EitherT[F, Bank.BankError, Balance] = EitherT(
        state.get.map(_.get(accountId).toRight(Bank.AccountDoesNotExist(accountId)))
      )

      private def validBalance(from: AccountId, balance: Balance, amount: Amount): EitherT[F, Bank.BankError, Unit] = EitherT.fromEither[F](
        if ((balance.amount - amount.amount) >= 0)
          Right(())
        else Left(Bank.InsufficientBalance(from))
      )

      private def notSelfTransfer(from: AccountId, to: AccountId): EitherT[F, Bank.InvalidSelfTransfer, Unit] = EitherT.fromEither[F](
        if (from != to) {
          Right(())
        } else Left(Bank.InvalidSelfTransfer(from))
      )

      override def transfer(from: AccountId, to: AccountId, amount: domain.Amount): F[Either[Bank.BankError, Bank.LatestBalance]] = (for {
        balance <- accountExists(from)
        _       <- notSelfTransfer(from, to)
        _       <- accountExists(to)
        _       <- validBalance(from, balance, amount)
        res <- EitherT.liftF(state.tryModify { state =>
          val latestBalance: LatestBalance = LatestBalance(
            recipient = AccountBalance(to, addToBalance(state(to), amount)),
            subsidiary = AccountBalance(from, subtractToBalance(state(from), amount))
          )
          (
            state
              .updated(from, latestBalance.subsidiary.balance)
              .updated(to, latestBalance.recipient.balance),
            latestBalance
          )
        })
        change <- EitherT.fromOption[F](res, concurrentUpdate)
      } yield change).value
    }
  )
}
