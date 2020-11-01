package mylib.echo.protobuf

import akka.serialization.SerializationExtension
import mylib.SpecBase
import mylib.echo.{Echo, EchoMessages}

final class EchoMessagesSerializerBindingSpec extends SpecBase {

  private val serialization = SerializationExtension(system)

  private def checkSerializer(message: EchoMessages): Unit = {
    val serializer = serialization.findSerializerFor(message)
    serializer shouldBe a[EchoMessagesSerializer]
  }

  private val probe1 = testKit.createTestProbe[Echo.Pong[Int]]
  private val probe2 = testKit.createTestProbe[Echo.Pong[String]]

  "SampleMessagesSerializer" should {
    "be bound to SampleMessages" in {
      checkSerializer(Echo.Ping(123, probe1.ref))
      checkSerializer(Echo.Ping("hello world", probe2.ref))
      checkSerializer(Echo.Pong(456))
      checkSerializer(Echo.Pong("goodbye"))
    }
  }

}
