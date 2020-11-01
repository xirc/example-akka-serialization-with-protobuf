package mylib.echo

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

private[echo] sealed trait EchoMessages

object Echo {
  case class Ping[T](message: T, replyTo: ActorRef[Pong[T]]) extends EchoMessages
  case class Pong[T](message: T) extends EchoMessages

  def apply[T](): Behavior[Ping[T]] = {
    Behaviors.receiveMessage { ping =>
      ping.replyTo ! Pong(ping.message)
      Behaviors.same
    }
  }
}
