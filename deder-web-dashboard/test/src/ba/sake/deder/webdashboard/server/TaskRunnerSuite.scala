package ba.sake.deder.webdashboard.server

import java.time.Instant
import ba.sake.deder.*
import ba.sake.deder.ServerNotification.LogLevel
import munit.FunSuite

class TaskRunnerSuite extends FunSuite {

  private def now: Instant = Instant.now()

  private def stubTaskInvoker(succeed: Boolean, outcomeModuleIds: Seq[String] = Seq("mod1")): TaskInvokerApi =
    new TaskInvokerApi {
      def invoke(
          taskName: String,
          moduleIds: Seq[String],
          args: Seq[String],
          onNotification: ServerNotification => Unit
      ): TaskInvokeResult = {
        onNotification(ServerNotification.Output(s"Running $taskName..."))
        val outcomes = outcomeModuleIds.map { m =>
          TaskInvokeOutcome(m, succeed, if succeed then None else Some("failure"), false)
        }
        onNotification(ServerNotification.Output(s"Done: $taskName"))
        TaskInvokeResult(outcomes, None)
      }
    }

  private def stubInternals(taskNames: Seq[String]): DederProjectInternals =
    new DederProjectInternals {
      def currentRequests: Seq[LiveRequest] = Seq.empty
      def recentHistory: Seq[CompletedRequest] = Seq.empty
      def taskStats(taskName: String): Option[TaskStats] = None
      def allTaskStats: Seq[(String, TaskStats)] = Seq.empty
      def totalRequestsServed: Long = 0L
      def totalErrors: Long = 0L
      def serverUptime: java.time.Duration = java.time.Duration.ZERO
      def inMemoryCachesStats: Map[String, InMemCacheStats] = Map.empty
      def loadedPlugins: Seq[LoadedPluginInfo] = Seq(LoadedPluginInfo("core", taskNames))
      def cancelRequest(requestId: String): Boolean = false
      def requestStatus(requestId: String): Option[RequestStatus] = None
      def allRequestStatuses: Seq[RequestStatus] = Seq.empty
      def purgeInMemoryCaches(): PurgeCachesResult = PurgeCachesResult(0, 0, 0, false)
    }

  test("trigger creates entry with PENDING then transitions to SUCCESS") {
    val log = TaskExecutionLog(10)
    val internals = stubInternals(Seq("compile"))
    val taskInvoker = stubTaskInvoker(succeed = true)
    val runner = TaskRunner(taskInvoker, internals, log, maxConcurrent = 3)

    val entry = runner.trigger("compile", Seq.empty)
    assertEquals(entry.status, ExecStatus.PENDING)
    assertEquals(entry.taskName, "compile")

    Thread.sleep(500)

    val completed = log.get(entry.execId).get
    assertEquals(completed.status, ExecStatus.SUCCESS)
    assert(completed.endTime.isDefined, s"endTime should be set")
    assert(completed.outcomes.nonEmpty, s"outcomes should be non-empty")
    assert(completed.output.contains("Running compile"), s"output should contain log lines")
    assert(completed.output.contains("Done: compile"), s"output should contain completion log")
  }

  test("trigger with failing task marks FAILURE") {
    val log = TaskExecutionLog(10)
    val internals = stubInternals(Seq("test"))
    val taskInvoker = stubTaskInvoker(succeed = false)
    val runner = TaskRunner(taskInvoker, internals, log, maxConcurrent = 3)

    runner.trigger("test", Seq.empty)
    Thread.sleep(500)

    val completed = log.recent(1).head
    assertEquals(completed.status, ExecStatus.FAILURE)
    assertEquals(completed.taskName, "test")
  }

  test("trigger with unknown taskName returns error entry immediately") {
    val log = TaskExecutionLog(10)
    val internals = stubInternals(Seq("compile"))
    val taskInvoker = stubTaskInvoker(succeed = true)
    val runner = TaskRunner(taskInvoker, internals, log, maxConcurrent = 3)

    val entry = runner.trigger("nonexistent", Seq.empty)
    assertEquals(entry.status, ExecStatus.FAILURE)
    assert(entry.error.isDefined, s"should have error message")
    assert(entry.error.get.contains("not found"), s"error should mention task not found")
  }

  test("semaphore limits concurrent executions") {
    val log = TaskExecutionLog(20)
    val internals = stubInternals(Seq("compile"))
    val blockingInvoker = new TaskInvokerApi {
      def invoke(
          taskName: String, moduleIds: Seq[String], args: Seq[String],
          onNotification: ServerNotification => Unit
      ): TaskInvokeResult = {
        onNotification(ServerNotification.Output(s"Running $taskName..."))
        Thread.sleep(5000)
        TaskInvokeResult(Seq(TaskInvokeOutcome("mod1", true, None, false)), None)
      }
    }
    val runner = TaskRunner(blockingInvoker, internals, log, maxConcurrent = 2)

    runner.trigger("compile", Seq.empty)
    runner.trigger("compile", Seq.empty)
    runner.trigger("compile", Seq.empty)

    Thread.sleep(200)

    val running = log.recent(20).count(_.status == ExecStatus.RUNNING)
    assert(running <= 2, s"Should have at most 2 running, got $running")

    val pending = log.recent(20).count(_.status == ExecStatus.PENDING)
    assert(pending >= 1, s"Should have at least 1 pending, got $pending")
  }

  test("output is captured from onNotification callback") {
    val log = TaskExecutionLog(10)
    val internals = stubInternals(Seq("compile"))
    val loggingInvoker = new TaskInvokerApi {
      def invoke(
          taskName: String, moduleIds: Seq[String], args: Seq[String],
          onNotification: ServerNotification => Unit
      ): TaskInvokeResult = {
        onNotification(ServerNotification.Output("line 1"))
        onNotification(ServerNotification.Output("line 2"))
        onNotification(ServerNotification.Log(LogLevel.INFO, Instant.now(), "log message", None, None))
        TaskInvokeResult(Seq(TaskInvokeOutcome("mod1", true, None, false)), None)
      }
    }
    val runner = TaskRunner(loggingInvoker, internals, log, maxConcurrent = 3)
    runner.trigger("compile", Seq.empty)
    Thread.sleep(500)

    val completed = log.recent(1).head
    assert(completed.output.contains("line 1"))
    assert(completed.output.contains("line 2"))
    assert(completed.output.contains("log message"))
  }
}
