package ba.sake.deder.flyway

import ba.sake.deder.plugins.Flyway
import scala.jdk.CollectionConverters.*

final case class ResolvedFlywayConfig(
    enabled: Boolean,
    url: String,
    user: String,
    password: String,
    locations: Seq[String],
    schemas: Seq[String],
    jdbcDeps: Seq[String],
    flywayVersion: String,
    cleanDisabled: Boolean
)

object FlywayConfigNormalizer {

  def normalize(moduleId: String, config: Flyway.FlywayPluginConfig): ResolvedFlywayConfig = {
    val defaults = config.defaults
    val module = Option(config.modules.get(moduleId))

    def string(value: Flyway.ModuleOverride => String, default: String): String =
      module.flatMap(m => Option(value(m))).getOrElse(default)

    def listing(value: Flyway.ModuleOverride => java.util.List[String], default: java.util.List[String]): Seq[String] =
      module.flatMap(m => Option(value(m))).getOrElse(default).asScala.toSeq.map(_.trim).filter(_.nonEmpty)

    ResolvedFlywayConfig(
      enabled = module.flatMap(m => Option(m.enabled).map(_.booleanValue())).getOrElse(defaults.enabled),
      url = string(_.url, defaults.url).trim,
      user = string(_.user, defaults.user),
      password = string(_.password, defaults.password),
      locations = listing(_.locations, defaults.locations),
      schemas = listing(_.schemas, defaults.schemas),
      jdbcDeps = listing(_.jdbcDeps, defaults.jdbcDeps),
      flywayVersion = string(_.flywayVersion, defaults.flywayVersion).trim,
      cleanDisabled =
        module.flatMap(m => Option(m.cleanDisabled).map(_.booleanValue())).getOrElse(defaults.cleanDisabled)
    )
  }
}
