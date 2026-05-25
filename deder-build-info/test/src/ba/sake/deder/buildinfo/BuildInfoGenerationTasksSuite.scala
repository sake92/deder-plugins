package ba.sake.deder.buildinfo

import ba.sake.deder.{*, given}
import ba.sake.deder.BuildInfo
import ba.sake.tupson.JsonRW
import munit.FunSuite

import java.util.{List as JList, Map as JMap}

class BuildInfoGenerationTasksSuite extends FunSuite {

  test("generateScalaSource produces all always-on fields") {
    val config = ResolvedBuildInfoConfig(
      enabled = true,
      packageName = Some("com.example"),
      objectName = "BuildInfo",
      includeGitHash = false,
      includeTimestamp = false,
      extra = Map.empty,
      scalaVersion = Some("3.7.4")
    )

    val source = BuildInfoGenerationTasks.generateScalaSource(
      moduleName = "my-app",
      config = config
    )

    assert(source.contains("package com.example"))
    assert(source.contains("object BuildInfo {"))
    assert(source.contains("val moduleName: String = \"my-app\""))
    assert(source.contains("val moduleVersion: String = \"0.1.0-SNAPSHOT\""))
    assert(source.contains("val scalaVersion: String = \"3.7.4\""))
    assert(source.contains("val javaVersion: String ="))
    assert(!source.contains("val gitHead"))
    assert(!source.contains("val builtAtMillis"))
    assert(source.contains("def toMap: Map[String, Any] ="))
  }

  test("generateScalaSource omits scalaVersion for Java modules") {
    val config = ResolvedBuildInfoConfig(
      enabled = true,
      packageName = None,
      objectName = "BuildInfo",
      includeGitHash = false,
      includeTimestamp = false,
      extra = Map.empty,
      scalaVersion = None
    )

    val source = BuildInfoGenerationTasks.generateScalaSource(
      moduleName = "my-app",
      config = config
    )

    assert(!source.contains("package "))
    assert(!source.contains("scalaVersion"))
    assert(source.contains("object BuildInfo {"))
  }

  test("generateScalaSource includes opt-in fields when enabled") {
    val config = ResolvedBuildInfoConfig(
      enabled = true,
      packageName = None,
      objectName = "BuildInfo",
      includeGitHash = true,
      includeTimestamp = true,
      extra = Map.empty,
      scalaVersion = None
    )

    val gitHash = "abc123def"
    val timestamp = 1711123200000L

    val source = BuildInfoGenerationTasks.generateScalaSource(
      moduleName = "my-app",
      config = config,
      gitHash = Some(gitHash),
      timestamp = Some(timestamp)
    )

    assert(source.contains(s"val gitHead: String = \"$gitHash\""))
    assert(source.contains(s"val builtAtMillis: Long = ${timestamp}L"))
    assert(source.contains(s""""gitHead" -> gitHead"""))
    assert(source.contains(s""""builtAtMillis" -> builtAtMillis"""))
  }

  test("generateScalaSource includes extra values alphabetically") {
    val config = ResolvedBuildInfoConfig(
      enabled = true,
      packageName = None,
      objectName = "BuildInfo",
      includeGitHash = false,
      includeTimestamp = false,
      extra = Map("zebra" -> "z", "alpha" -> "a"),
      scalaVersion = None
    )

    val source = BuildInfoGenerationTasks.generateScalaSource(
      moduleName = "my-app",
      config = config
    )

    assert(source.contains("val alpha: String = \"a\""))
    assert(source.contains("val zebra: String = \"z\""))

    val alphaIndex = source.indexOf("val alpha")
    val zebraIndex = source.indexOf("val zebra")
    assert(alphaIndex < zebraIndex, "extra values should be sorted alphabetically")
  }

  test("generateScalaSource toMap includes all active keys") {
    val config = ResolvedBuildInfoConfig(
      enabled = true,
      packageName = None,
      objectName = "BuildInfo",
      includeGitHash = true,
      includeTimestamp = true,
      extra = Map("custom" -> "val"),
      scalaVersion = Some("3.7.4")
    )

    val source = BuildInfoGenerationTasks.generateScalaSource(
      moduleName = "my-app",
      config = config,
      gitHash = Some("abc"),
      timestamp = Some(123L)
    )

    assert(source.contains("\"moduleName\" -> moduleName"))
    assert(source.contains("\"moduleVersion\" -> moduleVersion"))
    assert(source.contains("\"scalaVersion\" -> scalaVersion"))
    assert(source.contains("\"javaVersion\" -> javaVersion"))
    assert(source.contains("\"gitHead\" -> gitHead"))
    assert(source.contains("\"builtAtMillis\" -> builtAtMillis"))
    assert(source.contains("\"custom\" -> custom"))
  }

  test("task name is buildInfoGenerate") {
    val config = new BuildInfo.BuildInfoPluginConfig(
      new BuildInfo.ModuleDefaults(true, null, "BuildInfo", false, false, JMap.of()),
      JMap.of()
    )
    val task = BuildInfoGenerationTasks.sourceGeneratorTask(config)

    assertEquals(task.name, "buildInfoGenerate")
  }
}
