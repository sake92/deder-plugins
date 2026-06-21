package ba.sake.deder.webdashboard

import ba.sake.deder.*
import ba.sake.deder.webdashboard.server.DashboardServer
import ba.sake.deder.plugins.WebDashboard

class WebDashboardPluginImpl extends DederPluginApi {
  override def id: String = "web-dashboard"

  private var server: Option[DashboardServer] = None

  override def init(params: PluginInitParams): Either[String, Seq[AbstractTask[?]]] =
    try {
      val pluginModule = PluginConfigEvaluators.evaluate(
        pluginClassLoader = getClass.getClassLoader,
        modulePath = "WebDashboardPlugin.pkl",
        configText = params.configText,
        clazz = classOf[WebDashboard]
      )
      val config = pluginModule.config
      // if init() is re-called (e.g., config reload), stop any stale server
      server.foreach { srv =>
        srv.stop()
        server = None
      }

      if config.enabled then
        val srv = DashboardServer(config, params.project, params.internals, params.taskInvoker, params.taskRegistry)
        val thread = new Thread(() => srv.start())
        thread.setDaemon(true)
        thread.setName("deder-web-dashboard-server")
        thread.start()
        server = Some(srv)
        Thread.sleep(500)

      Right(Seq.empty)
    } catch {
      case error: Exception =>
        Left(s"Failed to initialize web-dashboard plugin: ${error.getMessage}")
    }

  override def close(): Unit =
    server.foreach { srv =>
      srv.stop()
      server = None
    }
}
