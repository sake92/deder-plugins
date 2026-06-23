package ba.sake.deder.tuidashboard.app

import ba.sake.deder.tuidashboard.model.*
import ba.sake.tupson.parseJson
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

  // --- Tasks tab tests ---

  test("parse ApiExecEntry JSON") {
    val json = """[{"execId":"e1","taskName":"compile","moduleIds":["core"],"startTimeMs":100,"endTimeMs":200,"status":"SUCCESS","output":"done","error":null,"requestId":"r1"}]"""
    val parsed = json.parseJson[Seq[ApiExecEntry]]
    assertEquals(parsed.length, 1)
    assertEquals(parsed(0).execId, "e1")
    assertEquals(parsed(0).taskName, "compile")
    assertEquals(parsed(0).status, "SUCCESS")
    assertEquals(parsed(0).requestId, Some("r1"))
  }

  test("parse TaskRunResult JSON") {
    val json = """{"execId":"e1","status":"PENDING","taskName":"compile","error":null}"""
    val parsed = json.parseJson[TaskRunResult]
    assertEquals(parsed.execId, Some("e1"))
    assertEquals(parsed.status, Some("PENDING"))
  }

  test("parse CancelResult JSON") {
    val json = """{"cancelled":true}"""
    val parsed = json.parseJson[CancelResult]
    assertEquals(parsed.cancelled, true)
  }

  test("TaskNameInput inserts character at cursor position") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(taskInput = "com", cursorPos = 2)
    val (newState, _) = app.update(TaskNameInput('p'), state)
    assertEquals(newState.taskInput, "copm")
    assertEquals(newState.cursorPos, 3)
  }

  test("TaskNameBackspace deletes character before cursor") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(taskInput = "compile", cursorPos = 4)
    val (newState, _) = app.update(TaskNameBackspace, state)
    assertEquals(newState.taskInput, "comile")
    assertEquals(newState.cursorPos, 3)
  }

  test("TaskNameBackspace does nothing when cursor at 0") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(taskInput = "compile", cursorPos = 0)
    val (newState, _) = app.update(TaskNameBackspace, state)
    assertEquals(newState.taskInput, "compile")
  }

  test("TaskNameClear resets input and selected task") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(taskInput = "compile", cursorPos = 7, selectedTaskName = Some("compile"))
    val (newState, _) = app.update(TaskNameClear, state)
    assertEquals(newState.taskInput, "")
    assertEquals(newState.cursorPos, 0)
    assertEquals(newState.selectedTaskName, None)
  }

  test("TaskNameConfirm selects first matching task from server info") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(
      taskInput = "com",
      cursorPos = 3,
      serverInfo = Some(ApiServerInfo(
        dederVersion = "0.1", jdkVersion = "21", jdkVendor = "Eclipse",
        osName = "Linux", osArch = "amd64", processors = 8,
        maxHeapMB = 4096L, usedHeapMB = 512L, uptimeSecs = 3600L,
        moduleCount = 5, pluginCount = 1, projectRoot = "/tmp",
        plugins = Seq(ApiPluginInfo("core", 3, Seq("compile", "test", "run")))
      ))
    )
    val (newState, _) = app.update(TaskNameConfirm, state)
    assertEquals(newState.selectedTaskName, Some("compile"))
    assertEquals(newState.taskInput, "compile")
  }

  test("TaskNameConfirm does nothing when no tasks match") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(
      taskInput = "xyz",
      selectedTaskName = None,
      serverInfo = Some(ApiServerInfo(
        dederVersion = "0.1", jdkVersion = "21", jdkVendor = "Eclipse",
        osName = "Linux", osArch = "amd64", processors = 8,
        maxHeapMB = 4096L, usedHeapMB = 512L, uptimeSecs = 3600L,
        moduleCount = 5, pluginCount = 1, projectRoot = "/tmp",
        plugins = Seq(ApiPluginInfo("core", 3, Seq("compile", "test", "run")))
      ))
    )
    val (newState, _) = app.update(TaskNameConfirm, state)
    assertEquals(newState.selectedTaskName, None)
  }

  test("FocusNext cycles between TaskInput, ModuleList, and ExecLog") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(focus = FocusField.TaskInput)
    val (s1, _) = app.update(FocusNext, state)
    assertEquals(s1.focus, FocusField.ModuleList)
    val (s2, _) = app.update(FocusNext, s1)
    assertEquals(s2.focus, FocusField.ExecLog)
    val (s3, _) = app.update(FocusNext, s2)
    assertEquals(s3.focus, FocusField.TaskInput)
  }

  test("ToggleModule adds and removes module IDs") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(toggledModuleIds = Set("a"))
    val (s1, _) = app.update(ToggleModule("a"), state)
    assertEquals(s1.toggledModuleIds, Set.empty)
    val (s2, _) = app.update(ToggleModule("b"), s1)
    assertEquals(s2.toggledModuleIds, Set("b"))
  }

  test("SelectAllModules selects all module IDs") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(modules = Seq(
      ApiModule("core", "SCALA", 2),
      ApiModule("api", "JAVA", 1)
    ))
    val (newState, _) = app.update(SelectAllModules, state)
    assertEquals(newState.toggledModuleIds, Set("core", "api"))
  }

  test("DeselectAllModules clears selection") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(toggledModuleIds = Set("core", "api"))
    val (newState, _) = app.update(DeselectAllModules, state)
    assertEquals(newState.toggledModuleIds, Set.empty)
  }

  test("ExpandExec toggles expansion") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(expandedExecId = None)
    val (s1, _) = app.update(ExpandExec("e1"), state)
    assertEquals(s1.expandedExecId, Some("e1"))
    val (s2, _) = app.update(ExpandExec("e1"), s1)
    assertEquals(s2.expandedExecId, None)
  }

  test("ModuleUp decreases index, clamped at 0") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(selectedModuleIdx = 2,
      modules = Seq(ApiModule("a", "SCALA", 0), ApiModule("b", "SCALA", 0), ApiModule("c", "SCALA", 0)))
    val (s1, _) = app.update(ModuleUp, state)
    assertEquals(s1.selectedModuleIdx, 1)
    val (s2, _) = app.update(ModuleUp, DashboardState(selectedModuleIdx = 0, modules = Seq()))
    assertEquals(s2.selectedModuleIdx, 0)
  }

  test("ModuleDown increases index, clamped at module count - 1") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(selectedModuleIdx = 0,
      modules = Seq(ApiModule("a", "SCALA", 0), ApiModule("b", "SCALA", 0), ApiModule("c", "SCALA", 0)))
    val (s1, _) = app.update(ModuleDown, state)
    assertEquals(s1.selectedModuleIdx, 1)
    val (s2, _) = app.update(ModuleDown, s1)
    assertEquals(s2.selectedModuleIdx, 2)
    val (s3, _) = app.update(ModuleDown, s2)
    assertEquals(s3.selectedModuleIdx, 2)
  }

  test("ExecUp decreases exec index, clamped at 0") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(selectedExecIdx = 2,
      taskExecutions = Seq(
        ApiExecEntry("e1", "compile", Seq.empty, 1L, None, "SUCCESS", "", None, None),
        ApiExecEntry("e2", "test", Seq.empty, 2L, None, "SUCCESS", "", None, None),
        ApiExecEntry("e3", "run", Seq.empty, 3L, None, "SUCCESS", "", None, None)
      ))
    val (s1, _) = app.update(ExecUp, state)
    assertEquals(s1.selectedExecIdx, 1)
  }

  test("ExecDown increases exec index, clamped at exec count - 1") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(taskExecutions = Seq(
      ApiExecEntry("e1", "compile", Seq.empty, 1L, None, "SUCCESS", "", None, None),
      ApiExecEntry("e2", "test", Seq.empty, 2L, None, "SUCCESS", "", None, None)
    ))
    val (s1, _) = app.update(ExecDown, state)
    assertEquals(s1.selectedExecIdx, 1)
    val (s2, _) = app.update(ExecDown, s1)
    assertEquals(s2.selectedExecIdx, 1)
  }

  test("ToggleCurrentModule toggles module at selected index") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(selectedModuleIdx = 1, toggledModuleIds = Set("a"),
      modules = Seq(ApiModule("a", "SCALA", 0), ApiModule("b", "SCALA", 0)))
    val (s1, _) = app.update(ToggleCurrentModule, state)
    assertEquals(s1.toggledModuleIds, Set("a", "b"))
    val (s2, _) = app.update(ToggleCurrentModule, s1)
    assertEquals(s2.toggledModuleIds, Set("a"))
  }

  test("TaskExecsResp parses execution entries") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState()
    val json = """[{"execId":"e1","taskName":"compile","moduleIds":[],"startTimeMs":100,"endTimeMs":200,"status":"SUCCESS","output":"ok","error":null}]"""
    val (newState, _) = app.update(TaskExecsResp(Right(json)), state)
    assertEquals(newState.taskExecutions.length, 1)
    assertEquals(newState.taskExecutions(0).execId, "e1")
    assertEquals(newState.taskExecutions(0).status, "SUCCESS")
    assertEquals(newState.lastError, None)
  }

  test("CursorLeft moves cursor left, clamped at 0") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(taskInput = "abc", cursorPos = 2)
    val (s1, _) = app.update(CursorLeft, state)
    assertEquals(s1.cursorPos, 1)
    val (s2, _) = app.update(CursorLeft, s1)
    assertEquals(s2.cursorPos, 0)
    val (s3, _) = app.update(CursorLeft, s2)
    assertEquals(s3.cursorPos, 0)
  }

  test("CursorRight moves cursor right, clamped at length") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(taskInput = "ab", cursorPos = 0)
    val (s1, _) = app.update(CursorRight, state)
    assertEquals(s1.cursorPos, 1)
    val (s2, _) = app.update(CursorRight, s1)
    assertEquals(s2.cursorPos, 2)
    val (s3, _) = app.update(CursorRight, s2)
    assertEquals(s3.cursorPos, 2)
  }

  test("CursorHome moves cursor to 0") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(taskInput = "abc", cursorPos = 2)
    val (newState, _) = app.update(CursorHome, state)
    assertEquals(newState.cursorPos, 0)
  }

  test("CursorEnd moves cursor to end of input") {
    val app = DashboardApp("http://localhost:9292", 1000)
    val state = DashboardState(taskInput = "abc", cursorPos = 1)
    val (newState, _) = app.update(CursorEnd, state)
    assertEquals(newState.cursorPos, 3)
  }

  test("default task input is compile, pre-selected") {
    val state = DashboardState()
    assertEquals(state.taskInput, "compile")
    assertEquals(state.cursorPos, 7)
    assertEquals(state.selectedTaskName, Some("compile"))
  }
}
