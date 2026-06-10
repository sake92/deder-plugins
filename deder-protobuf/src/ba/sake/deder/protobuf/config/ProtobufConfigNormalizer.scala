package ba.sake.deder.protobuf.config

import ba.sake.deder.plugins.Protobuf
import ba.sake.deder.config.DederProject.{DederModule, JavaModule, JavaTestModule, ScalaModule, ScalaTestModule}
import scala.jdk.CollectionConverters.*
import ba.sake.deder.protobuf.protoc.*

final case class ResolverSpec(
    kind: String,
    command: String,
    path: Option[String],
    dependency: Option[String],
    mainClass: Option[String],
    jvmArgs: Seq[String],
    executableSubpath: Option[String],
    url: Option[String],
    digest: Option[String],
    sanctionedExecutablePath: Option[String],
    skipOnUnsupportedPlatform: Boolean
)

final case class ConfiguredBuiltinTarget(
    name: String,
    options: Option[String],
    outputDir: Option[String]
)

final case class ConfiguredPluginTarget(
    name: String,
    options: Option[String],
    outputDir: Option[String],
    resolver: ResolverSpec
)

final case class ConfiguredDescriptor(
    fileName: String,
    includeImports: Boolean,
    includeSourceInfo: Boolean
)

final case class ResolvedSourceSetConfig(
    enabled: Boolean,
    sourceDirs: Seq[String],
    includeGlobs: Seq[String],
    excludeGlobs: Seq[String],
    extraImportPaths: Seq[String],
    extraImportDeps: Seq[String],
    extraArgs: Seq[String],
    env: Map[String, String]
)

final case class ResolvedImportConfig(
    includeProjectDependencies: Boolean,
    dependencyArtifacts: Seq[String],
    paths: Seq[String]
)

final case class ResolvedModuleConfig(
    enabled: Boolean,
    sourceSetKind: SourceSetKind,
    sourceSet: ResolvedSourceSetConfig,
    protoc: ResolverSpec,
    builtins: Seq[ConfiguredBuiltinTarget],
    plugins: Seq[ConfiguredPluginTarget],
    imports: ResolvedImportConfig,
    descriptor: Option[ConfiguredDescriptor]
)

object ProtobufConfigNormalizer {

  def normalize(module: DederModule, config: Protobuf.ProtobufPluginConfig): ResolvedModuleConfig =
    module match {
      case m: ScalaTestModule =>
        normalize(m.id, m.sources.asScala.toSeq, m.resources.asScala.toSeq, SourceSetKind.Test, config)
      case m: JavaTestModule =>
        normalize(m.id, m.sources.asScala.toSeq, m.resources.asScala.toSeq, SourceSetKind.Test, config)
      case m: ScalaModule =>
        normalize(m.id, m.sources.asScala.toSeq, m.resources.asScala.toSeq, SourceSetKind.Main, config)
      case m: JavaModule =>
        normalize(m.id, m.sources.asScala.toSeq, m.resources.asScala.toSeq, SourceSetKind.Main, config)
      case other =>
        throw IllegalArgumentException(s"Unsupported module type for protobuf generation: ${other.getClass.getName}")
    }

