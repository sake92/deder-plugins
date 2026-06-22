package ba.sake.deder.webdashboard.server

import scala.jdk.CollectionConverters.*
import ba.sake.sharaf.*
import ba.sake.querson.QueryStringRW
import ba.sake.deder.*
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
      case class QP(taskName: String, moduleIds: Seq[String]) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val taskName = qp.taskName
      val moduleIds = qp.moduleIds
      if taskName.nonEmpty then
        val entry = taskRunner.trigger(taskName, moduleIds)
        val status = entry.status.toString
        val err = entry.error.map(e => Map("error" -> e)).getOrElse(Map.empty)
        val res = Map("execId" -> entry.execId, "status" -> status, "taskName" -> entry.taskName) ++ err
        Response.withBody(res)
      else Response.withBody(Map("error" -> "taskName is required"))

    case GET -> Path("api", "tasks", "exec") =>
      val qp = Request.current.queryParams[(execId: String)]
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
      Response.withBody(currentRequests)

    case POST -> Path("api", "cancel") =>
      val requestId = Request.current.queryParams[(requestId: String)].requestId
      val cancelled = if requestId.nonEmpty then internals.cancelRequest(requestId) else false
      Response.withBody(Map("cancelled" -> cancelled))

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
      val qp = Request.current.queryParams[(task: String)]
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
      error = e.error
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
