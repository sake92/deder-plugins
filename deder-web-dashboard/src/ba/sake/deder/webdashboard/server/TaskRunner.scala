package ba.sake.deder.webdashboard.server

import java.time.Instant
import java.util.UUID
import java.util.concurrent.{Executors, Semaphore}
import scala.jdk.CollectionConverters.*
import ba.sake.deder.*
import ba.sake.deder.ServerNotification.LogLevel
import ba.sake.deder.config.DederProject

class TaskRunner(
  project: DederProject,
    taskInvoker: TaskInvokerApi,
    internals: DederProjectInternals,
    log: TaskExecutionLog,
    maxConcurrent: Int,
    taskRegistry: TasksRegistryApi
):
  private val executor = Executors.newVirtualThreadPerTaskExecutor()
  private val semaphore = new Semaphore(maxConcurrent)
  private val outputLock = new Object()
  private var runSubprocessSeen = false

  /** Triggers a task execution. Returns immediately with a PENDING or FAILURE ExecEntry. */
  def trigger(taskName: String, moduleIds: Seq[String], logLevel: LogLevel = LogLevel.INFO): ExecEntry =
    val allTasks = taskRegistry.allTasks
    val knownTasks = allTasks.map(t => t.name).toSet
    val allModulesIds = project.modules.asScala.map(_.id).toSeq
    val resolvedModuleIds = if moduleIds.isEmpty then allModulesIds else moduleIds

    val validationError: Option[String] =
      if !knownTasks.contains(taskName) then
        Some(s"Task '$taskName' not found. Available: ${knownTasks.toSeq.sorted.mkString(", ")}")
      else if allTasks.exists(t => t.name == taskName && t.singleton) && resolvedModuleIds.lengthIs != 1 then
        val hint = if resolvedModuleIds.isEmpty then "pick a single module" else "pick only 1 module"
        Some(s"Task '$taskName' is singleton — $hint")
      else None

    validationError match
      case Some(msg) =>
        val entry = failEntry(taskName, resolvedModuleIds, msg)
        log.add(entry)
        entry
      case None =>
        submitTask(taskName, resolvedModuleIds, logLevel)

  private def failEntry(taskName: String, moduleIds: Seq[String], msg: String): ExecEntry =
    ExecEntry(
      execId = UUID.randomUUID().toString,
      taskName = taskName,
      moduleIds = moduleIds,
      startTime = Instant.now(),
      endTime = Some(Instant.now()),
      status = ExecStatus.FAILURE,
      output = "",
      outcomes = Seq.empty,
      renderedSummary = None,
      error = Some(msg),
      requestId = None
    )

  private def submitTask(taskName: String, moduleIds: Seq[String], logLevel: LogLevel): ExecEntry =
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
            val line = formatNotification(notif, logLevel)
            if line.nonEmpty then
              outputLock.synchronized {
                output.append(line).append('\n')
              }
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

  private def formatNotification(notif: ServerNotification, logLevel: LogLevel): String = notif match
    case ServerNotification.Output(text) => text
    case ServerNotification.Log(level, _, message, moduleId, _) =>
      if level.ordinal <= logLevel.ordinal then
        val modStr = moduleId.map(m => s"[$m] ").getOrElse("")
        s"[$level] ${modStr}$message"
      else ""
    case ServerNotification.RunSubprocess(_, _, _) =>
      runSubprocessSeen = true
      ""
    case _ => ""
