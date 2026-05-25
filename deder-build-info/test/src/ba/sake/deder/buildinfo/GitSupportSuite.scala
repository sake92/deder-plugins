package ba.sake.deder.buildinfo

import munit.FunSuite

class GitSupportSuite extends FunSuite {

  test("gitHead returns a non-empty string when in a git repo") {
    val result = GitSupport.gitHead(os.pwd)
    assert(result.nonEmpty, s"Expected non-empty git hash, got '$result'")
    assert(!result.startsWith("fatal:"), s"Expected git hash, got error: $result")
  }

  test("gitHead returns 'unknown' for non-existent directory") {
    val nonGitDir = os.temp.dir()
    val result = GitSupport.gitHead(nonGitDir)
    assertEquals(result, "unknown")
  }
}
