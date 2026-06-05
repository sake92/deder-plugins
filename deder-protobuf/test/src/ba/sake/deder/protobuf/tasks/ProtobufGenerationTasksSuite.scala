package ba.sake.deder.protobuf.tasks

import ba.sake.deder.{*, given}
import ba.sake.deder.Protobuf
import ba.sake.deder.deps.Dependency
import ba.sake.tupson.JsonRW
import munit.FunSuite

import java.util.{List as JList, Map as JMap}

class ProtobufGenerationTasksSuite extends FunSuite {

  test("protobuf generation tasks use protobuf-prefixed names") {
    val config = new Protobuf.ProtobufPluginConfig(defaults, JMap.of())
    val protoSourceFiles = ProtoInputTasks.protoSourceFilesTask(config)
    val sourceGenerator = ProtobufGenerationTasks.sourceGeneratorTask(config, protoSourceFiles, constantTask("allDependencies", Seq.empty))
    val resourceGenerator = ProtobufGenerationTasks.resourceGeneratorTask(config, sourceGenerator)

    assertEquals(sourceGenerator.name, "protobufGenerate")
    assertEquals(resourceGenerator.name, "protobufGenerateResources")
  }

  private val defaults = new Protobuf.ModuleDefaults(
    true,
    new Protobuf.ResolverConfig(Protobuf.ResolverKind.SYSTEM_PATH, "protoc", null, null, null, JList.of(), null, null, null, null, false),
    JList.of(new Protobuf.BuiltinLanguageTarget("java", null, null)),
    JList.of(),
    new Protobuf.ImportConfig(true, JList.of(), JList.of()),
    null,
    new Protobuf.SourceSetConfig(true, JList.of(), JList.of("**/*.proto"), JList.of(), JList.of(), JList.of(), JList.of(), JMap.of()),
    new Protobuf.SourceSetConfig(true, JList.of(), JList.of("**/*.proto"), JList.of(), JList.of(), JList.of(), JList.of(), JMap.of())
  )

  private def constantTask[T: JsonRW: Hashable](name: String, value: T): AbstractTask[T] =
    TaskBuilder.make[T](name).build(_ => value)
}
