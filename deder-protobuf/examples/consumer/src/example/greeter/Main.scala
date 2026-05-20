package example.greeter

object Main {
  def main(args: Array[String]): Unit = {
    val request = HelloRequest.newBuilder().setName("Deder").build()
    val descriptorName = GreeterGrpc.getServiceDescriptor.getName
    println(s"${request.getName} -> ${descriptorName}")
  }
}
