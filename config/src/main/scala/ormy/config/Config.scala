package ormy.config

import cats.implicits._
import ciris._
import ciris.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.string.IPv4
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString

case class App(name: NonEmptyString, version: NonEmptyString)
case class Server(host: String Refined IPv4, port: UserPortNumber)
case class Config(app: App, server: Server)

object Config {
  private val appConfig = (
    env("APP_NAME").as[NonEmptyString].default("localhost"),
    env("APP_VERSION").as[NonEmptyString]
  ).parMapN(App)

  private val serverConfig = (
    env("HOST").as[String Refined IPv4],
    env("PORT").as[UserPortNumber]
  ).parMapN(Server)

  val config = (appConfig, serverConfig).parMapN((app, server) => new Config(app, server))
}
