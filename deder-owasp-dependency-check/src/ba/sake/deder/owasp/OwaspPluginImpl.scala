package ba.sake.deder.owasp

import ba.sake.deder.*
import ba.sake.deder.plugins.Owasp

class OwaspPluginImpl extends DederPluginApi {
  override def id: String = "owasp-dependency-check"
  override def init(params: PluginInitParams): Either[String, Seq[AbstractTask[?]]] =
    try {
      val config = PluginConfigEvaluators.evaluate(
        pluginClassLoader = getClass.getClassLoader,
        modulePath = "OwaspPlugin.pkl",
        configText = params.configText,
        clazz = classOf[Owasp]
      ).config
      Right(Seq(OwaspTask.make(config, params.coreTasks.runClasspathTask)))
    } catch {
      case error: Exception => Left(s"Failed to initialize OWASP plugin config: ${error.getMessage}")
    }
}
