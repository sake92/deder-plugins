package ba.sake.deder.protobuf

import ba.sake.deder.*
import ba.sake.deder.protobuf.tasks.{ProtoInputTasks, ProtobufGenerationTasks}
import ba.sake.deder.Protobuf

class ProtobufPluginImpl extends DederPluginApi {
  override def id: String = "protobuf"

  override def tasks(
      params: PluginTasksParams
  ): Either[String, Seq[AbstractTask[?]]] =
    try {
      val pluginModule = PluginConfigEvaluators.evaluate(
        pluginClassLoader = getClass.getClassLoader,
        modulePath = "ProtobufPlugin.pkl",
        configText = params.configText,
        clazz = classOf[Protobuf]
      )
      val config = pluginModule.config
      val protoSourceFiles = ProtoInputTasks.protoSourceFilesTask(config)
      val sourceGenerator = ProtobufGenerationTasks.sourceGeneratorTask(config, protoSourceFiles, params.coreTasks)
      val resourceGenerator = ProtobufGenerationTasks.resourceGeneratorTask(config, sourceGenerator)
      Right(Seq(protoSourceFiles, sourceGenerator, resourceGenerator))
    } catch {
      case error: Exception =>
        Left(s"Failed to initialize protobuf plugin config: ${error.getMessage}")
    }
}
