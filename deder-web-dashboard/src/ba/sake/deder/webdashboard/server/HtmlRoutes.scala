package ba.sake.deder.webdashboard.server

import scala.jdk.CollectionConverters.*
import ba.sake.sharaf.{*, given}
import ba.sake.querson.QueryStringRW
import ba.sake.deder.*
import ba.sake.deder.config.DederProject
import ba.sake.deder.webdashboard.*
import ba.sake.deder.plugins.WebDashboard.WebDashboardPluginConfig
import ba.sake.deder.webdashboard.pages.*
import java.time.Instant

class HtmlRoutes(
    config: WebDashboardPluginConfig,
    dashboardService: DashboardService,
    project: DederProject,
    internals: DederProjectInternals,
    executionLog: TaskExecutionLog,
    taskRunner: TaskRunner
) {

  private val refreshMs = config.statsRefreshIntervalMs.toInt
  private lazy val projectRoot = DederGlobals.projectRootDir.toString

  val routes = Routes {
    case GET -> Path() =>
      Response.redirect("/server")

    case GET -> Path("modules") =>
      val content = ModulesPage.modulesTable(project)
      Response.withBody(Layout.htmlPage("Modules list - Deder Dashboard", "modules", content, projectRoot))

    case GET -> Path("modules", "graph") =>
      val content = ModulesGraphPage.dependencyGraph(project)
      Response.withBody(Layout.htmlPage("Modules graph - Deder Dashboard", "graph", content, projectRoot))

    case GET -> Path("server") =>
      val content = ServerPage.fullPage(internals, project, refreshMs)
      Response.withBody(Layout.htmlPage("Home - Deder Dashboard", "server", content, projectRoot))

    // --- HTMX partials for Home tab ---
    case GET -> Path("server", "deder-card") =>
      val h = ServerPage.dederCard(internals, project)
      Response.withBody(h)

    // --- Auto-refresh toggle for Home ---
    case GET -> Path("server", "auto-refresh") =>
      case class QP(enabled: Boolean = true) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val enabled = qp.enabled
      val cb = ServerPage.autoRefreshCheckbox(enabled)
      val oob = ServerPage.autoRefreshOob(enabled, refreshMs)
      Response.withBody(html"$cb$oob")

    // --- Stats tab: Live ---
    case GET -> Path("live") =>
      val content = LivePage.fullPage(refreshMs)
      Response.withBody(Layout.htmlPage("Live - Deder Dashboard", "live", content, projectRoot))

    // --- Stats tab: History ---
    case GET -> Path("history") =>
      val content = HistoryPage.fullPage(refreshMs)
      Response.withBody(Layout.htmlPage("History - Deder Dashboard", "history", content, projectRoot))

    // --- Stats tab: Aggregates ---
    case GET -> Path("stats") =>
      val content = StatsPage.fullPage(refreshMs)
      Response.withBody(Layout.htmlPage("Aggregates - Deder Dashboard", "stats", content, projectRoot))

    // --- HTMX partials for Live tab ---
    case GET -> Path("stats", "overview") =>
      val h = LivePage.overviewCards(internals)
      Response.withBody(h)

    case GET -> Path("stats", "requests") =>
      val statuses = dashboardService.requestStatuses
      val h = LivePage.requestSections(statuses)
      Response.withBody(h)

    case GET -> Path("stats", "caches") =>
      val h = LivePage.cachesTable(internals)
      Response.withBody(h)

    case POST -> Path("stats", "caches", "clear") =>
      val result = internals.purgeInMemoryCaches()
      val h = LivePage.cachesClearedResponse(result, internals)
      Response.withBody(h)

    case POST -> Path("stats", "cancel") =>
      case class QP(requestId: String) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val requestId = qp.requestId
      if requestId.nonEmpty then
        val cancelled = internals.cancelRequest(requestId)
        if cancelled then Response.withBody(LivePage.cancelledBadge)
        else Response.withBody(LivePage.cancelButton(requestId))
      else Response.withBody(html"<span class='failure'>Invalid request</span>")

    // --- Auto-refresh toggle endpoints ---
    case GET -> Path("stats", "auto-refresh", "live") =>
      case class QP(enabled: Boolean = true) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val enabled = qp.enabled
      val cb = LivePage.autoRefreshCheckbox(enabled)
      val oob = LivePage.autoRefreshOob(enabled, refreshMs)
      Response.withBody(html"$cb$oob")

    case GET -> Path("stats", "auto-refresh", "history") =>
      case class QP(enabled: Boolean = true) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val enabled = qp.enabled
      val cb = HistoryPage.autoRefreshCheckbox(enabled)
      val oob = HistoryPage.autoRefreshOob(enabled, refreshMs)
      Response.withBody(html"$cb$oob")

    case GET -> Path("stats", "auto-refresh", "stats") =>
      case class QP(enabled: Boolean = true) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val enabled = qp.enabled
      val cb = StatsPage.autoRefreshCheckbox(enabled)
      val oob = StatsPage.autoRefreshOob(enabled, refreshMs)
      Response.withBody(html"$cb$oob")

    case GET -> Path("tasks", "auto-refresh") =>
      case class QP(enabled: Boolean = true) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val enabled = qp.enabled
      val cb = TasksPage.autoRefreshCheckbox(enabled)
      val oob = TasksPage.autoRefreshOob(enabled, refreshMs)
      Response.withBody(html"$cb$oob")

    // --- HTMX partials for History tab ---
    case GET -> Path("stats", "history-table") =>
      case class QP(
          search: String = "",
          caller: String = "",
          status: String = "all",
          sort: String = "newest",
          limit: Int = 50,
          offset: Int = 0
      ) derives QueryStringRW
      val qp = Request.current.queryParams[QP]

      val search = qp.search
      val caller = qp.caller
      val status = qp.status
      val sort = qp.sort
      val limit = qp.limit
      val offset = qp.offset

      val entries = dashboardService.filteredHistory(search, caller, status, sort, limit, offset)
      val hasMore = entries.size >= limit
      val table = HistoryPage.historyTable(entries)
      val moreBtn = HistoryPage.loadMoreButton(hasMore, limit, offset + limit)
      Response.withBody(html"$table$moreBtn")

    case GET -> Path("stats", "more-history") =>
      case class QP(
          search: String = "",
          caller: String = "",
          status: String = "all",
          sort: String = "newest",
          limit: Int = 50,
          offset: Int = 0
      ) derives QueryStringRW
      val qp = Request.current.queryParams[QP]

      val search = qp.search
      val caller = qp.caller
      val status = qp.status
      val sort = qp.sort
      val limit = qp.limit
      val offset = qp.offset

      val entries = dashboardService.filteredHistory(search, caller, status, sort, limit, offset)
      val hasMore = entries.size >= limit
      val rows = HistoryPage.historyTableRows(entries)
      val moreBtn =
        if hasMore then HistoryPage.loadMoreButton(hasMore, limit, offset + limit)
        else HistoryPage.noMoreContent()
      Response.withBody(html"$rows$moreBtn")

    // --- HTMX partials for Stats tab ---
    case GET -> Path("stats", "task-aggregates") =>
      val aggregates = dashboardService.taskAggregates
      val h = StatsPage.taskAggregatesTable(aggregates)
      Response.withBody(h)

    case GET -> Path("stats", "top-offenders") =>
      case class QP(n: Int = 5) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val n = qp.n
      val offenders = dashboardService.topOffenders(n)
      val h = StatsPage.topOffenders(offenders)
      Response.withBody(h)

    case GET -> Path("stats", "module-aggregates") =>
      case class QP(n: Int = 5) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val n = qp.n
      val aggregates = dashboardService.moduleAggregates(n)
      val h = StatsPage.moduleAggregatesSection(aggregates)
      Response.withBody(h)

    case GET -> Path("stats", "error-summary") =>
      val errors = dashboardService.errorSummary
      val h = StatsPage.errorSummary(errors)
      Response.withBody(h)

    case GET -> Path("stats", "module-breakdown") =>
      case class QP(task: String, expanded: Boolean = false) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val task = qp.task
      val expanded = qp.expanded
      val aggregates = dashboardService.taskAggregates
      aggregates.find(_.taskName == task) match
        case Some(agg) =>
          if expanded then
            val modules = dashboardService.moduleBreakdown(task)
            Response.withBody(StatsPage.expandedTaskRow(agg, modules))
          else Response.withBody(StatsPage.collapsedTaskRow(agg))
        case None =>
          Response.withBody(html"""<tr><td colspan="7">Task not found: $task</td></tr>""")

    case GET -> Path("stats", "collapse-task") =>
      case class QP(task: String) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val task = qp.task
      val aggregates = dashboardService.taskAggregates
      aggregates.find(_.taskName == task) match
        case Some(agg) =>
          Response.withBody(StatsPage.collapsedTaskRow(agg))
        case None =>
          Response.withBody(html"""<tr><td colspan="7">Task not found: $task</td></tr>""")

    // --- Tasks tab ---
    case GET -> Path("tasks") =>
      val allTasks = dashboardService.allTasks
      val content = TasksPage.fullPage(executionLog, internals, project, refreshMs, allTasks)
      Response.withBody(Layout.htmlPage("Tasks - Deder Dashboard", "tasks", content, projectRoot))

    case GET -> Path("tasks", "run") =>
      case class QP(taskName: String, moduleIds: Seq[String]) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val taskName = qp.taskName
      val moduleIdsRaw = qp.moduleIds
      if taskName.isEmpty then
        Response.withBody(html"""<tr><td colspan="7" class="failure">Task name is required</td></tr>""")
      else
        val moduleIds = qp.moduleIds
        taskRunner.trigger(taskName, moduleIds)
        val table = TasksPage.logTable(executionLog)
        Response.withBody(table)

    case POST -> Path("tasks", "cancel") =>
      case class QP(execId: String) derives QueryStringRW
      val qp = Request.current.queryParams[QP]
      val execId = qp.execId
      if execId.nonEmpty then
        executionLog.get(execId).flatMap(_.requestId).foreach { requestId =>
          internals.cancelRequest(requestId)
        }
        executionLog.update(execId)(_.copy(status = ExecStatus.CANCELLED, endTime = Some(Instant.now())))
      val table = TasksPage.logTable(executionLog)
      Response.withBody(table)

    case GET -> Path("tasks", "log-table") =>
      val table = TasksPage.logTable(executionLog)
      Response.withBody(table)
  }
}
