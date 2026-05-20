package ba.sake.deder.protobuf.sources

import munit.FunSuite

class GlobFilterSuite extends FunSuite {

  test("include and exclude globs are applied to relative proto paths") {
    val filtered = GlobFilter.filter(
      relativePaths = Seq(
        "example/greeter/service.proto",
        "example/internal/skip.proto",
        "notes.txt"
      ),
      includeGlobs = Seq("**/*.proto"),
      excludeGlobs = Seq("**/internal/**")
    )

    assertEquals(filtered, Seq("example/greeter/service.proto"))
  }
}