  def normalize(
      moduleId: String,
      sources: Seq[String],
      resources: Seq[String],
      sourceSetKind: SourceSetKind,
      config: Protobuf.ProtobufPluginConfig
  ): ResolvedModuleConfig = {
    val defaults = config.defaults
    val overrideConfig = Option(config.modules.get(moduleId))
    val layout = ModuleLayoutInference.infer(sources, resources)
    val defaultSourceSet =
      if sourceSetKind == SourceSetKind.Main then defaults.main
      else defaults.test
    val overrideSourceSet =
      overrideConfig.flatMap(overrideValue =>
        if sourceSetKind == SourceSetKind.Main then Option(overrideValue.main)
        else Option(overrideValue.test)
      )

    val enabled = overrideConfig.flatMap(value => Option(value.enabled).map(_.booleanValue())).getOrElse(defaults.enabled)
    val sourceDirs = pickStrings(
      overrideSourceSet.flatMap(value => Option(value.sourceDirs).map(_.asScala.toSeq)),
      defaults = defaultSourceSet.sourceDirs.asScala.toSeq,
      fallback = ModuleLayoutInference.defaultProtoSourceDirs(layout, sourceSetKind)
    )
    val sourceSet = ResolvedSourceSetConfig(
      enabled = overrideSourceSet.flatMap(value => Option(value.enabled).map(_.booleanValue())).getOrElse(defaultSourceSet.enabled),
      sourceDirs = sourceDirs,
      includeGlobs = pickStrings(
        overrideSourceSet.flatMap(value => Option(value.includeGlobs).map(_.asScala.toSeq)),
        defaultSourceSet.includeGlobs.asScala.toSeq,
        Seq("**/*.proto")
      ),
      excludeGlobs = pickStrings(
        overrideSourceSet.flatMap(value => Option(value.excludeGlobs).map(_.asScala.toSeq)),
        defaultSourceSet.excludeGlobs.asScala.toSeq,
        Seq.empty
      ),
      extraImportPaths = pickStrings(
        overrideSourceSet.flatMap(value => Option(value.extraImportPaths).map(_.asScala.toSeq)),
        defaultSourceSet.extraImportPaths.asScala.toSeq,
        Seq.empty
      ),
      extraImportDeps = pickStrings(
        overrideSourceSet.flatMap(value => Option(value.extraImportDeps).map(_.asScala.toSeq)),
        defaultSourceSet.extraImportDeps.asScala.toSeq,
        Seq.empty
      ),
      extraArgs = pickStrings(
        overrideSourceSet.flatMap(value => Option(value.extraArgs).map(_.asScala.toSeq)),
        defaultSourceSet.extraArgs.asScala.toSeq,
        Seq.empty
      ),
      env = pickMap(
        overrideSourceSet.flatMap(value => Option(value.env).map(_.asScala.toMap)),
        defaultSourceSet.env.asScala.toMap,
        Map.empty
      )
    )
    val resolvedImports = {
      val importOverride = overrideConfig.flatMap(value => Option(value.imports))
      val importConfig = importOverride.getOrElse(defaults.imports)
      ResolvedImportConfig(
        includeProjectDependencies = importOverride.flatMap(value =>
          Option(value.includeProjectDependencies).map(_.booleanValue())
        ).getOrElse(importConfig.includeProjectDependencies),
        dependencyArtifacts = pickStrings(
          importOverride.flatMap(value => Option(value.dependencyArtifacts).map(_.asScala.toSeq)),
          defaults.imports.dependencyArtifacts.asScala.toSeq,
          Seq.empty
        ),
        paths = pickStrings(
          importOverride.flatMap(value => Option(value.paths).map(_.asScala.toSeq)),
          defaults.imports.paths.asScala.toSeq,
          Seq.empty
        )
      )
    }
    val protoc = toResolverSpec(
      overrideConfig.flatMap(value => Option(value.protoc)).getOrElse(defaults.protoc),
      defaultCommand = "protoc"
    )
    val builtins = overrideConfig
      .flatMap(value => Option(value.builtins).map(_.asScala.toSeq))
      .getOrElse(defaults.builtins.asScala.toSeq)
      .map(target =>
        ConfiguredBuiltinTarget(
          name = target.name,
          options = trimToOption(target.options),
          outputDir = trimToOption(target.outputDir)
        )
      )
    val plugins = overrideConfig
      .flatMap(value => Option(value.plugins).map(_.asScala.toSeq))
      .getOrElse(defaults.plugins.asScala.toSeq)
      .map(target =>
        ConfiguredPluginTarget(
          name = target.name,
          options = trimToOption(target.options),
          outputDir = trimToOption(target.outputDir),
          resolver = toResolverSpec(
            target.resolver,
            defaultCommand = s"protoc-gen-${target.name}"
          )
        )
      )
    val descriptor = overrideConfig
      .flatMap(value => Option(value.descriptor))
      .orElse(Option(defaults.descriptor))
      .map(value =>
        ConfiguredDescriptor(
          fileName = value.fileName,
          includeImports = value.includeImports,
          includeSourceInfo = value.includeSourceInfo
        )
      )

    ResolvedModuleConfig(
      enabled = enabled,
      sourceSetKind = sourceSetKind,
      sourceSet = sourceSet,
      protoc = protoc,
      builtins = builtins,
      plugins = plugins,
      imports = resolvedImports,
      descriptor = descriptor
    )
  }

  private def toResolverSpec(config: Protobuf.ResolverConfig, defaultCommand: String): ResolverSpec = {
    validateDeferredOptions(config)
    val command = trimToOption(config.command).getOrElse(defaultCommand)
    ResolverSpec(
      kind = config.kind.toString.toLowerCase.replace('_', '-'),
      command = command,
      path = trimToOption(config.path),
      dependency = trimToOption(config.dependency),
      mainClass = trimToOption(config.mainClass),
      jvmArgs = config.jvmArgs.asScala.toSeq,
      executableSubpath = trimToOption(config.executableSubpath),
      url = trimToOption(config.url),
      digest = trimToOption(config.digest),
      sanctionedExecutablePath = trimToOption(config.sanctionedExecutablePath),
      skipOnUnsupportedPlatform = config.skipOnUnsupportedPlatform
    )
  }

  private def validateDeferredOptions(config: Protobuf.ResolverConfig): Unit = {
    if config.url != null then
      throw IllegalArgumentException("URL-based resolver config is not supported in the MVP implementation")
    if config.digest != null then
      throw IllegalArgumentException("Digest verification is not supported in the MVP implementation")
    if config.sanctionedExecutablePath != null then
      throw IllegalArgumentException("Sanctioned executable paths are not supported in the MVP implementation")
    if config.skipOnUnsupportedPlatform then
      throw IllegalArgumentException("skipOnUnsupportedPlatform is reserved for a future parity follow-up")
  }

  private def trimToOption(value: String): Option[String] =
    Option(value).map(_.trim).filter(_.nonEmpty)

  private def pickStrings(overrideValue: Option[Seq[String]], defaults: Seq[String], fallback: Seq[String]): Seq[String] =
    overrideValue.getOrElse(if defaults.nonEmpty then defaults else fallback).map(_.trim).filter(_.nonEmpty).distinct

  private def pickMap(
      overrideValue: Option[Map[String, String]],
      defaults: Map[String, String],
      fallback: Map[String, String]
  ): Map[String, String] =
    overrideValue.getOrElse(if defaults.nonEmpty then defaults else fallback).view
      .mapValues(_.trim)
      .toMap
      .filter { case (key, value) => key.trim.nonEmpty && value.nonEmpty }
}
