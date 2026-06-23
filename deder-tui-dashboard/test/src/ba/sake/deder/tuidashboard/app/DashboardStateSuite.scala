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
    val json = """[{"requestId":"r1","caller":"CLI","taskName":"compile","moduleIds":["a"],"startTimeMs":1,"durationMs":100,"success":true},{"requestId":"r2","caller":"BSP","taskName":"test","moduleIds":[],"startTimeMs":2,"durationMs":50,"success":false}]"""
    val (newState, _) = app.update(HistoryResp(Right(json)), state)
    assertEquals(newState.history.length, 2)
    assertEquals(newState.history(0).caller, "CLI")
    assertEquals(newState.history(0).success, true)
    assertEquals(newState.history(1).caller, "BSP")
    assertEquals(newState.history(1).success, false)
  }

  test("update parses task stats JSON and sorts by total time desc") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState()
    val json = """[{"taskName":"compile","invocations":10,"errors":2,"totalTimeMs":5000,"avgTimeMs":500,"minTimeMs":100,"maxTimeMs":1200,"longestModuleId":"core"},{"taskName":"test","invocations":5,"errors":0,"totalTimeMs":1000,"avgTimeMs":200,"minTimeMs":50,"maxTimeMs":400,"longestModuleId":"core-test"}]"""
    val (newState, _) = app.update(TaskStatsResp(Right(json)), state)
    assertEquals(newState.taskStats.length, 2)
    assertEquals(newState.taskStats(0).taskName, "compile")
    assertEquals(newState.taskStats(1).taskName, "test")
    assertEquals(newState.lastError, None)
  }

  test("update sets error on bad task stats JSON") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState()
    val (newState, _) = app.update(TaskStatsResp(Right("not json")), state)
    assert(newState.lastError.isDefined)
    assertEquals(newState.taskStats.length, 0)
  }

  test("update parses errors JSON") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState()
    val json = """[{"taskName":"compile","moduleIds":["core"],"errorCount":3}]"""
    val (newState, _) = app.update(ErrorsResp(Right(json)), state)
    assertEquals(newState.errors.length, 1)
    assertEquals(newState.errors(0).taskName, "compile")
    assertEquals(newState.errors(0).errorCount, 3L)
    assertEquals(newState.lastError, None)
  }

  test("update parses server info JSON") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState()
    val json = """{"dederVersion":"0.18.0","jdkVersion":"21","jdkVendor":"Eclipse","osName":"Linux","osArch":"amd64","processors":8,"maxHeapMB":4096,"usedHeapMB":512,"uptimeSecs":3600,"moduleCount":5,"pluginCount":2,"projectRoot":"/home/project","plugins":[{"id":"web-dashboard","taskCount":0,"taskNames":[]}]}"""
    val (newState, _) = app.update(ServerInfoResp(Right(json)), state)
    assert(newState.serverInfo.isDefined)
    assertEquals(newState.serverInfo.get.dederVersion, "0.18.0")
    assertEquals(newState.serverInfo.get.projectRoot, "/home/project")
    assertEquals(newState.lastError, None)
  }

  test("ChangeTab switches active tab") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState()
    val (newState, _) = app.update(ChangeTab(Tab.Live), state)
    assertEquals(newState.activeTab, Tab.Live)
    assertEquals(newState.lastError, None)
  }

  test("ToggleSort cycles sort order") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState()

    val (s1, _) = app.update(ToggleSort, state)
    assertEquals(s1.historySort, SortOrder.Oldest)

    val (s2, _) = app.update(ToggleSort, s1)
    assertEquals(s2.historySort, SortOrder.Longest)

    val (s3, _) = app.update(ToggleSort, s2)
    assertEquals(s3.historySort, SortOrder.Shortest)

    val (s4, _) = app.update(ToggleSort, s3)
    assertEquals(s4.historySort, SortOrder.Newest)
  }

  test("update parses request-statuses JSON with state, lock and task progress") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState()
    val json = """[{"requestId":"req-q1","caller":"CLI","taskName":"compile","moduleIds":["core"],"startTimeMs":1,"state":"Queued","lockProgress":null,"taskProgress":null},{"requestId":"req-l1","caller":"BSP","taskName":"test","moduleIds":["core-test"],"startTimeMs":2,"state":"AcquiringLocks","lockProgress":{"acquired":1,"total":3,"blockingOn":"core.compile","heldBy":"req-042"},"taskProgress":null}]"""
    val (newState, _) = app.update(CurrentResp(Right(json)), state)
    assertEquals(newState.currentRequests.length, 2)
    val r1 = newState.currentRequests(0)
    assertEquals(r1.requestId, "req-q1")
    assertEquals(r1.state, ApiRequestState.Queued)
    assertEquals(r1.lockProgress, None)
    assertEquals(r1.taskProgress, None)
    val r2 = newState.currentRequests(1)
    assertEquals(r2.requestId, "req-l1")
    assertEquals(r2.state, ApiRequestState.AcquiringLocks)
    assert(r2.lockProgress.isDefined)
    assertEquals(r2.lockProgress.get.acquired, 1)
    assertEquals(r2.lockProgress.get.total, 3)
    assertEquals(r2.taskProgress, None)
    assertEquals(newState.lastError, None)
  }
}
