package ba.sake.deder.buildinfo

import ba.sake.deder.plugins.Buildinfo
import scala.jdk.CollectionConverters.*

final case class ResolvedBuildInfoConfig(
    enabled: Boolean,
    packageName: Option[String],
    objectName: String,
    includeGitHash: Boolean,
    includeTimestamp: Boolean,
    extra: Map[String, String],
    scalaVersion: Option[String]
)

object BuildInfoConfigNormalizer {

  def normalize(
      moduleId: String,
      moduleType: String,
      scalaVersion: Option[String],
      config: Buildinfo.BuildInfoPluginConfig
  ): ResolvedBuildInfoConfig = {
    val defaults = config.defaults
    val overrideConfig = Option(config.modules.get(moduleId))

    val enabled = overrideConfig
      .flatMap(value => Option(value.enabled).map(_.booleanValue()))
      .getOrElse(defaults.enabled)

    val packageName = overrideConfig
      .flatMap(value => Option(value.packageName))
      .orElse(trimToOption(defaults.packageName))

    val objectName = overrideConfig
      .flatMap(value => trimToOption(value.objectName))
      .getOrElse(defaults.objectName)

    val includeGitHash = overrideConfig
      .flatMap(value => Option(value.includeGitHash).map(_.booleanValue()))
      .getOrElse(defaults.includeGitHash)

    val includeTimestamp = overrideConfig
      .flatMap(value => Option(value.includeTimestamp).map(_.booleanValue()))
      .getOrElse(defaults.includeTimestamp)

    val extra = overrideConfig
      .flatMap(value => Option(value.extra).map(_.asScala.toMap))
      .getOrElse(defaults.extra.asScala.toMap)
      .view
      .mapValues(_.trim)
      .toMap
      .filter { case (key, value) => key.trim.nonEmpty && value.nonEmpty }

    ResolvedBuildInfoConfig(
      enabled = enabled,
      packageName = packageName,
      objectName = objectName,
      includeGitHash = includeGitHash,
      includeTimestamp = includeTimestamp,
      extra = extra,
      scalaVersion = scalaVersion
    )
  }

  private def trimToOption(value: String): Option[String] =
    Option(value).map(_.trim).filter(_.nonEmpty)
}
