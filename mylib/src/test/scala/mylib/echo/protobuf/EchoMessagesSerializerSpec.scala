package mylib.echo.protobuf

import akka.actor.ExtendedActorSystem
import mylib.SpecBase
import mylib.echo.Echo

final class EchoMessagesSerializerSpec extends SpecBase {

  private val serializer = new EchoMessagesSerializer(
    system.classicSystem.asInstanceOf[ExtendedActorSystem],
  )

  private def checkSerialization(obj: AnyRef): Unit = {
    val blob = serializer.toBinary(obj)
    val ref = serializer.fromBinary(blob, serializer.manifest(obj))
    ref should ===(obj)
  }

  private val probe1 = testKit.createTestProbe[Echo.Pong[Int]]()
  private val probe2 = testKit.createTestProbe[Echo.Pong[String]]()

  "EchoMessages" should {
    "be serializable" in {
      checkSerialization(Echo.Ping(123, probe1.ref))
      checkSerialization(Echo.Ping("hello world", probe2.ref))
    }
  }

}
