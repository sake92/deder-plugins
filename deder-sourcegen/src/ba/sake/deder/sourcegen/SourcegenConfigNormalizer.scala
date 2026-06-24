package ba.sake.deder.sourcegen

import ba.sake.deder.config.DederProject.DederModule
import ba.sake.deder.plugins.Sourcegen
import scala.jdk.CollectionConverters.*

case class ResolvedModuleConfig(
    enabled: Boolean,
    deps: Seq[String],
    scriptsDir: String,
    scalaVersion: Option[String],
    outputSourceSet: String,
    extra: Map[String, String]
)

object SourcegenConfigNormalizer:

  def normalize(module: DederModule, config: Sourcegen.SourcegenPluginConfig): ResolvedModuleConfig =
    val moduleId = module.id
    val defaults = config.defaults
    val overrideConfig = Option(config.modules.get(moduleId))

    val enabled = overrideConfig
      .flatMap(v => Option(v.enabled).map(_.booleanValue()))
      .getOrElse(defaults.enabled)

    val deps = overrideConfig
      .flatMap(v => Option(v.deps).map(_.asScala.toSeq))
      .getOrElse(defaults.deps.asScala.toSeq)

    val scriptsDir = overrideConfig
      .flatMap(v => Option(v.scriptsDir))
      .filter(_.nonEmpty)
      .getOrElse(defaults.scriptsDir)

    val scalaVersion = overrideConfig
      .flatMap(v => Option(v.scalaVersion))
      .orElse(Option(defaults.scalaVersion))

    val outputSourceSet = overrideConfig
      .flatMap(v => Option(v.outputSourceSet))
      .filter(_.nonEmpty)
      .getOrElse(defaults.outputSourceSet)

    val extra = overrideConfig
      .flatMap(v => Option(v.extra).map(_.asScala.toMap))
      .getOrElse(defaults.extra.asScala.toMap)

    ResolvedModuleConfig(enabled, deps, scriptsDir, scalaVersion, outputSourceSet, extra)
