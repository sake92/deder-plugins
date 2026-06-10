package ba.sake.deder.tuidashboard.app

import ba.sake.deder.tuidashboard.model.*
import munit.FunSuite

class DashboardStateSuite extends FunSuite {

  test("update parses modules JSON and clears error") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(lastError = Some("old error"))
    val json = """[{"id":"foo","type":"SCALA","deps":3}]"""
    val (newState, _) = app.update(ModulesResp(Right(json)), state)
    assertEquals(newState.modules.length, 1)
    assertEquals(newState.modules(0).id, "foo")
    assertEquals(newState.lastError, None)
  }

  test("update sets error on bad modules JSON") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState()
    val (newState, _) = app.update(ModulesResp(Right("not json")), state)
    assert(newState.lastError.isDefined)
    assertEquals(newState.modules.length, 0)
  }

  test("update sets error on HTTP error") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState()
    val (newState, _) = app.update(OverviewResp(Left("connection refused")), state)
    assertEquals(newState.lastError, Some("overview: connection refused"))
  }

  test("update parses overview JSON") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState()
    val json = """{"totalRequestsServed":150,"totalErrors":2,"uptimeSecs":3600}"""
    val (newState, _) = app.update(OverviewResp(Right(json)), state)
    assert(newState.overview.isDefined)
    assertEquals(newState.overview.get.totalRequestsServed, 150L)
  }

  test("update parses history JSON") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState()
    val json = """[{"requestId":"r1","taskName":"compile","moduleIds":["a"],"startTimeMs":1,"durationMs":100,"success":true},{"requestId":"r2","taskName":"test","moduleIds":[],"startTimeMs":2,"durationMs":50,"success":false}]"""
    val (newState, _) = app.update(HistoryResp(Right(json)), state)
    assertEquals(newState.history.length, 2)
    assertEquals(newState.history(0).success, true)
    assertEquals(newState.history(1).success, false)
  }
}
