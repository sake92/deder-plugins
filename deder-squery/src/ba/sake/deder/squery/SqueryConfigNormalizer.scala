package ba.sake.deder.squery

import ba.sake.deder.plugins.Squery
import scala.jdk.CollectionConverters.*

final case class SqueryConfig(
    enabled: Boolean,
    jdbcUrl: String,
    jdbcDeps: Seq[String],
    schemaMappings: Seq[(String, String)],
    colNameIdentifierMapper: String,
    typeNameMapper: String,
    rowTypeSuffix: String,
    daoTypeSuffix: String,
    typeMappingRules: Seq[String],
    includeTables: Seq[String],
    excludeTables: Seq[String],
    squeryVersion: String
)

object SqueryConfigNormalizer {
  def normalize(moduleId: String, config: Squery.SqueryPluginConfig): SqueryConfig = {
    val defaults = config.defaults
    val module = Option(config.modules.get(moduleId))

    def string(value: Squery.ModuleOverride => String, default: String): String =
      module.flatMap(m => Option(value(m))).getOrElse(default)

    def listing(value: Squery.ModuleOverride => java.util.List[String], default: java.util.List[String]): Seq[String] =
      module.flatMap(m => Option(value(m))).getOrElse(default).asScala.toSeq

    def mapping(
        value: Squery.ModuleOverride => java.util.Map[String, String],
        default: java.util.Map[String, String]
    ): Seq[(String, String)] =
      module.flatMap(m => Option(value(m))).getOrElse(default).asScala.toSeq

    SqueryConfig(
      enabled = module.flatMap(m => Option(m.enabled).map(_.booleanValue())).getOrElse(defaults.enabled),
      jdbcUrl = string(_.jdbcUrl, defaults.jdbcUrl),
      jdbcDeps = listing(_.jdbcDeps, defaults.jdbcDeps),
      schemaMappings = mapping(_.schemaMappings, defaults.schemaMappings),
      colNameIdentifierMapper = string(_.colNameIdentifierMapper, defaults.colNameIdentifierMapper),
      typeNameMapper = string(_.typeNameMapper, defaults.typeNameMapper),
      rowTypeSuffix = string(_.rowTypeSuffix, defaults.rowTypeSuffix),
      daoTypeSuffix = string(_.daoTypeSuffix, defaults.daoTypeSuffix),
      typeMappingRules = listing(_.typeMappingRules, defaults.typeMappingRules),
      includeTables = listing(_.includeTables, defaults.includeTables),
      excludeTables = listing(_.excludeTables, defaults.excludeTables),
      squeryVersion = string(_.squeryVersion, defaults.squeryVersion)
    )
  }
}
