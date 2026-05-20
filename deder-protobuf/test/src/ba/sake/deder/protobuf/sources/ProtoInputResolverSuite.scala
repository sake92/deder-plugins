package ba.sake.deder.protobuf.sources

import ba.sake.deder.protobuf.config.{ResolvedImportConfig, ResolvedSourceSetConfig}
import munit.FunSuite

class ProtoInputResolverSuite extends FunSuite {

  test("declared input files include local import path protos") {
    val moduleRoot = os.temp.dir(prefix = "protobuf-inputs")
    os.makeDir.all(moduleRoot / "protobuf")
    os.makeDir.all(moduleRoot / "shared")
    os.write.over(moduleRoot / "protobuf" / "service.proto", "syntax = \"proto3\";")
    os.write.over(moduleRoot / "shared" / "common.proto", "syntax = \"proto3\";")

    val sourceSet = ResolvedSourceSetConfig(
      enabled = true,
      sourceDirs = Seq("protobuf"),
      includeGlobs = Seq("**/*.proto"),
      excludeGlobs = Seq.empty,
      extraImportPaths = Seq("shared"),
      extraImportDeps = Seq.empty,
      extraArgs = Seq.empty,
      env = Map.empty
    )
    val imports = ResolvedImportConfig(
      includeProjectDependencies = false,
      dependencyArtifacts = Seq.empty,
      paths = Seq.empty
    )

    val inputs = ProtoInputResolver.declaredInputFiles(moduleRoot, sourceSet, imports)

    assertEquals(
      inputs.map(_.toString).sorted,
      Seq(
        (moduleRoot / "protobuf" / "service.proto").toString,
        (moduleRoot / "shared" / "common.proto").toString
      ).sorted
    )
  }
}
