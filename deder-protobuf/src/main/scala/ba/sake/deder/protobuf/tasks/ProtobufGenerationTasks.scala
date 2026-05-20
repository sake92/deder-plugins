package ba.sake.deder.protobuf.tasks

import ba.sake.deder.{*, given}
import ba.sake.deder.config.DederProject.ModuleType
import ba.sake.deder.deps.Dependency
import ba.sake.deder.protobuf.config.*
import ba.sake.deder.protobuf.protoc.*
import ba.sake.deder.protobuf.sources.ProtoInputResolver
import ba.sake.deder.Protobuf

object ProtobufGenerationTasks {

  private val supportedModuleTypes: Set[ModuleType] =
    Set(ModuleType.JAVA, ModuleType.JAVA_TEST, ModuleType.SCALA, ModuleType.SCALA_TEST)

  def sourceGeneratorTask(
      config: Protobuf.ProtobufPluginConfig,
      protoSourceFilesTask: SourceFilesTask,
      coreTasks: CoreTasksApi
  ) =
    CachedTaskBuilder
      .make[os.Path](
        name = "protobufGenerate",
        supportedModuleTypes = supportedModuleTypes,
        category = "Protobuf",
        kind = TaskKind.SourceGenerator
      )
      .dependsOn(protoSourceFilesTask)
      .dependsOn(coreTasks.allDependenciesTask)
      .build { ctx =>
        val (protoFiles, dependencies) = ctx.depResults
        val resolved = ProtobufConfigNormalizer.normalize(ctx.module, config)
        val sourceOut = ctx.out / "sources"
        prepareDirectory(sourceOut)
        prepareDirectory(ctx.out / "resources")
        if !resolved.enabled || !resolved.sourceSet.enabled || protoFiles.isEmpty then sourceOut
        else {
          val moduleRoot = ProtoInputResolver.moduleRoot(ctx.module)
          val inputs = ProtoInputResolver.resolveGenerationInputs(
            module = ctx.module,
            sourceSet = resolved.sourceSet,
            imports = resolved.imports,
            projectDependencies = dependencies,
            dependencyResolver = ctx.dependencyResolver,
            notifications = ctx.notifications,
            workspace = ctx.out / "imports"
          )
          val request = ProtocInvocationRequest(
            executable = ProtocDistributionResolver.resolve(ctx, resolved.protoc, moduleRoot, ctx.out / "tools" / "protoc"),
            protoFiles = inputs.localProtoFiles.map(_.toString),
            importRoots = inputs.importRoots.map(_.toString),
            sourceOutDir = sourceOut.toString,
            builtins = resolved.builtins.map(target =>
              ResolvedBuiltinTarget(
                name = target.name,
                options = target.options,
                outputDir = target.outputDir.map(dir => (sourceOut / os.RelPath(dir)).toString)
              )
            ),
            plugins = resolved.plugins.map(plugin =>
              ProtocPluginResolver.resolve(ctx, plugin, moduleRoot, ctx.out / "tools" / "plugins").copy(
                outputDir = plugin.outputDir.map(dir => (sourceOut / os.RelPath(dir)).toString)
              )
            ),
            descriptor = resolved.descriptor.map(descriptor =>
              ResolvedDescriptorConfig(
                path = (ctx.out / "resources" / descriptor.fileName).toString,
                includeImports = descriptor.includeImports,
                includeSourceInfo = descriptor.includeSourceInfo
              )
            ),
            extraArgs = resolved.sourceSet.extraArgs
          )
          if resolved.builtins.isEmpty && resolved.plugins.isEmpty && resolved.descriptor.isEmpty then sourceOut
          else {
            ProtocRunner.run(
              request = request,
              env = resolved.sourceSet.env,
              cwd = moduleRoot,
              notifications = ctx.notifications
            )
            sourceOut
          }
        }
      }

  def resourceGeneratorTask(
      config: Protobuf.ProtobufPluginConfig,
      sourceGeneratorTask: AbstractTask[os.Path]
  ) =
    CachedTaskBuilder
      .make[os.Path](
        name = "protobufGenerateResources",
        supportedModuleTypes = supportedModuleTypes,
        category = "Protobuf",
        kind = TaskKind.ResourceGenerator
      )
      .dependsOn(sourceGeneratorTask)
      .build { ctx =>
        val sourceGeneratorOut = ctx.depResults._1
        val resolved = ProtobufConfigNormalizer.normalize(ctx.module, config)
        val resourceOut = ctx.out / "resources"
        prepareDirectory(resourceOut)
        resolved.descriptor match {
          case None => resourceOut
          case Some(_) =>
            val generatedResourcesDir = sourceGeneratorOut / os.up / "resources"
            if os.exists(generatedResourcesDir) then
              os.copy.over(generatedResourcesDir, resourceOut, createFolders = true)
            resourceOut
        }
      }

  private def prepareDirectory(path: os.Path): Unit = {
    if os.exists(path) then os.remove.all(path)
    os.makeDir.all(path)
  }
}
