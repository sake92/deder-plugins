package ba.sake.deder.springboot

import ba.sake.deder.*
import ba.sake.deder.plugins.Springboot

class SpringBootPluginImpl extends DederPluginApi {
  override def id: String = "spring-boot"

  override def init(params: PluginInitParams): Either[String, Seq[AbstractTask[?]]] =
    try {
      val pluginModule = PluginConfigEvaluators.evaluate(
        pluginClassLoader = getClass.getClassLoader,
        modulePath = "SpringBootPlugin.pkl",
        configText = params.configText,
        clazz = classOf[Springboot]
      )
      val config = pluginModule.config
      val core = params.coreTasks
      val jar = SpringBootTasks.jarTask(config, core.runClasspathTask, core.finalMainClassTask)
      val run = SpringBootTasks.runTask(config, jar, core.javaHomeTask)
      Right(Seq(jar, run))
    } catch {
      case error: Exception =>
        Left(s"Failed to initialize spring-boot plugin config: ${error.getMessage}")
    }
}
