package ba.sake.deder.protobuf.protoc

import ba.sake.deder.TaskExecContext
import ba.sake.deder.protobuf.config.ResolverSpec

object ProtocDistributionResolver {

  def resolve[T, Deps <: Tuple](
      ctx: TaskExecContext[T, Deps],
      spec: ResolverSpec,
      moduleRoot: os.Path,
      workspace: os.Path
  ): ResolvedExecutable =
    ExecutableResolverSupport.resolveExecutable(ctx, spec, moduleRoot, workspace, "protoc")
}
