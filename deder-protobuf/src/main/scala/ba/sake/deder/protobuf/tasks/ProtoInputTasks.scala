package ba.sake.deder.protobuf.tasks

import ba.sake.deder.{*, given}
import ba.sake.deder.config.DederProject.ModuleType
import ba.sake.deder.protobuf.config.ProtobufConfigNormalizer
import ba.sake.deder.protobuf.sources.ProtoInputResolver
import ba.sake.deder.Protobuf

object ProtoInputTasks {

  val supportedModuleTypes: Set[ModuleType] =
    Set(ModuleType.JAVA, ModuleType.JAVA_TEST, ModuleType.SCALA, ModuleType.SCALA_TEST)

  def protoSourceFilesTask(config: Protobuf.ProtobufPluginConfig): SourceFilesTask =
    SourceFilesTask(
      name = "protobufSourceFiles",
      execute = { ctx =>
        val resolved = ProtobufConfigNormalizer.normalize(ctx.module, config)
        if !resolved.enabled || !resolved.sourceSet.enabled then Seq.empty
        else
          ProtoInputResolver
            .declaredInputFiles(ProtoInputResolver.moduleRoot(ctx.module), resolved.sourceSet, resolved.imports)
            .map(DederPath(_))
      },
      supportedModuleTypes = supportedModuleTypes,
      description = "Discovered local .proto inputs for the module",
      category = "Protobuf"
    )
}
