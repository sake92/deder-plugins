package ba.sake.deder.protobuf.protoc

import ba.sake.deder.TaskExecContext
import ba.sake.deder.protobuf.config.ConfiguredPluginTarget

object ProtocPluginResolver {

  def resolve[T, Deps <: Tuple](
      ctx: TaskExecContext[T, Deps],
      plugin: ConfiguredPluginTarget,
      moduleRoot: os.Path,
      workspace: os.Path
  ): ResolvedPluginTarget =
    ResolvedPluginTarget(
      name = plugin.name,
      options = plugin.options,
      outputDir = plugin.outputDir,
      executable = ExecutableResolverSupport.resolveExecutable(
        ctx = ctx,
        spec = plugin.resolver,
        moduleRoot = moduleRoot,
        workspace = workspace / s"protoc-gen-${plugin.name}",
        defaultCommand = s"protoc-gen-${plugin.name}"
      )
    )
}
