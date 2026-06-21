package ba.sake.deder.webdashboard.server

import java.time.Instant
import java.util.UUID
import java.util.concurrent.{Executors, Semaphore}
import ba.sake.deder.*
import ba.sake.deder.ServerNotification.LogLevel

class TaskRunner(
    taskInvoker: TaskInvokerApi,
    internals: DederProjectInternals,
    log: TaskExecutionLog,
    maxConcurrent: Int,
    taskRegistry: TasksRegistryApi
):
  private val executor = Executors.newVirtualThreadPerTaskExecutor()
  private val semaphore = new Semaphore(maxConcurrent)
  private val outputLock = new Object()

  /** Triggers a task execution. Returns immediately with a PENDING ExecEntry.
    * The task runs on a virtual thread and updates the log on completion.
    */
  def trigger(taskName: String, moduleIds: Seq[String]): ExecEntry =
    // Validate task name
    val knownTasks = taskRegistry.allTasks.map(t => t.name).toSet
    if !knownTasks.contains(taskName) then
      val entry = ExecEntry(
        execId = UUID.randomUUID().toString,
        taskName = taskName,
        moduleIds = moduleIds,
        startTime = Instant.now(),
        endTime = Some(Instant.now()),
        status = ExecStatus.FAILURE,
        output = "",
        outcomes = Seq.empty,
        renderedSummary = None,
        error = Some(s"Task '$taskName' not found. Available: ${knownTasks.toSeq.sorted.mkString(", ")}"),
        requestId = None
      )
      log.add(entry)
      return entry

    val execId = UUID.randomUUID().toString
    val entry = ExecEntry(
      execId = execId,
      taskName = taskName,
      moduleIds = moduleIds,
      startTime = Instant.now(),
      endTime = None,
      status = ExecStatus.PENDING,
      output = "",
      outcomes = Seq.empty,
      renderedSummary = None,
      error = None,
      requestId = None
    )
    log.add(entry)

    // Submit to virtual thread executor
    executor.submit(new Runnable { def run(): Unit = {
      semaphore.acquire()
      log.update(execId)(_.copy(status = ExecStatus.RUNNING))
      try
        runSubprocessSeen = false
        val output = new StringBuilder()
        val idHolder = new java.util.concurrent.atomic.AtomicReference[String](null)
        val result = taskInvoker.invoke(
          taskName = taskName,
          moduleIds = moduleIds,
          args = Seq.empty,
          onNotification = notif =>
            val line = formatNotification(notif)
            if line.nonEmpty then
              outputLock.synchronized {
                output.append(line).append('\n')
              }
            // capture requestId from currentRequests if available
            if idHolder.get() == null then
              internals.currentRequests.find(r =>
                r.taskName == taskName && r.moduleIds.toSet.subsetOf(moduleIds.toSet)
              ).foreach(r => idHolder.set(r.requestId))
        )
        if runSubprocessSeen then
          outputLock.synchronized {
            output.append("\n[WARN] Running processes via web dashboard is not supported\n")
          }
        val outcomes = result.outcomes.map(o =>
          ExecModuleOutcome(o.moduleId, o.success, o.error, o.fromCache)
        )
        val success = result.outcomes.forall(_.success)
        log.update(execId)(_.copy(
          status = if success then ExecStatus.SUCCESS else ExecStatus.FAILURE,
          endTime = Some(Instant.now()),
          outcomes = outcomes,
          output = output.toString(),
          renderedSummary = if runSubprocessSeen then None else result.renderedSummary,
          requestId = Option(idHolder.get())
        ))
      catch
        case e: Exception =>
          log.update(execId)(_.copy(
            status = ExecStatus.FAILURE,
            endTime = Some(Instant.now()),
            error = Some(e.getMessage)
          ))
      finally
        semaphore.release()
    }})

    entry

  private var runSubprocessSeen = false

  private def formatNotification(notif: ServerNotification): String = notif match
    case ServerNotification.Output(text) => text
    case ServerNotification.Log(level, _, message, moduleId, _) =>
      val modStr = moduleId.map(m => s"[$m] ").getOrElse("")
      s"[$level] ${modStr}$message"
    case ServerNotification.RunSubprocess(cmd, _, _) =>
      runSubprocessSeen = true
      ""
    case _ => ""
