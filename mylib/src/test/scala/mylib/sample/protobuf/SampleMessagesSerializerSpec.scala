package mylib.sample.protobuf

import akka.actor.ExtendedActorSystem
import mylib.SpecBase
import mylib.sample.SampleMessages.{MessageWithAny, MessageWithPrimitive}

final class SampleMessagesSerializerSpec extends SpecBase {

  private val serializer = new SampleMessagesSerializer(
    system.classicSystem.asInstanceOf[ExtendedActorSystem],
  )

  private def checkSerialization(obj: AnyRef): Unit = {
    val blob = serializer.toBinary(obj)
    val ref = serializer.fromBinary(blob, serializer.manifest(obj))
    ref should ===(obj)
  }

  "SampleMessages" should {
    "be serializable" in {
      checkSerialization(MessageWithPrimitive(123, "hello"))
      checkSerialization(MessageWithAny(456))
      checkSerialization(MessageWithAny("goodbye"))
    }
  }

}
