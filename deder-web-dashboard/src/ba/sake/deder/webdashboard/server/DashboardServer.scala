package ba.sake.deder.webdashboard.server

import ba.sake.sharaf.*, ba.sake.sharaf.jdkhttp.*
import ba.sake.sharaf.given_ResponseWritable_Html
import ba.sake.deder.*
import ba.sake.deder.config.DederProject
import ba.sake.deder.webdashboard.pages.{Layout, ModulesPage, ModulesGraphPage, ServerPage, LivePage, HistoryPage, StatsPage}
import ba.sake.deder.plugins.WebDashboard.WebDashboardPluginConfig
import ba.sake.deder.webdashboard.server.ApiRoutes.{ApiTaskAggregate, ApiModuleAggregate}

class DashboardServer(
    config: WebDashboardPluginConfig,
    project: DederProject,
    internals: DederProjectInternals
) {
  private var jdkServer: Option[JdkHttpServerSharafServer] = None

  private val refreshMs = config.statsRefreshIntervalMs.toInt
  private lazy val projectRoot = DederGlobals.projectRootDir.toString

  private val routes = Routes {
    case GET -> Path() =>
      Response.redirect("/modules")

    case GET -> Path("modules") =>
      val content = ModulesPage.modulesTable(project)
      Response.withBody(Layout.htmlPage("Modules list - Deder Dashboard", "modules", content, projectRoot))

    case GET -> Path("modules", "graph") =>
      val content = ModulesGraphPage.dependencyGraph(project)
      Response.withBody(Layout.htmlPage("Modules graph - Deder Dashboard", "graph", content, projectRoot))

    case GET -> Path("server") =>
      val content = ServerPage.serverInfo(internals, project)
      Response.withBody(Layout.htmlPage("Info - Deder Dashboard", "server", content, projectRoot))

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

    case GET -> Path("stats", "current") =>
      val h = LivePage.currentRequestsTable(internals)
      Response.withBody(h)

    case GET -> Path("stats", "caches") =>
      val h = LivePage.cachesTable(internals)
      Response.withBody(h)

    // --- Auto-refresh toggle endpoints ---
    case GET -> Path("stats", "auto-refresh", "live") =>
      val req = summon[Request]
      val enabled = param(req, "enabled", "true").toBoolean
      val cb = LivePage.autoRefreshCheckbox(enabled)
      val oob = LivePage.autoRefreshOob(enabled, refreshMs)
      Response.withBody(html"$cb$oob")

    case GET -> Path("stats", "auto-refresh", "history") =>
      val req = summon[Request]
      val enabled = param(req, "enabled", "true").toBoolean
      val cb = HistoryPage.autoRefreshCheckbox(enabled)
      val oob = HistoryPage.autoRefreshOob(enabled, refreshMs)
      Response.withBody(html"$cb$oob")

    case GET -> Path("stats", "auto-refresh", "stats") =>
      val req = summon[Request]
      val enabled = param(req, "enabled", "true").toBoolean
      val cb = StatsPage.autoRefreshCheckbox(enabled)
      val oob = StatsPage.autoRefreshOob(enabled, refreshMs)
      Response.withBody(html"$cb$oob")

    // --- HTMX partials for History tab ---
    case GET -> Path("stats", "history-table") =>
      val req = summon[Request]
      val search = param(req, "search", "")
      val caller = param(req, "caller", "")
      val status = param(req, "status", "all")
      val sort = param(req, "sort", "newest")
      val limit = param(req, "limit", "50").toInt
      val offset = param(req, "offset", "0").toInt

      val entries = ApiRoutes.filteredHistory(internals, search, caller, status, sort, limit, offset)
      val hasMore = entries.size >= limit
      val table = HistoryPage.historyTable(entries)
      val moreBtn = HistoryPage.loadMoreButton(hasMore, limit, offset + limit)
      Response.withBody(html"$table$moreBtn")

    case GET -> Path("stats", "more-history") =>
      val req = summon[Request]
      val search = param(req, "search", "")
      val caller = param(req, "caller", "")
      val status = param(req, "status", "all")
      val sort = param(req, "sort", "newest")
      val limit = param(req, "limit", "50").toInt
      val offset = param(req, "offset", "0").toInt

      val entries = ApiRoutes.filteredHistory(internals, search, caller, status, sort, limit, offset)
      val hasMore = entries.size >= limit
      val rows = HistoryPage.historyTableRows(entries)
      val moreBtn = if hasMore then
        HistoryPage.loadMoreButton(hasMore, limit, offset + limit)
      else
        HistoryPage.noMoreContent()
      Response.withBody(html"$rows$moreBtn")

    // --- HTMX partials for Stats tab ---
    case GET -> Path("stats", "task-aggregates") =>
      val aggregates = ApiRoutes.taskAggregates(internals)
      val h = StatsPage.taskAggregatesTable(aggregates)
      Response.withBody(h)

    case GET -> Path("stats", "top-offenders") =>
      val req = summon[Request]
      val n = param(req, "n", "5").toInt
      val offenders = ApiRoutes.topOffenders(internals, n)
      val h = StatsPage.topOffenders(offenders)
      Response.withBody(h)

    case GET -> Path("stats", "error-summary") =>
      val errors = ApiRoutes.errorSummary(internals)
      val h = StatsPage.errorSummary(errors)
      Response.withBody(h)

    case GET -> Path("stats", "module-breakdown") =>
      val req = summon[Request]
      val task = param(req, "task", "")
      val expanded = param(req, "expanded", "false").toBoolean
      val aggregates = ApiRoutes.taskAggregates(internals)
      aggregates.find(_.taskName == task) match
        case Some(agg) =>
          if expanded then
            val modules = ApiRoutes.moduleBreakdown(internals, task)
            Response.withBody(StatsPage.expandedTaskRow(agg, modules))
          else
            Response.withBody(StatsPage.collapsedTaskRow(agg))
        case None =>
          Response.withBody(html"""<tr><td colspan="7">Task not found: $task</td></tr>""")

    case GET -> Path("stats", "collapse-task") =>
      val req = summon[Request]
      val task = param(req, "task", "")
      val aggregates = ApiRoutes.taskAggregates(internals)
      aggregates.find(_.taskName == task) match
        case Some(agg) =>
          Response.withBody(StatsPage.collapsedTaskRow(agg))
        case None =>
          Response.withBody(html"""<tr><td colspan="7">Task not found: $task</td></tr>""")

    // --- JSON APIs (existing + new) ---
    case GET -> Path("api", "modules") =>
      Response.withBody(ApiRoutes.modulesJson(project))
        .settingHeader("Content-Type", "application/json")

    case GET -> Path("api", "stats", "overview") =>
      Response.withBody(ApiRoutes.overviewJson(internals))
        .settingHeader("Content-Type", "application/json")

    case GET -> Path("api", "stats", "current") =>
      Response.withBody(ApiRoutes.currentRequestsJson(internals))
        .settingHeader("Content-Type", "application/json")

    case GET -> Path("api", "stats", "history") =>
      Response.withBody(ApiRoutes.historyJson(internals))
        .settingHeader("Content-Type", "application/json")

    case GET -> Path("api", "stats", "task-aggregates") =>
      Response.withBody(ApiRoutes.taskAggregatesJson(internals))
        .settingHeader("Content-Type", "application/json")

    case GET -> Path("api", "stats", "module-breakdown") =>
      val req = summon[Request]
      val task = param(req, "task", "")
      Response.withBody(ApiRoutes.moduleBreakdownJson(internals, task))
        .settingHeader("Content-Type", "application/json")

    case GET -> Path("api", "stats", "error-summary") =>
      Response.withBody(ApiRoutes.errorSummaryJson(internals))
        .settingHeader("Content-Type", "application/json")
  }

  private def param(req: Request, name: String, default: String): String =
    req.queryParamsRaw.get(name).flatMap(_.headOption).getOrElse(default)

  def start(): Unit = {
    jdkServer = Some(JdkHttpServerSharafServer(config.host, config.port.toInt, routes))
    jdkServer.foreach(_.start())
  }

  def stop(): Unit = {
    jdkServer.foreach(_.stop())
  }
}
