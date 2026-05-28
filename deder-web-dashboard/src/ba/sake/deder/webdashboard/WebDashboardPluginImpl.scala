package ba.sake.deder.webdashboard

import ba.sake.deder.*
import ba.sake.deder.webdashboard.server.DashboardServer
import WebDashboard.WebDashboardPluginConfig

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

      val startTask = TaskBuilder.make[String](
        name = "webDashboardStart",
        singleton = true,
        category = "Web Dashboard",
        kind = TaskKind.Standard
      ).build { ctx =>
        server match
          case Some(srv) =>
            s"Server already running at http://${config.host}:${config.port}"
          case None =>
            val srv = DashboardServer(config, ctx.project, params.internals)
            val thread = new Thread(() => srv.start())
            thread.setDaemon(true)
            thread.setName("deder-web-dashboard-server")
            thread.start()
            server = Some(srv)
            Thread.sleep(500)
            s"http://${config.host}:${config.port}"
      }

      val stopTask = TaskBuilder.make[String](
        name = "webDashboardStop",
        singleton = true,
        category = "Web Dashboard",
        kind = TaskKind.Standard
      ).build { _ =>
        server match
          case Some(srv) =>
            srv.stop()
            server = None
            "Server stopped"
          case None =>
            "Server not running"
      }

      Right(Seq(startTask, stopTask))
    } catch {
      case error: Exception =>
        Left(s"Failed to initialize web-dashboard plugin config: ${error.getMessage}")
    }

  override def close(): Unit =
    server.foreach { srv =>
      srv.stop()
      server = None
    }
}
