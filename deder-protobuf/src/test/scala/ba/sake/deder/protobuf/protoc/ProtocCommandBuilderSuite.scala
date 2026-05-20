package ba.sake.deder.protobuf.protoc

import munit.FunSuite

class ProtocCommandBuilderSuite extends FunSuite {

  test("command builder includes imports builtins plugins descriptor and args") {
    val request = ProtocInvocationRequest(
      executable = ResolvedExecutable.command("protoc"),
      protoFiles = Seq("src/main/protobuf/example/greeter/service.proto"),
      importRoots = Seq("/imports/project", "/imports/deps"),
      sourceOutDir = "/tmp/out/sources",
      builtins = Seq(ResolvedBuiltinTarget("java", Some("lite"), None)),
      plugins = Seq(
        ResolvedPluginTarget(
          name = "grpc-java",
          options = Some("lite"),
          outputDir = None,
          executable = ResolvedExecutable.command("/tmp/bin/protoc-gen-grpc-java")
        )
      ),
      descriptor = Some(ResolvedDescriptorConfig("/tmp/out/resources/descriptor-set.pb", includeImports = true, includeSourceInfo = false)),
      extraArgs = Seq("--experimental_allow_proto3_optional")
    )

    val command = ProtocCommandBuilder.build(request)

    assertEquals(
      command,
      Seq(
        "protoc",
        "-I=/imports/project",
        "-I=/imports/deps",
        "--java_out=lite:/tmp/out/sources",
        "--plugin=protoc-gen-grpc-java=/tmp/bin/protoc-gen-grpc-java",
        "--grpc-java_out=lite:/tmp/out/sources",
        "--descriptor_set_out=/tmp/out/resources/descriptor-set.pb",
        "--include_imports",
        "--experimental_allow_proto3_optional",
        "src/main/protobuf/example/greeter/service.proto"
      )
    )
  }
}
