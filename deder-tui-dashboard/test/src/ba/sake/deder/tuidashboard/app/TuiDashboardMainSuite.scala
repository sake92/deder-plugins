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

  test("DashboardData construction") {
    val data = DashboardData(
      modules = Seq(ApiModule("foo", "SCALA", 3)),
      overview = Some(StatsOverview(100, 5, 3600)),
      currentRequests = Seq.empty,
      history = Seq.empty
    )
    assertEquals(data.modules.length, 1)
    assertEquals(data.overview.get.totalRequestsServed, 100L)
    assertEquals(data.currentRequests.length, 0)
  }

  test("DashboardData with empty overview") {
    val data = DashboardData(
      modules = Seq.empty,
      overview = None,
      currentRequests = Seq.empty,
      history = Seq.empty
    )
    assertEquals(data.overview, None)
  }

  test("ApiModule type field is properly named with backticks") {
    val m = ApiModule("test", "JAVA", 0)
    assertEquals(m.`type`, "JAVA")
  }
}
