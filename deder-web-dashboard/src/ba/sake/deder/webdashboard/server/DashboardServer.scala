package ba.sake.deder.webdashboard.server

import ba.sake.sharaf.*, ba.sake.sharaf.jdkhttp.*
import ba.sake.deder.*
import ba.sake.deder.webdashboard.pages.{Layout, ModulesPage, LiveStatsPage}
import WebDashboard.WebDashboardPluginConfig

class DashboardServer(
    config: WebDashboardPluginConfig,
    project: DederProject,
    internals: DederProjectInternals
) {
  private var jdkServer: Option[JdkHttpServerSharafServer] = None

  private val refreshMs = config.statsRefreshIntervalMs

  private val routes = Routes {
    case GET -> Path() =>
      Response.redirect("/modules")

    case GET -> Path("modules") =>
      val content = ModulesPage.modulesTable(project)
      Response.withBody(Layout.htmlPage("Modules - Deder Dashboard", content))

    case GET -> Path("stats") =>
      val content = LiveStatsPage.fullStatsPage(internals, refreshMs)
      Response.withBody(Layout.htmlPage("Live Stats - Deder Dashboard", content))

    case GET -> Path("stats", "overview") =>
      Response.withBody(LiveStatsPage.overviewCards(internals))

    case GET -> Path("stats", "current") =>
      Response.withBody(LiveStatsPage.currentRequestsTable(internals))

    case GET -> Path("stats", "history") =>
      Response.withBody(LiveStatsPage.historyTable(internals))
  }

  def start(): Unit = {
    jdkServer = Some(JdkHttpServerSharafServer(config.host, config.port, routes))
    jdkServer.foreach(_.start())
  }

  def stop(): Unit = {
    jdkServer.foreach(_.stop())
  }
}
