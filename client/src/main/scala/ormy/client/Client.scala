package ormy.client

import cats.MonadThrow
import ormy.definition.Request.Transfer
import ormy.definition.{ErrorResponse, Response}
import ormy.domain.{AccountId, Amount, Balance}
import sttp.client3.SttpBackend
import sttp.model.{StatusCode, Uri}
import sttp.tapir.{DecodeResult, PublicEndpoint}
import sttp.tapir.client.sttp.SttpClientInterpreter
import ormy.definition._
import ormy.definition.Response.BalanceUpdated
import ormy.client.Client.ClientError._
import scala.util.control.NoStackTrace
import cats.implicits._

trait Client[F[_]] {
  def createAccount: F[AccountId]
  def getBalance(id: AccountId): F[Balance]
  def transfer(from: AccountId, to: AccountId, amount: Amount): F[BalanceUpdated]
}

object Client {
  sealed trait ClientError extends NoStackTrace

  object ClientError {
    case object DecodeError                                       extends ClientError
    case class ResponseError(status: StatusCode, message: String) extends ClientError
  }

  def make[F[_], R](baseUri: Uri, backend: SttpBackend[F, R])(implicit F: MonadThrow[F]): Client[F] = new Client[F] {
    private val interpreter = SttpClientInterpreter()
    private def partialRequest[I, E, O](request: PublicEndpoint[I, E, O, R]) =
      interpreter.toClient(request, Some(baseUri), backend)

    private def handleResponse[T](from: DecodeResult[Either[(StatusCode, ErrorResponse), T]]): F[T] =
      from match {
        case _: DecodeResult.Failure => F.raiseError(DecodeError)
        case DecodeResult.Value(result) =>
          result match {
            case Left((status, error)) => F.raiseError(ResponseError(status, error.message))
            case Right(value)          => F.pure(value)
          }
      }

    override def createAccount: F[AccountId] = partialRequest(Account.createAccount)(())
      .flatMap(handleResponse)
      .map(_.id)

    override def getBalance(id: AccountId): F[Balance] = partialRequest(Account.getBalance)(id)
      .flatMap(handleResponse)
      .map(_.amount)

    def transfer(from: AccountId, to: AccountId, amount: Amount): F[Response.BalanceUpdated] =
      partialRequest(Operations.transfer)(Transfer(from, to, amount)).flatMap(handleResponse)
  }
}
