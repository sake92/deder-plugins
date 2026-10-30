package ba.sake.deder.flyway

import ba.sake.deder.*
import ba.sake.deder.plugins.Flyway

class FlywayPluginImpl extends DederPluginApi {
  override def id: String = "flyway"

  override def init(params: PluginInitParams): Either[String, Seq[AbstractTask[?]]] =
    try {
      val pluginModule = PluginConfigEvaluators.evaluate(
        pluginClassLoader = getClass.getClassLoader,
        modulePath = "FlywayPlugin.pkl",
        configText = params.configText,
        clazz = classOf[Flyway]
      )
      val config = pluginModule.config
      val classpath = params.coreTasks.runClasspathTask
      Right(FlywayTasks.all(config, classpath))
    } catch {
      case error: Exception => Left(s"Failed to initialize flyway plugin config: ${error.getMessage}")
    }
}
