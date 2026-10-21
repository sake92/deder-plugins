package ba.sake.deder.springboot

import ba.sake.deder.plugins.Springboot
import scala.jdk.CollectionConverters.*

final case class ResolvedSpringBootConfig(
    enabled: Boolean,
    springBootVersion: String,
    mainClass: Option[String],
    jvmOptions: Seq[String]
)

object SpringBootConfigNormalizer {

  def normalize(moduleId: String, config: Springboot.SpringBootPluginConfig): ResolvedSpringBootConfig = {
    val defaults = config.defaults
    val overrideConfig = Option(config.modules.get(moduleId))

    val enabled = overrideConfig
      .flatMap(value => Option(value.enabled).map(_.booleanValue()))
      .getOrElse(defaults.enabled)

    val springBootVersion = overrideConfig
      .flatMap(value => trimToOption(value.springBootVersion))
      .getOrElse(defaults.springBootVersion.trim)

    val mainClass = overrideConfig
      .flatMap(value => trimToOption(value.mainClass))
      .orElse(trimToOption(defaults.mainClass))

    val jvmOptions = overrideConfig
      .flatMap(value => Option(value.jvmOptions).map(_.asScala.toSeq))
      .getOrElse(defaults.jvmOptions.asScala.toSeq)
      .map(_.trim)
      .filter(_.nonEmpty)

    ResolvedSpringBootConfig(enabled, springBootVersion, mainClass, jvmOptions)
  }

  private def trimToOption(value: String): Option[String] =
    Option(value).map(_.trim).filter(_.nonEmpty)
}
