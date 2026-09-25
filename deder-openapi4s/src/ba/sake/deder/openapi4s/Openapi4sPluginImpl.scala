package ba.sake.deder.openapi4s

import ba.sake.deder.{AbstractTask, DederPluginApi, PluginConfigEvaluators, PluginInitParams}
import ba.sake.deder.plugins.Openapi4s

class Openapi4sPluginImpl extends DederPluginApi:
  override val id: String = "openapi4s"

  override def init(params: PluginInitParams): Either[String, Seq[AbstractTask[?]]] =
    try
      val pluginModule = PluginConfigEvaluators.evaluate(
        pluginClassLoader = getClass.getClassLoader,
        modulePath = "Openapi4sPlugin.pkl",
        configText = params.configText,
        clazz = classOf[Openapi4s]
      )
      Right(Seq(Openapi4sGenerationTask.make(pluginModule.config)))
    catch
      case error: Exception => Left(s"Failed to initialize openapi4s plugin config: ${error.getMessage}")
