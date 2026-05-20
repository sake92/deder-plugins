package ba.sake.deder.protobuf.protoc

import ba.sake.deder.{ServerNotification, ServerNotificationsLogger}

object ProtocRunner {

  def run(
      request: ProtocInvocationRequest,
      env: Map[String, String],
      cwd: os.Path,
      notifications: ServerNotificationsLogger
  ): Unit = {
    val command = ProtocCommandBuilder.build(request)
    notifications.add(ServerNotification.logInfo(s"Running protoc: ${command.mkString(" ")}"))
    val result = os.proc(command).call(cwd = cwd, env = env, stdout = os.Pipe, stderr = os.Pipe, check = false)
    val stdout = result.out.text().trim
    if stdout.nonEmpty then stdout.linesIterator.foreach(line => notifications.add(ServerNotification.logInfo(line)))
    val stderr = result.err.text().trim
    if stderr.nonEmpty then stderr.linesIterator.foreach(line => notifications.add(ServerNotification.logWarning(line)))
    if result.exitCode != 0 then
      throw RuntimeException(s"protoc failed with exit code ${result.exitCode}")
  }
}
