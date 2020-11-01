package mylib

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

abstract class SpecBase extends ScalaTestWithActorTestKit with AnyWordSpecLike with Matchers
