package mylib.sample.protobuf

import akka.serialization.SerializationExtension
import mylib.SpecBase
import mylib.sample.SampleMessages
import mylib.sample.SampleMessages.{MessageWithAny, MessageWithPrimitive}

final class SampleMessagesSerializerBindingSpec extends SpecBase {

  private val serialization = SerializationExtension(system)

  private def checkSerializer(message: SampleMessages): Unit = {
    val serializer = serialization.findSerializerFor(message)
    serializer shouldBe a[SampleMessagesSerializer]
  }

  "SampleMessagesSerializer" should {
    "be bound to SampleMessages" in {
      checkSerializer(MessageWithPrimitive(123, "hello"))
      checkSerializer(MessageWithAny(456))
      checkSerializer(MessageWithAny("goodbye"))
    }
  }

}
