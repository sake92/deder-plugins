package ba.sake.deder.sourcegen

import ba.sake.deder.{DederPluginApi, PluginInitParams, AbstractTask, PluginConfigEvaluators}
import ba.sake.deder.plugins.Sourcegen

class SourcegenPluginImpl extends DederPluginApi:

  override val id: String = "sourcegen"

  override def init(params: PluginInitParams): Either[String, Seq[AbstractTask[?]]] =
    try
      val pluginModule = PluginConfigEvaluators.evaluate(
        pluginClassLoader = getClass.getClassLoader,
        modulePath = "SourcegenPlugin.pkl",
        configText = params.configText,
        clazz = classOf[Sourcegen]
      )
      val config = pluginModule.config

      val discoverTask = SourcegenGenerationTasks.scriptSourceFilesTask(config)
      val generateTask = SourcegenGenerationTasks.sourceGeneratorTask(
        config, discoverTask, params.coreTasks.allDependenciesTask
      )

      Right(Seq(discoverTask, generateTask))
    catch
      case error: Exception =>
        Left(s"Failed to initialize sourcegen plugin config: ${error.getMessage}")
