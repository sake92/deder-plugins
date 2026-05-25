package ba.sake.deder.buildinfo

import ba.sake.deder.*
import ba.sake.deder.BuildInfo

class BuildInfoPluginImpl extends DederPluginApi {
  override def id: String = "build-info"

  override def tasks(
      params: PluginTasksParams
  ): Either[String, Seq[AbstractTask[?]]] =
    try {
      val pluginModule = PluginConfigEvaluators.evaluate(
        pluginClassLoader = getClass.getClassLoader,
        modulePath = "BuildInfoPlugin.pkl",
        configText = params.configText,
        clazz = classOf[BuildInfo]
      )
      val config = pluginModule.config
      val generator = BuildInfoGenerationTasks.sourceGeneratorTask(config)
      Right(Seq(generator))
    } catch {
      case error: Exception =>
        Left(s"Failed to initialize build-info plugin config: ${error.getMessage}")
    }
}
