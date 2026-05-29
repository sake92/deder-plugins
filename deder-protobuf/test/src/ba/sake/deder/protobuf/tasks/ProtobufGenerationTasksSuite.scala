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
    val sourceGenerator = ProtobufGenerationTasks.sourceGeneratorTask(config, protoSourceFiles, stubCoreTasks)
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

  private val stubCoreTasks = new CoreTasksApi {
    override val sourcesTask: AbstractTask[Seq[DederPath]] = constantTask("sources", Seq.empty)
    override val sourceFilesTask: AbstractTask[Seq[DederPath]] = constantTask("sourceFiles", Seq.empty)
    override val resourcesTask: AbstractTask[Seq[DederPath]] = constantTask("resources", Seq.empty)
    override val classesTask: AbstractTask[DederPath] = constantTask("classes", DederPath(os.root))
    override val allClassesDirsTask: AbstractTask[Seq[DederPath]] = constantTask("allClassesDirs", Seq.empty)
    override val compileTask: AbstractTask[DederPath] = constantTask("compile", DederPath(os.SubPath("compile")))
    override val allDependenciesTask: AbstractTask[Seq[Dependency]] = constantTask("allDependencies", Seq.empty)
    override val compileClasspathTask: AbstractTask[Seq[os.Path]] = constantTask("compileClasspath", Seq.empty)
  }

  private def constantTask[T: JsonRW: Hashable](name: String, value: T): AbstractTask[T] =
    TaskBuilder.make[T](name).build(_ => value)
}
