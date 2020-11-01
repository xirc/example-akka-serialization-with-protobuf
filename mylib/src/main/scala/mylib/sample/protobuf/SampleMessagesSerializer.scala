package mylib.sample.protobuf

import java.io.NotSerializableException

import akka.actor.ExtendedActorSystem
import akka.serialization.{
  BaseSerializer,
  SerializationExtension,
  SerializerWithStringManifest,
  Serializers,
}
import akka.util.ByteIterator.ByteArrayIterator
import com.google.protobuf.ByteString
import mylib.sample.SampleMessages
import mylib.sample.SampleMessages.{MessageWithAny, MessageWithPrimitive}

final class SampleMessagesSerializer(val system: ExtendedActorSystem)
    extends SerializerWithStringManifest
    with BaseSerializer {

  private lazy val serialization = SerializationExtension(system)

  private val MessageWithPrimitiveManifest = "A"
  private val MessageWithAnyManifest = "B"
  private val MessageWithTypedManifest = "C"

  private val fromBinaryMap = Map[String, Array[Byte] => SampleMessages](
    MessageWithPrimitiveManifest -> messageWithPrimitiveFromBinary,
    MessageWithAnyManifest -> messageWithAnyFromBinary,
  )

  override def manifest(o: AnyRef): String = o match {
    case m: SampleMessages => sampleMessageManifest(m)
    case _ =>
      throw new IllegalArgumentException(
        s"Can't serialize object of type ${o.getClass} in [${getClass.getName}]",
      )
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case m: SampleMessages => sampleMessageToBinary(m)
    case _ =>
      throw new IllegalArgumentException(
        s"Can't serialize object of type ${o.getClass} in [${getClass.getName}]",
      )
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
    fromBinaryMap.get(manifest) match {
      case Some(fromBinary) =>
        fromBinary(bytes)
      case None =>
        throw new NotSerializableException(
          s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]",
        )
    }
  }

  private def sampleMessageManifest(o: SampleMessages): String = o match {
    case _: MessageWithPrimitive => MessageWithPrimitiveManifest
    case _: MessageWithAny       => MessageWithAnyManifest
  }

  private def sampleMessageToBinary(message: SampleMessages): Array[Byte] = {
    message match {
      case m: MessageWithPrimitive => messageWithPrimitiveToProto(m).toByteArray
      case m: MessageWithAny       => messageWithAnyToProto(m).toByteArray
    }
  }

  private def messageWithPrimitiveToProto(
      message: MessageWithPrimitive,
  ): msg.MessageWithPrimitive = {
    msg.MessageWithPrimitive(message.id, message.message)
  }

  private def messageWithPrimitiveFromBinary(bytes: Array[Byte]): MessageWithPrimitive = {
    val proto = msg.MessageWithPrimitive.parseFrom(bytes)
    MessageWithPrimitive(proto.id, proto.message)
  }

  private def messageWithAnyToProto(message: MessageWithAny): msg.MessageWithAny = {
    val payload = toPayload(message.message)
    msg.MessageWithAny(payload = Some(payload))
  }

  private def messageWithAnyFromBinary(bytes: Array[Byte]): MessageWithAny = {
    val proto = msg.MessageWithAny.parseFrom(bytes)
    val payload = proto.payload.getOrElse(msg.Payload.defaultInstance)
    MessageWithAny(fromPayload(payload))
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
