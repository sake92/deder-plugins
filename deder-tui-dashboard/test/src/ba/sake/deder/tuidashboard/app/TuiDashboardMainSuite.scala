package ba.sake.deder.tuidashboard.app

import ba.sake.deder.tuidashboard.model.*

import munit.FunSuite

class TuiDashboardMainSuite extends FunSuite {

  test("parseFlagValue returns default when flag not present") {
    val result = parseFlagValue(Seq("--other", "val"), "--server-url", "http://localhost:9292")
    assertEquals(result, "http://localhost:9292")
  }

  test("parseFlagValue returns value after flag") {
    val result = parseFlagValue(Seq("--server-url", "http://example.com:8080"), "--server-url", "http://localhost:9292")
    assertEquals(result, "http://example.com:8080")
  }

  test("parseFlagValue returns default when flag is last arg") {
    val result = parseFlagValue(Seq("--server-url"), "--server-url", "http://localhost:9292")
    assertEquals(result, "http://localhost:9292")
  }

  test("ApiModule type field is properly named with backticks") {
    val m = ApiModule("test", "JAVA", 0)
    assertEquals(m.`type`, "JAVA")
  }
}
