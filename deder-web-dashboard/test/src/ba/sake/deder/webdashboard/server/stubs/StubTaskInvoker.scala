package ba.sake.deder.webdashboard.server.stubs

import ba.sake.deder.*

class StubTaskInvoker(
    val invokeFn: (String, Seq[String], Seq[String], ServerNotification => Unit) => TaskInvokeResult
) extends TaskInvokerApi:

  def invoke(
      taskName: String,
      moduleIds: Seq[String],
      args: Seq[String],
      onNotification: ServerNotification => Unit
  ): TaskInvokeResult = invokeFn(taskName, moduleIds, args, onNotification)
