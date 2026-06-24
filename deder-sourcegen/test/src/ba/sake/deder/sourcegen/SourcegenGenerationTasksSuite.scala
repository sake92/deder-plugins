package ba.sake.deder.sourcegen

import ba.sake.deder.{*, given}
import ba.sake.deder.plugins.Sourcegen
import ba.sake.deder.deps.Dependency
import ba.sake.tupson.JsonRW
import scala.jdk.CollectionConverters.*
import java.util.{Map as JMap, List as JList}

class SourcegenGenerationTasksSuite extends munit.FunSuite:

  def makeConfig(): Sourcegen.SourcegenPluginConfig =
    val defaults = Sourcegen.ModuleDefaults(
      true, JList.of(), "deder/sourcegen", null, "main", JMap.of()
    )
    new Sourcegen.SourcegenPluginConfig(defaults, JMap.of())

  private def constantTask[T: JsonRW: Hashable](name: String, value: T): AbstractTask[T] =
    TaskBuilder.make[T](name).build(_ => value)

  test("scriptSourceFilesTask has correct name and settings") {
    val config = makeConfig()
    val task = SourcegenGenerationTasks.scriptSourceFilesTask(config)

    assertEquals(task.name, "sourcegenDiscoverScripts")
  }

  test("scriptSourceFilesTask uses all module types") {
    val config = makeConfig()
    val task = SourcegenGenerationTasks.scriptSourceFilesTask(config)

    assert(task.supportedModuleTypes.isEmpty)
  }

  test("sourceGeneratorTask has correct name and kind") {
    val config = makeConfig()
    val discoverTask = SourcegenGenerationTasks.scriptSourceFilesTask(config)
    val allDepsTask = constantTask("allDependencies", Seq.empty[Dependency])
    val task = SourcegenGenerationTasks.sourceGeneratorTask(config, discoverTask, allDepsTask)

    assertEquals(task.name, "sourcegenGenerate")
  }
