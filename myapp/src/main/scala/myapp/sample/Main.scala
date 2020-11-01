package myapp.sample

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, path}
import akka.http.scaladsl.server.Route
import mylib.sample.SampleMessages

import scala.util.Random

object Main extends App {

  object Guardian {
    def apply(): Behavior[SampleMessages] = {
      Behaviors.logMessages(Behaviors.ignore[SampleMessages])
    }
  }

  implicit val system: ActorSystem[SampleMessages] = ActorSystem(Guardian(), "system")

  val route: Route = {
    path("hello") {
      val msg = Random.nextInt(2) match {
        case 0 =>
          SampleMessages.MessageWithPrimitive(Random.nextInt(), "hello world")
        case 1 =>
          SampleMessages.MessageWithAny(Random.nextInt())
      }
      system ! msg
      complete(StatusCodes.OK)
    }
  }
  Http().newServerAt("127.0.0.1", 8080).bind(route)
}
