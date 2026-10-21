package ba.sake.deder.flyway

import ba.sake.deder.plugins.Flyway
import munit.FunSuite

import java.util.{List as JList, Map as JMap}

class FlywayConfigNormalizerSuite extends FunSuite {
  private def defaults = new Flyway.ModuleDefaults(
    true,
    "jdbc:h2:mem:default",
    "sa",
    "secret",
    JList.of("classpath:db/migration"),
    JList.of(),
    JList.of("com.h2database:h2:2.3.232"),
    "11.8.2",
    true
  )

  private def empty = new Flyway.ModuleOverride(null, null, null, null, null, null, null, null, null)

  test("defaults apply when module has no override") {
    val config = new Flyway.FlywayPluginConfig(defaults, JMap.of())
    val resolved = FlywayConfigNormalizer.normalize("app", config)
    assertEquals(resolved.url, "jdbc:h2:mem:default")
    assertEquals(resolved.locations, Seq("classpath:db/migration"))
    assertEquals(resolved.cleanDisabled, true)
    assertEquals(resolved.flywayVersion, "11.8.2")
  }

  test("module overrides replace defaults and inherit the rest") {
    val overrideConfig = new Flyway.ModuleOverride(
      null,
      "jdbc:h2:mem:other",
      null,
      null,
      JList.of("filesystem:sql", " "),
      JList.of("PUBLIC"),
      null,
      null,
      false
    )
    val config = new Flyway.FlywayPluginConfig(defaults, JMap.of("app", overrideConfig))
    val resolved = FlywayConfigNormalizer.normalize("app", config)
    assertEquals(resolved.url, "jdbc:h2:mem:other")
    assertEquals(resolved.user, "sa")
    assertEquals(resolved.locations, Seq("filesystem:sql"))
    assertEquals(resolved.schemas, Seq("PUBLIC"))
    assertEquals(resolved.jdbcDeps, Seq("com.h2database:h2:2.3.232"))
    assertEquals(resolved.cleanDisabled, false)
    assertEquals(FlywayConfigNormalizer.normalize("other", config).url, "jdbc:h2:mem:default")
  }

  test("module can be disabled") {
    val config = new Flyway.FlywayPluginConfig(defaults, JMap.of("app", empty.withEnabled(false)))
    assert(!FlywayConfigNormalizer.normalize("app", config).enabled)
  }
}

class FlywayTasksSuite extends FunSuite {
  private val root = os.Path("/tmp/project")

  test("relative filesystem locations are resolved against the project root") {
    assertEquals(FlywayTasks.resolveLocation("filesystem:db/migration", root), "filesystem:/tmp/project/db/migration")
    assertEquals(FlywayTasks.resolveLocation("filesystem:/abs/sql", root), "filesystem:/abs/sql")
    assertEquals(FlywayTasks.resolveLocation("classpath:db/migration", root), "classpath:db/migration")
  }

  test("clean requires cleanDisabled = false and url is required") {
    val config = ResolvedFlywayConfig(true, "jdbc:h2:mem:x", "", "", Seq(), Seq(), Seq(), "11.8.2", true)
    intercept[IllegalArgumentException](FlywayTasks.validate(config, "app", "clean"))
    FlywayTasks.validate(config, "app", "migrate")
    FlywayTasks.validate(config.copy(cleanDisabled = false), "app", "clean")
    intercept[IllegalArgumentException](FlywayTasks.validate(config.copy(url = ""), "app", "migrate"))
  }
}
