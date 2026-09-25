package ba.sake.deder.squery

import ba.sake.deder.*
import ba.sake.deder.plugins.Squery

class SqueryPluginImpl extends DederPluginApi {
  override def id: String = "squery"

  override def init(params: PluginInitParams): Either[String, Seq[AbstractTask[?]]] =
    try {
      val pluginModule = PluginConfigEvaluators.evaluate(
        pluginClassLoader = getClass.getClassLoader,
        modulePath = "SqueryPlugin.pkl",
        configText = params.configText,
        clazz = classOf[Squery]
      )
      Right(Seq(SqueryGenerationTask.make(pluginModule.config)))
    } catch {
      case error: Exception => Left(s"Failed to initialize squery plugin config: ${error.getMessage}")
    }
}
