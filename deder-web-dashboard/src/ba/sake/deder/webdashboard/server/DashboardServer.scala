package ba.sake.deder.webdashboard.server

import java.time.Instant
import ba.sake.sharaf.*, ba.sake.sharaf.jdkhttp.*
import ba.sake.sharaf.given_ResponseWritable_Html
import ba.sake.deder.*
import ba.sake.deder.config.DederProject
import ba.sake.deder.plugins.WebDashboard.WebDashboardPluginConfig

class DashboardServer(
    config: WebDashboardPluginConfig,
    apiRoutes: ApiRoutes,
    htmlRoutes: HtmlRoutes
) {
  private var jdkServer: Option[JdkHttpServerSharafServer] = None

  private val routes = Routes.merge(Seq(apiRoutes.routes, htmlRoutes.routes))

  def start(): Unit = {
    jdkServer = Some(JdkHttpServerSharafServer(config.host, config.port.toInt, routes))
    jdkServer.foreach(_.start())
  }

  def stop(): Unit = {
    jdkServer.foreach(_.stop())
  }
}
