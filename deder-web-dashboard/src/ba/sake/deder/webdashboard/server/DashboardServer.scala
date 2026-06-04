package ba.sake.deder.webdashboard.server

import ba.sake.sharaf.*, ba.sake.sharaf.jdkhttp.*
import ba.sake.sharaf.given_ResponseWritable_Html
import ba.sake.deder.*
import ba.sake.deder.config.DederProject
import ba.sake.deder.webdashboard.pages.{Layout, ModulesPage, ModulesGraphPage, ServerPage, LiveStatsPage}
import WebDashboard.WebDashboardPluginConfig

class DashboardServer(
    config: WebDashboardPluginConfig,
    project: DederProject,
    internals: DederProjectInternals
) {
  private var jdkServer: Option[JdkHttpServerSharafServer] = None

  private val refreshMs = config.statsRefreshIntervalMs.toInt

  private val routes = Routes {
    case GET -> Path() =>
      Response.redirect("/modules")

    case GET -> Path("modules") =>
      val content = ModulesPage.modulesTable(project)
      Response.withBody(Layout.htmlPage("Modules list - Deder Dashboard", "modules", content))

    case GET -> Path("modules", "graph") =>
      val content = ModulesGraphPage.dependencyGraph(project)
      Response.withBody(Layout.htmlPage("Modules graph - Deder Dashboard", "graph", content))

    case GET -> Path("server") =>
      val content = ServerPage.serverInfo(internals, project)
      Response.withBody(Layout.htmlPage("Server - Deder Dashboard", "server", content))

    case GET -> Path("live") =>
      val content = LiveStatsPage.fullPage(internals, refreshMs)
      Response.withBody(Layout.htmlPage("Live Stats - Deder Dashboard", "live", content))

    case GET -> Path("stats", "overview") =>
      val html = LiveStatsPage.overviewCards(internals)
      Response.withBody(html)

    case GET -> Path("stats", "current") =>
      val html = LiveStatsPage.currentRequestsTable(internals)
      Response.withBody(html)

    case GET -> Path("stats", "history") =>
      val html = LiveStatsPage.historyTable(internals)
      Response.withBody(html)
  }

  def start(): Unit = {
    jdkServer = Some(JdkHttpServerSharafServer(config.host, config.port.toInt, routes))
    jdkServer.foreach(_.start())
  }

  def stop(): Unit = {
    jdkServer.foreach(_.stop())
  }
}
