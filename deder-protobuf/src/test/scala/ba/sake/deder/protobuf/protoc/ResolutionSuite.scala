package ba.sake.deder.protobuf.protoc

import munit.FunSuite

class ResolutionSuite extends FunSuite {

  test("classifier detection handles linux x86_64") {
    val detected = PlatformClassifier.detect("Linux", "amd64")

    assertEquals(detected.classifier, "linux-x86_64")
    assertEquals(detected.binaryExtension, "exe")
  }

  test("classifier detection handles mac arm64") {
    val detected = PlatformClassifier.detect("Mac OS X", "aarch64")

    assertEquals(detected.classifier, "osx-aarch_64")
    assertEquals(detected.binaryExtension, "exe")
  }
}
