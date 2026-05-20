package ba.sake.deder.protobuf.protoc

import munit.FunSuite

class BinaryMavenDependencySuite extends FunSuite {

  test("native binary dependencies preserve classifier and executable type") {
    val dependency = BinaryMavenDependency.toCoursierDependency(
      declaration = "io.grpc:protoc-gen-grpc-java:1.75.0",
      scalaVersion = "3.7.4",
      classifier = PlatformClassifier("linux-x86_64", "exe")
    )

    assertEquals(dependency.getModule.getOrganization, "io.grpc")
    assertEquals(dependency.getModule.getName, "protoc-gen-grpc-java")
    assertEquals(dependency.getVersion, "1.75.0")
    assertEquals(dependency.getClassifier, "linux-x86_64")
    assertEquals(dependency.getType, "exe")
  }

  test("binary fetch requests the executable artifact type and classifier") {
    val classifier = PlatformClassifier("linux-x86_64", "exe")
    val dependency = BinaryMavenDependency.toCoursierDependency(
      declaration = "io.grpc:protoc-gen-grpc-java:1.75.0",
      scalaVersion = "3.7.4",
      classifier = classifier
    )

    val fetch = BinaryMavenDependency.newFetch(dependency, classifier)

    assert(fetch.getClassifiers.contains("linux-x86_64"), fetch.getClassifiers)
    assert(fetch.getArtifactTypes.contains("exe"), fetch.getArtifactTypes)
  }
}
