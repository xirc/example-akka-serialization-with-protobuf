package mylib.sample

sealed trait SampleMessages

object SampleMessages {
  case class MessageWithPrimitive(id: Long, message: String) extends SampleMessages
  case class MessageWithAny(message: Any) extends SampleMessages
}
