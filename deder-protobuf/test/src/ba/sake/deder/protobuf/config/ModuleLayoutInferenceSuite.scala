package ba.sake.deder.protobuf.config

import munit.FunSuite

class ModuleLayoutInferenceSuite extends FunSuite {

  test("maven-like sources select maven proto defaults") {
    val layout = ModuleLayoutInference.infer(
      sources = Seq("src/main/scala"),
      resources = Seq("src/main/resources")
    )

    assertEquals(layout, ModuleLayout.MavenLike)
    assertEquals(
      ModuleLayoutInference.defaultProtoSourceDirs(layout, SourceSetKind.Main),
      Seq("src/main/protobuf", "src/main/proto")
    )
    assertEquals(
      ModuleLayoutInference.defaultProtoSourceDirs(layout, SourceSetKind.Test),
      Seq("src/test/protobuf", "src/test/proto")
    )
  }

  test("root-level sources select default proto defaults") {
    val layout = ModuleLayoutInference.infer(
      sources = Seq("src"),
      resources = Seq("resources")
    )

    assertEquals(layout, ModuleLayout.DefaultLike)
    assertEquals(
      ModuleLayoutInference.defaultProtoSourceDirs(layout, SourceSetKind.Main),
      Seq("protobuf", "proto")
    )
    assertEquals(
      ModuleLayoutInference.defaultProtoSourceDirs(layout, SourceSetKind.Test),
      Seq("test/protobuf", "test/proto")
    )
  }

  test("plain-layout test sources still select default proto defaults") {
    val layout = ModuleLayoutInference.infer(
      sources = Seq("test/src"),
      resources = Seq("test/resources")
    )

    assertEquals(layout, ModuleLayout.DefaultLike)
    assertEquals(
      ModuleLayoutInference.defaultProtoSourceDirs(layout, SourceSetKind.Test),
      Seq("test/protobuf", "test/proto")
    )
  }
}
