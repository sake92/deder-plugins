package ba.sake.deder.squery

import ba.sake.deder.plugins.Squery
import munit.FunSuite

import java.util.{List as JList, Map as JMap}

class SqueryConfigurationSuite extends FunSuite {
  private def defaults = new Squery.ModuleDefaults(
    true,
    "jdbc:h2:mem:default",
    JList.of("com.h2database:h2:2.3.232"),
    JMap.of("PUBLIC", "example"),
    "camelcase",
    "camelcase",
    "Row",
    "Dao",
    JList.of(),
    JList.of(),
    JList.of(),
    "0.12.0"
  )

  test("module overrides replace configured collections and inherit other defaults") {
    val overrideConfig = new Squery.ModuleOverride(
      null,
      "jdbc:h2:mem:other",
      JList.of(),
      JMap.of("OTHER", "custom"),
      null,
      null,
      null,
      null,
      null,
      JList.of("OTHER\\.ACTOR"),
      null,
      null
    )
    val config = new Squery.SqueryPluginConfig(defaults, JMap.of("other", overrideConfig))

    val resolved = SqueryConfigNormalizer.normalize("other", config)
    assertEquals(resolved.jdbcUrl, "jdbc:h2:mem:other")
    assertEquals(resolved.jdbcDeps, Seq.empty)
    assertEquals(resolved.schemaMappings, Seq("OTHER" -> "custom"))
    assertEquals(resolved.includeTables, Seq("OTHER\\.ACTOR"))
    assertEquals(resolved.rowTypeSuffix, "Row")
    assertEquals(SqueryConfigNormalizer.normalize("default", config).jdbcDeps, Seq("com.h2database:h2:2.3.232"))
  }

  test("CLI arguments preserve ordered repeated values and default to all tables") {
    val config = SqueryConfigNormalizer.normalize(
      "app",
      new Squery.SqueryPluginConfig(defaults, JMap.of())
    ).copy(
      typeMappingRules = Seq(".*_id|UUID|java.util.UUID", "name|VARCHAR|String"),
      excludeTables = Seq("PUBLIC\\.AUDIT_LOG")
    )

    val args = SqueryGenerationTask.arguments(config, os.Path("/tmp/generated"))
    assertEquals(args.sliding(2).filter(_.head == "--typeMappingRule").map(_.last).toSeq, config.typeMappingRules)
    assert(args.contains("PUBLIC:example"))
    assertEquals(args.sliding(2).filter(_.head == "--includeTables").map(_.last).toSeq, Seq(".*"))
    assertEquals(args.sliding(2).filter(_.head == "--excludeTables").map(_.last).toSeq, config.excludeTables)
  }
}
