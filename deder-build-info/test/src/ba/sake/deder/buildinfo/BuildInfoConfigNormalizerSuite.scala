package ba.sake.deder.buildinfo

import ba.sake.deder.plugins.Buildinfo
import munit.FunSuite

import java.util.{List as JList, Map as JMap}

class BuildInfoConfigNormalizerSuite extends FunSuite {

  test("defaults resolve to expected values") {
    val defaults = new Buildinfo.ModuleDefaults(
      true,
      null,
      "BuildInfo",
      false,
      false,
      JMap.of()
    )
    val config = new Buildinfo.BuildInfoPluginConfig(defaults, JMap.of())

    val resolved = BuildInfoConfigNormalizer.normalize(
      moduleId = "my-module",
      moduleType = "JAVA",
      scalaVersion = None,
      config = config
    )

    assertEquals(resolved.enabled, true)
    assertEquals(resolved.packageName, None)
    assertEquals(resolved.objectName, "BuildInfo")
    assertEquals(resolved.includeGitHash, false)
    assertEquals(resolved.includeTimestamp, false)
    assertEquals(resolved.extra, Map.empty)
    assertEquals(resolved.scalaVersion, None)
  }

  test("packageName resolves to Some when set") {
    val defaults = new Buildinfo.ModuleDefaults(
      true,
      "com.example",
      "BuildInfo",
      false,
      false,
      JMap.of()
    )
    val config = new Buildinfo.BuildInfoPluginConfig(defaults, JMap.of())

    val resolved = BuildInfoConfigNormalizer.normalize(
      moduleId = "my-module",
      moduleType = "SCALA",
      scalaVersion = Some("3.7.4"),
      config = config
    )

    assertEquals(resolved.packageName, Some("com.example"))
    assertEquals(resolved.scalaVersion, Some("3.7.4"))
  }

  test("module override wins over defaults") {
    val defaults = new Buildinfo.ModuleDefaults(
      true,
      null,
      "BuildInfo",
      false,
      false,
      JMap.of()
    )
    val moduleOverride = new Buildinfo.ModuleOverride(
      null,
      "com.override",
      "MyBuildInfo",
      true,
      null,
      JMap.of("myKey", "myValue")
    )
    val config = new Buildinfo.BuildInfoPluginConfig(defaults, JMap.of("my-module", moduleOverride))

    val resolved = BuildInfoConfigNormalizer.normalize(
      moduleId = "my-module",
      moduleType = "SCALA",
      scalaVersion = Some("3.7.4"),
      config = config
    )

    assertEquals(resolved.packageName, Some("com.override"))
    assertEquals(resolved.objectName, "MyBuildInfo")
    assertEquals(resolved.includeGitHash, true)
    assertEquals(resolved.includeTimestamp, false) // not overridden, uses default
    assertEquals(resolved.extra, Map("myKey" -> "myValue"))
  }

  test("disabled module resolves enabled=false") {
    val defaults = new Buildinfo.ModuleDefaults(
      true,
      null,
      "BuildInfo",
      false,
      false,
      JMap.of()
    )
    val moduleOverride = new Buildinfo.ModuleOverride(
      false,
      null,
      null,
      null,
      null,
      null
    )
    val config = new Buildinfo.BuildInfoPluginConfig(defaults, JMap.of("my-module", moduleOverride))

    val resolved = BuildInfoConfigNormalizer.normalize(
      moduleId = "my-module",
      moduleType = "SCALA",
      scalaVersion = None,
      config = config
    )

    assertEquals(resolved.enabled, false)
  }

  test("extra values merge from override and defaults") {
    val defaults = new Buildinfo.ModuleDefaults(
      true,
      null,
      "BuildInfo",
      false,
      false,
      JMap.of("defaultKey", "defaultVal")
    )
    val moduleOverride = new Buildinfo.ModuleOverride(
      null,
      null,
      null,
      null,
      null,
      JMap.of("overrideKey", "overrideVal")
    )
    val config = new Buildinfo.BuildInfoPluginConfig(defaults, JMap.of("my-module", moduleOverride))

    val resolved = BuildInfoConfigNormalizer.normalize(
      moduleId = "my-module",
      moduleType = "SCALA",
      scalaVersion = None,
      config = config
    )

    assertEquals(resolved.extra, Map("overrideKey" -> "overrideVal"))
  }
}
