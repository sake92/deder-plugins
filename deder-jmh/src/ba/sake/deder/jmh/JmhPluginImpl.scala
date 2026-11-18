package ba.sake.deder.jmh

import ba.sake.deder.*
import ba.sake.deder.plugins.Jmh

class JmhPluginImpl extends DederPluginApi {
  override def id: String = "jmh"

  override def init(params: PluginInitParams): Either[String, Seq[AbstractTask[?]]] =
    try {
      val config = PluginConfigEvaluators.evaluate(
        pluginClassLoader = getClass.getClassLoader,
        modulePath = "JmhPlugin.pkl",
        configText = params.configText,
        clazz = classOf[Jmh]
      ).config
      val core = params.coreTasks
      Right(Seq(
        JmhTasks.runTask("jmhRun", false, config, core),
        JmhTasks.runTask("jmhList", true, config, core)
      ))
    } catch {
      case error: Exception => Left(s"Failed to initialize JMH plugin config: ${error.getMessage}")
    }
}
