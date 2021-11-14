package myapp.echo

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, onComplete, path}
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import mylib.echo.Echo

import scala.concurrent.duration.DurationInt

object Main extends App {

  object Guardian {
    def apply(): Behavior[Echo.Ping[String]] = {
      Echo()
    }
  }

  implicit val timeout: Timeout = 3.seconds
  implicit val system: ActorSystem[Echo.Ping[String]] = ActorSystem(Guardian(), "system")

  val route: Route = {
    path(Segment) { msg =>
      val pong = system.ask[Echo.Pong[String]](Echo.Ping(msg, _))
      onComplete(pong) { p =>
        complete(StatusCodes.OK -> p.toString)
      }
    }
  }
  Http().newServerAt("127.0.0.1", 8080).bind(route)
}
