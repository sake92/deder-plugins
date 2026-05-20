package ba.sake.deder.protobuf.protoc

final case class ResolvedExecutable(command: String)

object ResolvedExecutable {
  def command(value: String): ResolvedExecutable = ResolvedExecutable(value)
}

final case class ResolvedBuiltinTarget(
    name: String,
    options: Option[String],
    outputDir: Option[String]
)

final case class ResolvedPluginTarget(
    name: String,
    options: Option[String],
    outputDir: Option[String],
    executable: ResolvedExecutable
)

final case class ResolvedDescriptorConfig(
    path: String,
    includeImports: Boolean,
    includeSourceInfo: Boolean
)

final case class ProtocInvocationRequest(
    executable: ResolvedExecutable,
    protoFiles: Seq[String],
    importRoots: Seq[String],
    sourceOutDir: String,
    builtins: Seq[ResolvedBuiltinTarget],
    plugins: Seq[ResolvedPluginTarget],
    descriptor: Option[ResolvedDescriptorConfig],
    extraArgs: Seq[String]
)
