package ba.sake.deder.protobuf.config

import ba.sake.deder.plugins.Protobuf
import munit.FunSuite

import java.util.{List as JList, Map as JMap}

class ProtobufConfigNormalizerSuite extends FunSuite {

  test("default-like test modules fall back to test proto directories") {
    val defaults = new Protobuf.ModuleDefaults(
      true,
      new Protobuf.ResolverConfig(Protobuf.ResolverKind.SYSTEM_PATH, "protoc", null, null, null, JList.of(), null, null, null, null, false),
      JList.of(),
      JList.of(),
      new Protobuf.ImportConfig(true, JList.of(), JList.of()),
      null,
      new Protobuf.SourceSetConfig(true, JList.of(), JList.of("**/*.proto"), JList.of(), JList.of(), JList.of(), JList.of(), JMap.of()),
      new Protobuf.SourceSetConfig(true, JList.of(), JList.of("**/*.proto"), JList.of(), JList.of(), JList.of(), JList.of(), JMap.of())
    )
    val config = new Protobuf.ProtobufPluginConfig(defaults, JMap.of())

    val resolved = ProtobufConfigNormalizer.normalize(
      moduleId = "consumer-test",
      sources = Seq("test/src"),
      resources = Seq("test/resources"),
      sourceSetKind = SourceSetKind.Test,
      config = config
    )

    assertEquals(resolved.sourceSet.sourceDirs, Seq("test/protobuf", "test/proto"))
  }

  test("module overrides win over defaults for test source sets") {
    val defaults = new Protobuf.ModuleDefaults(
      true,
      new Protobuf.ResolverConfig(Protobuf.ResolverKind.SYSTEM_PATH, "protoc", null, null, null, JList.of(), null, null, null, null, false),
      JList.of(new Protobuf.BuiltinLanguageTarget("java", null, null)),
      JList.of(),
      new Protobuf.ImportConfig(true, JList.of(), JList.of()),
      null,
      new Protobuf.SourceSetConfig(true, JList.of(), JList.of("**/*.proto"), JList.of(), JList.of(), JList.of(), JList.of(), JMap.of()),
      new Protobuf.SourceSetConfig(true, JList.of(), JList.of("**/*.proto"), JList.of(), JList.of(), JList.of(), JList.of(), JMap.of())
    )
    val testOverride = new Protobuf.SourceSetOverride(
      true,
      JList.of("custom/test-proto"),
      null,
      null,
      null,
      null,
      null,
      null
    )
    val moduleOverride = new Protobuf.ModuleOverride(
      true,
      null,
      null,
      null,
      null,
      null,
      null,
      testOverride
    )
    val config = new Protobuf.ProtobufPluginConfig(defaults, JMap.of("consumer-test", moduleOverride))

    val resolved = ProtobufConfigNormalizer.normalize(
      moduleId = "consumer-test",
      sources = Seq("test/src"),
      resources = Seq("test/resources"),
      sourceSetKind = SourceSetKind.Test,
      config = config
    )

    assertEquals(resolved.sourceSet.sourceDirs, Seq("custom/test-proto"))
    assertEquals(resolved.builtins.map(_.name), Seq("java"))
  }

  test("plugin resolvers default system-path commands to protoc-gen-name") {
    val defaults = new Protobuf.ModuleDefaults(
      true,
      new Protobuf.ResolverConfig(Protobuf.ResolverKind.SYSTEM_PATH, "protoc", null, null, null, JList.of(), null, null, null, null, false),
      JList.of(),
      JList.of(
        new Protobuf.PluginTarget(
          "grpc-java",
          null,
          null,
          new Protobuf.ResolverConfig(Protobuf.ResolverKind.SYSTEM_PATH, null, null, null, null, JList.of(), null, null, null, null, false)
        )
      ),
      new Protobuf.ImportConfig(true, JList.of(), JList.of()),
      null,
      new Protobuf.SourceSetConfig(true, JList.of(), JList.of("**/*.proto"), JList.of(), JList.of(), JList.of(), JList.of(), JMap.of()),
      new Protobuf.SourceSetConfig(true, JList.of(), JList.of("**/*.proto"), JList.of(), JList.of(), JList.of(), JList.of(), JMap.of())
    )
    val config = new Protobuf.ProtobufPluginConfig(defaults, JMap.of())

    val resolved = ProtobufConfigNormalizer.normalize(
      moduleId = "consumer",
      sources = Seq("src"),
      resources = Seq("resources"),
      sourceSetKind = SourceSetKind.Main,
      config = config
    )

    assertEquals(resolved.plugins.map(_.resolver.command), Seq("protoc-gen-grpc-java"))
  }
}
