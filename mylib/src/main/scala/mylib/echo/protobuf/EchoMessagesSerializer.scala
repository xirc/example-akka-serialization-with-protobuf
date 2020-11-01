package mylib.echo.protobuf

import java.io.NotSerializableException

import akka.actor.ExtendedActorSystem
import akka.actor.typed.ActorRefResolver
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.serialization.{
  BaseSerializer,
  SerializationExtension,
  SerializerWithStringManifest,
  Serializers,
}
import com.google.protobuf.ByteString
import mylib.echo.Echo.{Ping, Pong}
import mylib.echo.EchoMessages
import mylib.echo.protobuf.msg.Payload

final class EchoMessagesSerializer(val system: ExtendedActorSystem)
    extends SerializerWithStringManifest
    with BaseSerializer {

  private lazy val serialization = SerializationExtension(system)
  private lazy val actorRefResolver = ActorRefResolver(system.toTyped)

  private val PingManifest = "A"
  private val PongManifest = "B"

  private val fromBinaryMap = Map[String, Array[Byte] => EchoMessages](
    PingManifest -> pingFromBinary,
    PongManifest -> pongFromBinary,
  )

  override def manifest(o: AnyRef): String = o match {
    case m: EchoMessages => echoManifest(m)
    case _ =>
      throw new IllegalArgumentException(
        s"Can't serialize object of type ${o.getClass} in [${getClass.getName}]",
      )
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case m: EchoMessages => echoMessageToBinary(m)
    case _ =>
      throw new IllegalArgumentException(
        s"Can't serialize object of type ${o.getClass} in [${getClass.getName}]",
      )
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
    fromBinaryMap.get(manifest) match {
      case Some(fromBinary) => fromBinary(bytes)
      case None =>
        throw new NotSerializableException(
          s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]",
        )
    }
  }

  private def echoManifest(message: EchoMessages): String = message match {
    case _: Ping[_] => PingManifest
    case _: Pong[_] => PongManifest
  }

  private def echoMessageToBinary(message: EchoMessages): Array[Byte] = message match {
    case m: Ping[_] => pingToProto(m).toByteArray
    case m: Pong[_] => pongToProto(m).toByteArray
  }

  private def pingToProto(message: Ping[_]): msg.Ping = {
    val payload = toPayload(message.message)
    val replyTo = actorRefResolver.toSerializationFormat(message.replyTo)
    msg.Ping(payload = Some(payload), replyTo = replyTo)
  }

  private def pingFromBinary(bytes: Array[Byte]): Ping[_] = {
    val proto = msg.Ping.parseFrom(bytes)
    val payload = proto.payload.getOrElse(Payload.defaultInstance)
    val replyTo = actorRefResolver.resolveActorRef[Pong[_]](proto.replyTo)
    Ping(fromPayload(payload), replyTo)
  }

  private def pongToProto(message: Pong[_]): msg.Pong = {
    val payload = toPayload(message.message)
    msg.Pong(payload = Some(payload))
  }

  private def pongFromBinary(bytes: Array[Byte]): Pong[_] = {
    val proto = msg.Pong.parseFrom(bytes)
    val payload = proto.payload.getOrElse(Payload.defaultInstance)
    Pong(fromPayload(payload))
  }

  private def toPayload(message: Any): msg.Payload = {
    val m = message.asInstanceOf[AnyRef]
    val messageSerializer = serialization.findSerializerFor(m)
    val messagePayload = messageSerializer.toBinary(m)
    val messageManifest = Serializers.manifestFor(messageSerializer, m)

    msg.Payload(
      serializerId = messageSerializer.identifier,
      manifest = messageManifest,
      message = ByteString.copyFrom(messagePayload),
    )
  }

  private def fromPayload(payload: msg.Payload): AnyRef = {
    serialization
      .deserialize(
        payload.message.toByteArray,
        payload.serializerId,
        payload.manifest,
      )
      .get
  }

}
