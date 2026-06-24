package ba.sake.deder.sourcegen

import ba.sake.deder.config.DederProject
import ba.sake.deder.plugins.Sourcegen
import ba.sake.deder.sourcegen.munitHelper.FakeDederModule
import scala.jdk.CollectionConverters.*

import java.util.{List as JList, Map as JMap}

class SourcegenConfigNormalizerSuite extends munit.FunSuite {

  def makeDefaults(
      enabled: Boolean = true,
      deps: Seq[String] = Seq.empty,
      scriptsDir: String = "deder/sourcegen",
      scalaVersion: String = null,
      outputSourceSet: String = "main",
      extra: Map[String, String] = Map.empty
  ): Sourcegen.ModuleDefaults =
    new Sourcegen.ModuleDefaults(
      enabled,
      deps.asJava,
      scriptsDir,
      scalaVersion,
      outputSourceSet,
      extra.asJava
    )

  def makeOverride(
      enabled: java.lang.Boolean = null,
      deps: JList[String] = null,
      scriptsDir: String = null,
      scalaVersion: String = null,
      outputSourceSet: String = null,
      extra: JMap[String, String] = null
  ): Sourcegen.ModuleOverride =
    new Sourcegen.ModuleOverride(enabled, deps, scriptsDir, scalaVersion, outputSourceSet, extra)

  def makeConfig(
      defaults: Sourcegen.ModuleDefaults,
      overrides: Map[String, Sourcegen.ModuleOverride] = Map.empty
  ): Sourcegen.SourcegenPluginConfig =
    new Sourcegen.SourcegenPluginConfig(defaults, overrides.asJava)

  def makeModule(id: String): FakeDederModule =
    FakeDederModule(id, ".", DederProject.ModuleType.SCALA)

  test("resolves defaults when no override exists") {
    val defaults = makeDefaults(
      deps = Seq("com.lihaoyi::upickle:4.1.0"),
      extra = Map("package" -> "com.example")
    )
    val config = makeConfig(defaults)
    val module = makeModule("my-module")

    val resolved = SourcegenConfigNormalizer.normalize(module, config)

    assert(resolved.enabled)
    assertEquals(resolved.deps, Seq("com.lihaoyi::upickle:4.1.0"))
    assertEquals(resolved.scriptsDir, "deder/sourcegen")
    assertEquals(resolved.scalaVersion, Option.empty[String])
    assertEquals(resolved.outputSourceSet, "main")
    assertEquals(resolved.extra, Map("package" -> "com.example"))
  }

  test("module override wins over defaults") {
    val defaults = makeDefaults(
      deps = Seq("com.lihaoyi::upickle:4.1.0"),
      extra = Map("package" -> "com.example")
    )
    val moduleOverride = makeOverride(
      scriptsDir = "custom/gen",
      extra = JMap.of("package", "com.override")
    )
    val config = makeConfig(defaults, Map("my-module" -> moduleOverride))
    val module = makeModule("my-module")

    val resolved = SourcegenConfigNormalizer.normalize(module, config)

    assertEquals(resolved.scriptsDir, "custom/gen")
    assertEquals(resolved.extra("package"), "com.override")
  }

  test("partial override inherits non-overridden fields from defaults") {
    val defaults = makeDefaults(
      deps = Seq("com.lihaoyi::upickle:4.1.0"),
      scriptsDir = "deder/sourcegen",
      outputSourceSet = "main",
      extra = Map("key" -> "defaultVal")
    )
    val moduleOverride = makeOverride(
      outputSourceSet = "test"
    )
    val config = makeConfig(defaults, Map("my-module" -> moduleOverride))
    val module = makeModule("my-module")

    val resolved = SourcegenConfigNormalizer.normalize(module, config)

    assertEquals(resolved.deps, Seq("com.lihaoyi::upickle:4.1.0"))
    assertEquals(resolved.scriptsDir, "deder/sourcegen")
    assertEquals(resolved.outputSourceSet, "test")
    assertEquals(resolved.extra("key"), "defaultVal")
  }

  test("disabled module returns enabled = false") {
    val defaults = makeDefaults(enabled = true)
    val moduleOverride = makeOverride(enabled = false)
    val config = makeConfig(defaults, Map("my-module" -> moduleOverride))
    val module = makeModule("my-module")

    val resolved = SourcegenConfigNormalizer.normalize(module, config)

    assert(!resolved.enabled)
  }

  test("scalaVersion override") {
    val defaults = makeDefaults(scalaVersion = "3.6.0")
    val moduleOverride = makeOverride(scalaVersion = "3.7.4")
    val config = makeConfig(defaults, Map("my-module" -> moduleOverride))
    val module = makeModule("my-module")

    val resolved = SourcegenConfigNormalizer.normalize(module, config)

    assertEquals(resolved.scalaVersion, Some("3.7.4"))
  }
}
