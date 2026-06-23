package ba.sake.deder.webdashboard.server

import scala.jdk.CollectionConverters.*
import ba.sake.sharaf.*
import ba.sake.querson.QueryStringRW
import ba.sake.deder.*
import ba.sake.deder.ServerNotification.LogLevel
import ba.sake.deder.config.DederProject
import ba.sake.deder.webdashboard.*

class ApiRoutes(
    dashboardService: DashboardService,
    project: DederProject,
    internals: DederProjectInternals,
    executionLog: TaskExecutionLog,
    taskRunner: TaskRunner
) {
  val routes = Routes {
    case GET -> Path("api", "tasks") =>
      Response.withBody(executionLog.recent(200).map(toApi))

    case POST -> Path("api", "tasks", "run") =>
      case class QP(taskName: String, moduleIds: Seq[String], logLevel: String = "INFO") derives QueryStringRW{
        def filteredModuleIds: Seq[String] = moduleIds.filterNot(_.isBlank())
      }
      val qp = Request.current.queryParams[QP]
      val taskName = qp.taskName
      val logLevel = try LogLevel.valueOf(qp.logLevel) catch case _: Exception => LogLevel.INFO
      if taskName.nonEmpty then
        val entry = taskRunner.trigger(taskName, qp.filteredModuleIds, logLevel)
        Response.withBody(
          TaskRunResult(
            execId = Some(entry.execId),
            status = Some(entry.status.toString),
            taskName = Some(entry.taskName),
            error = entry.error
          )
        )
      else Response.withBody(TaskRunResult(error = Some("taskName is required")))

    case GET -> Path("api", "tasks", "exec") =>
      case class QP(execId: String = "") derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val res = executionLog.get(qp.execId).map(toApi)
      Response.withBodyOpt(res, "execId")

    case GET -> Path("api", "modules") =>
      val modules = project.modules.asScala.toSeq.map { m =>
        val moduleType = Option(m.`type`).map(_.name()).getOrElse("UNKNOWN")
        ApiModule(m.id, moduleType, m.moduleDeps.size())
      }
      Response.withBody(modules)

    case GET -> Path("api", "stats", "overview") =>
      val uptimeSecs = internals.serverUptime.getSeconds
      val res = StatsOverview(
        totalRequestsServed = internals.totalRequestsServed,
        totalErrors = internals.totalErrors,
        uptimeSecs = uptimeSecs
      )
      Response.withBody(res)

    case GET -> Path("api", "stats", "request-statuses") =>
      Response.withBody(dashboardService.requestStatuses)

    case POST -> Path("api", "cancel") =>
      case class QP(requestId: String) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val requestId = qp.requestId
      val cancelled = if requestId.nonEmpty then internals.cancelRequest(requestId) else false
      Response.withBody(CancelResult(cancelled))

    case POST -> Path("api", "tasks", "cancel") =>
      case class QP(execId: String) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val execId = qp.execId
      var cancelled = false
      if execId.nonEmpty then
        executionLog.get(execId).flatMap(_.requestId).foreach { requestId =>
          cancelled = internals.cancelRequest(requestId)
        }
        if cancelled then
          executionLog.update(execId)(_.copy(status = ExecStatus.CANCELLED, endTime = Some(java.time.Instant.now())))
      Response.withBody(CancelResult(cancelled))

    case GET -> Path("api", "stats", "history") =>
      val entries = internals.recentHistory.map { r =>
        ApiHistoryEntry(
          requestId = r.requestId,
          caller = formatCallerType(r.caller),
          taskName = r.taskName,
          moduleIds = r.moduleIds,
          startTimeMs = r.startTime.toEpochMilli,
          durationMs = r.duration.toMillis,
          success = r.success
        )
      }
      Response.withBody(entries)

    case GET -> Path("api", "stats", "task-aggregates") =>
      val res = dashboardService.taskAggregates
      Response.withBody(res)

    case GET -> Path("api", "stats", "module-breakdown") =>
      case class QP(task: String) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val res = dashboardService.moduleBreakdown(qp.task)
      Response.withBody(res)

    case GET -> Path("api", "stats", "error-summary") =>
      val res = dashboardService.errorSummary
      Response.withBody(res)

    case GET -> Path("api", "stats", "module-aggregates") =>
      case class QP(n: Int = 5) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val res = dashboardService.moduleAggregates(qp.n)
      Response.withBody(res)

    case GET -> Path("api", "server") =>
      Response.withBody(serverInfoJson)
  }

  private def toApi(e: ExecEntry): ApiExecEntry =
    ApiExecEntry(
      execId = e.execId,
      taskName = e.taskName,
      moduleIds = e.moduleIds,
      startTimeMs = e.startTime.toEpochMilli,
      endTimeMs = e.endTime.map(_.toEpochMilli),
      status = e.status.toString,
      output = e.output,
      error = e.error,
      requestId = e.requestId
    )

  private def currentRequests = {
    val requests = internals.currentRequests.map { r =>
      ApiCurrentRequest(
        requestId = r.requestId,
        caller = formatCallerType(r.caller),
        taskName = r.taskName,
        moduleIds = r.moduleIds,
        startTimeMs = r.startTime.toEpochMilli
      )
    }
    requests
  }

  private def serverInfoJson = {
    val jdkVersion = System.getProperty("java.version")
    val jdkVendor = System.getProperty("java.vendor")
    val osName = System.getProperty("os.name")
    val osArch = System.getProperty("os.arch")
    val processors = Runtime.getRuntime().availableProcessors()
    val maxHeapMB = Runtime.getRuntime().maxMemory() / (1024 * 1024)
    val usedHeapMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
    val dederVersion = DederGlobals.version
    val uptimeSecs = internals.serverUptime.getSeconds
    val moduleCount = project.modules.size()
    val projectRoot = DederGlobals.projectRootDir.toString
    val plugins = internals.loadedPlugins.map { p =>
      ApiPluginInfo(p.id, p.taskNames.size, p.taskNames)
    }
    ApiServerInfo(
      dederVersion = dederVersion,
      jdkVersion = jdkVersion,
      jdkVendor = jdkVendor,
      osName = osName,
      osArch = osArch,
      processors = processors,
      maxHeapMB = maxHeapMB,
      usedHeapMB = usedHeapMB,
      uptimeSecs = uptimeSecs,
      moduleCount = moduleCount,
      pluginCount = plugins.size,
      projectRoot = projectRoot,
      plugins = plugins
    )
  }

  private def formatCallerType(ct: CallerType): String = ct match
    case CallerType.Cli => "CLI"
    case CallerType.Bsp => "BSP"
    case null           => "unknown"
    case _              => ct.toString

}
