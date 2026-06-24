package consumer

import consumer.generated.GeneratedMessage

@main def main(): Unit =
  val msg = GeneratedMessage("Hello from sourcegen!")
  println(msg.text)
