package ba.sake.deder.webdashboard.server

import java.time.{Duration, Instant}
import ba.sake.deder.*
import ba.sake.deder.config.DederProject
import ba.sake.deder.plugins.WebDashboard.WebDashboardPluginConfig
import ba.sake.tupson.*
import sttp.client4.quick.*
import ba.sake.deder.webdashboard.*
import ba.sake.deder.webdashboard.server.stubs.*
import munit.FunSuite

class DashboardServerSuite extends FunSuite {

  private val testHost = "localhost"
  private val testPort = 19999
  private val testRefreshMs = 5000

  private def now: Instant = Instant.now()

  private lazy val stubInternals: DederProjectInternals = StubInternals(
    currentRequests = Seq(LiveRequest("req-001", CallerType.Cli, "compile", Seq("my-module"), now)),
    recentHistory = Seq(
      CompletedRequest("req-000", CallerType.Cli, "compile", Seq("my-module"), now.minusSeconds(10), Duration.ofSeconds(5), true, None),
      CompletedRequest("req-001", CallerType.Bsp, "test", Seq("core-test"), now.minusSeconds(20), Duration.ofSeconds(12), false, Some("test failed")),
      CompletedRequest("req-002", CallerType.Cli, "compile", Seq("core"), now.minusSeconds(30), Duration.ofSeconds(2), true, None),
      CompletedRequest("req-003", CallerType.Cli, "compile", Seq("api"), now.minusSeconds(40), Duration.ofMillis(800), true, None),
      CompletedRequest("req-004", CallerType.Bsp, "compile", Seq("core", "api"), now.minusSeconds(50), Duration.ofSeconds(45), true, None),
      CompletedRequest("req-005", CallerType.Cli, "test", Seq("core-test"), now.minusSeconds(60), Duration.ofSeconds(1), false, Some("assertion error")),
    ),
    totalRequestsServed = 100L,
    totalErrors = 5L,
    serverUptime = Duration.ofSeconds(8130L),
    loadedPlugins = Seq(
      LoadedPluginInfo("web-dashboard", Seq()),
      LoadedPluginInfo("core", Seq("compile", "test", "run", "jar", "publishLocal"))
    ),
    cancelFn = _ != "nonexistent",
    allRequestStatuses = Seq(
      RequestStatus("req-q1", CallerType.Cli, "compile", Seq("core"), now,
        RequestState.QUEUED, None, None),
      RequestStatus("req-l1", CallerType.Bsp, "test", Seq("core-test"), now.minusSeconds(5),
        RequestState.ACQUIRING_LOCKS,
        Some(LockProgress(1, 3, Some("core.compile"), Some("req-042"))),
        None),
      RequestStatus("req-e1", CallerType.Cli, "compile", Seq("app"), now.minusSeconds(10),
        RequestState.EXECUTING, None,
        Some(TaskStageProgress(2, 5, Seq("a", "b"), Seq.empty, Seq.empty, Seq("c", "d"), Seq("e", "f", "g")))),
      RequestStatus("req-e2", CallerType.Bsp, "run", Seq("app"), now.minusSeconds(20),
        RequestState.EXECUTING, None, None)
    ),
  )

  private lazy val stubProject: DederProject =
    new DederProject(
      java.util.List.of(),
      java.util.List.of(),
      java.util.List.of(),
      true,
      java.util.List.of(),
      true,
      1L,
      1L,
      java.util.Map.of()
    )

  private val config = WebDashboardPluginConfig(true, testHost, testPort.toLong, testRefreshMs.toLong, 3L, 200L, 500L)

  private lazy val stubTaskInvoker: TaskInvokerApi = StubTaskInvoker { (taskName, moduleIds, _, onNotification) =>
    onNotification(ServerNotification.Output(s"Running $taskName..."))
    val outcomes = moduleIds.map { m =>
      TaskInvokeOutcome(m, success = true, None, fromCache = false)
    }
    TaskInvokeResult(outcomes, None, None)
  }

  private lazy val stubTaskRegistry: TasksRegistryApi = StubTaskRegistry(tasks = Seq(
    TaskInfo("compile", "Compile Scala/Java sources", "Build", TaskKind.Standard, Seq.empty, false, false, false, Seq.empty),
    TaskInfo("test", "Run tests", "Test", TaskKind.Standard, Seq.empty, false, false, false, Seq.empty),
    TaskInfo("run", "Run main class", "Run", TaskKind.Standard, Seq.empty, false, false, false, Seq.empty),
  ))

  val dashboardService = new DashboardService(stubInternals, stubTaskRegistry)
  val executionLog = TaskExecutionLog(config.tasksMaxHistory.toInt)
  val taskRunner = TaskRunner(
    stubProject,
    stubTaskInvoker,
    stubInternals,
    executionLog,
    config.tasksMaxConcurrent.toInt,
    stubTaskRegistry
  )
  val apiRoutes = new ApiRoutes(dashboardService, stubProject, stubInternals, executionLog, taskRunner)
  val htmlRoutes =
    new HtmlRoutes(config, dashboardService, stubProject, stubInternals, executionLog, taskRunner)
  val server = DashboardServer(config, apiRoutes, htmlRoutes)
  private val baseUrl = s"http://$testHost:$testPort"
  private val projectRootProperty = "DEDER_PROJECT_ROOT_DIR"
  private var previousProjectRoot: Option[String] = None

  override def beforeAll(): Unit = {
    previousProjectRoot = Option(System.getProperty(projectRootProperty))
    System.setProperty(projectRootProperty, os.pwd.toString)
    server.start()
    Thread.sleep(500)
  }

  override def afterAll(): Unit = {
    server.stop()
    previousProjectRoot match {
      case Some(value) => System.setProperty(projectRootProperty, value)
      case None        => System.clearProperty(projectRootProperty)
    }
  }

  test("GET / redirects to /server") {
    val res = quickRequest.get(uri"$baseUrl/").followRedirects(false).send()
    assertEquals(res.code.code, 301)
  }

  test("GET /modules returns HTML page") {
    val res = quickRequest.get(uri"$baseUrl/modules").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("Modules"), s"body should contain 'Modules', got: ${res.body.take(300)}")
  }

  test("GET /modules/graph returns HTML page") {
    val res = quickRequest.get(uri"$baseUrl/modules/graph").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("Modules graph"), s"body should contain 'Modules graph', got: ${res.body.take(300)}")
  }

  test("GET /server returns HTML page with server info") {
    val res = quickRequest.get(uri"$baseUrl/server").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("Deder"), s"body should contain 'Deder', got: ${res.body.take(300)}")
    assert(res.body.contains("JDK"), s"body should contain 'JDK', got: ${res.body.take(300)}")
    assert(res.body.contains("OS"), s"body should contain 'OS', got: ${res.body.take(300)}")
  }

  // --- Live tab ---
  test("GET /live returns HTML page with Live tab") {
    val res = quickRequest.get(uri"$baseUrl/live").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("Live"), s"body should contain 'Live', got: ${res.body.take(300)}")
    assert(res.body.contains("Auto-refresh"), s"body should contain auto-refresh toggle, got: ${res.body.take(300)}")
    assert(res.body.contains("Requests"), s"body should contain 'Requests' section, got: ${res.body.take(300)}")
  }

  test("GET /stats/overview returns stat cards HTML fragment") {
    val res = quickRequest.get(uri"$baseUrl/stats/overview").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("100"), s"should contain total requests (100), got: ${res.body}")
    assert(res.body.contains("5"), s"should contain total errors (5), got: ${res.body}")
  }

  test("GET /stats/requests returns state-grouped request sections") {
    val res = quickRequest.get(uri"$baseUrl/stats/requests").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("Queued"), s"should contain Queued section, got: ${res.body}")
    assert(res.body.contains("Acquiring Locks"), s"should contain Acquiring Locks section, got: ${res.body}")
    assert(res.body.contains("Executing"), s"should contain Executing section, got: ${res.body}")
    assert(res.body.contains("Lock 1/3"), s"should contain lock progress, got: ${res.body}")
    assert(res.body.contains("Stage 2/5"), s"should contain stage progress, got: ${res.body}")
  }

  test("POST /stats/cancel with valid requestId returns cancelled badge") {
    val res = quickRequest.post(uri"$baseUrl/stats/cancel?requestId=req-q1").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("Cancelled"), s"should contain 'Cancelled', got: ${res.body}")
  }

  test("GET /stats/caches returns caches table HTML") {
    val res = quickRequest.get(uri"$baseUrl/stats/caches").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("No in-memory caches active"), s"should show empty caches message, got: ${res.body}")
  }

  test("POST /stats/caches/clear returns result summary and updated table") {
    val res = quickRequest.post(uri"$baseUrl/stats/caches/clear").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("No caches were active"), s"should contain 'No caches were active', got: ${res.body}")
    assert(res.body.contains("No in-memory caches active"), s"should show updated empty caches message, got: ${res.body}")
  }

  // --- History tab ---
  test("GET /history returns HTML page with History tab") {
    val res = quickRequest.get(uri"$baseUrl/history").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("History"), s"body should contain 'History', got: ${res.body.take(300)}")
    assert(res.body.contains("history-filters"), s"body should contain filter form, got: ${res.body.take(300)}")
  }

  test("GET /stats/history-table returns filtered history HTML table") {
    val res = quickRequest.get(uri"$baseUrl/stats/history-table?limit=50&offset=0").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("compile"), s"should contain task name 'compile', got: ${res.body}")
    assert(res.body.contains("Client"), s"should contain Client column header, got: ${res.body}")
    assert(res.body.contains("OK"), s"should contain success marker, got: ${res.body}")
  }

  test("GET /stats/history-table filters by status=success") {
    val res = quickRequest.get(uri"$baseUrl/stats/history-table?status=success&limit=50&offset=0").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("OK"), s"should contain OK, got: ${res.body}")
    assert(!res.body.contains("FAIL"), s"should not contain FAIL when filtering success, got: ${res.body}")
  }

  test("GET /stats/history-table filters by search=core") {
    val res = quickRequest.get(uri"$baseUrl/stats/history-table?search=core&limit=50&offset=0").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("core"), s"should contain 'core', got: ${res.body}")
  }

  test("GET /stats/history-table filters by caller=CLI") {
    val res = quickRequest.get(uri"$baseUrl/stats/history-table?caller=CLI&limit=50&offset=0").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("compile"), s"should contain results, got: ${res.body}")
  }

  // --- Stats tab ---
  test("GET /stats returns HTML page with Aggregates tab") {
    val res = quickRequest.get(uri"$baseUrl/stats").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("Aggregates"), s"body should contain 'Aggregates', got: ${res.body.take(300)}")
  }

  test("GET /stats/task-aggregates returns per-task stats HTML") {
    val res = quickRequest.get(uri"$baseUrl/stats/task-aggregates").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("compile"), s"should contain task 'compile', got: ${res.body}")
    assert(res.body.contains("test"), s"should contain task 'test', got: ${res.body}")
  }

  test("GET /stats/module-breakdown returns module rows for a task") {
    val res = quickRequest.get(uri"$baseUrl/stats/module-breakdown?task=compile&expanded=true").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("core"), s"should contain module 'core', got: ${res.body}")
    assert(res.body.contains("api"), s"should contain module 'api', got: ${res.body}")
  }

  test("GET /stats/top-offenders returns top tasks by time") {
    val res = quickRequest.get(uri"$baseUrl/stats/top-offenders?n=3").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("#1"), s"should contain '#1' ranking, got: ${res.body}")
  }

  test("GET /stats/module-aggregates returns heaviest modules HTML") {
    val res = quickRequest.get(uri"$baseUrl/stats/module-aggregates?n=3").send()
    assertEquals(res.code.code, 200)
    // core has 2+45=47s, api has 0.8+45=45.8s, core-test has 12+1=13s
    assert(res.body.contains("#1"), s"should contain '#1' ranking, got: ${res.body}")
    assert(res.body.contains("core"), s"should contain 'core' as heaviest, got: ${res.body}")
    assert(res.body.contains("api"), s"should contain 'api', got: ${res.body}")
  }

  test("GET /stats/error-summary returns error summary") {
    val res = quickRequest.get(uri"$baseUrl/stats/error-summary").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("test"), s"should contain task with errors, got: ${res.body}")
  }

  // --- JSON APIs ---
  test("GET /api/modules returns JSON") {
    val res = quickRequest.get(uri"$baseUrl/api/modules").send()
    assertEquals(res.code.code, 200)
    val modules = res.body.parseJson[Seq[ApiModule]]
    assertEquals(modules, Seq.empty)
  }

  test("GET /api/stats/overview returns JSON with totals") {
    val res = quickRequest.get(uri"$baseUrl/api/stats/overview").send()
    assertEquals(res.code.code, 200)
    val overview = res.body.parseJson[StatsOverview]
    assertEquals(overview.totalRequestsServed, 100L)
    assertEquals(overview.totalErrors, 5L)
    assertEquals(overview.uptimeSecs, 8130L)
  }

  test("GET /api/stats/history returns JSON with history entries") {
    val res = quickRequest.get(uri"$baseUrl/api/stats/history").send()
    assertEquals(res.code.code, 200)
    val history = res.body.parseJson[Seq[ApiHistoryEntry]]
    assert(history.exists(_.requestId == "req-000"), s"should contain req-000, got: ${res.body}")
    assert(history.exists(_.caller == "CLI"), s"should contain caller CLI, got: ${res.body}")
    assert(history.exists(_.success), s"should contain success:true, got: ${res.body}")
  }

  test("GET /api/stats/task-aggregates returns JSON") {
    val res = quickRequest.get(uri"$baseUrl/api/stats/task-aggregates").send()
    assertEquals(res.code.code, 200)
    val aggregates = res.body.parseJson[Seq[ApiTaskAggregate]]
    assert(aggregates.nonEmpty, s"should be non-empty, got: ${res.body}")
    assert(aggregates.exists(_.taskName == "compile"), s"should contain compile, got: ${res.body}")
    assert(aggregates.forall(_.invocations >= 0), s"should have invocations >= 0, got: ${res.body}")
  }

  test("GET /api/stats/module-breakdown returns JSON") {
    val res = quickRequest.get(uri"$baseUrl/api/stats/module-breakdown?task=compile").send()
    assertEquals(res.code.code, 200)
    val breakdown = res.body.parseJson[Seq[ApiModuleAggregate]]
    assert(breakdown.nonEmpty, s"should be non-empty, got: ${res.body}")
    assert(breakdown.head.moduleId.nonEmpty, s"should contain moduleId, got: ${res.body}")
  }

  test("GET /api/stats/error-summary returns JSON") {
    val res = quickRequest.get(uri"$baseUrl/api/stats/error-summary").send()
    assertEquals(res.code.code, 200)
    val summary = res.body.parseJson[Seq[ApiErrorSummaryEntry]]
    assert(summary.nonEmpty, s"should be a non-empty JSON array, got: ${res.body.take(200)}")
  }

  test("GET /api/stats/module-aggregates returns JSON with modules ranked by time") {
    val res = quickRequest.get(uri"$baseUrl/api/stats/module-aggregates?n=3").send()
    assertEquals(res.code.code, 200)
    val aggregates = res.body.parseJson[Seq[ApiModuleAggregate]]
    assert(aggregates.nonEmpty, s"should be non-empty, got: ${res.body}")
    assert(aggregates.head.moduleId == "core", s"should contain core as heaviest, got: ${res.body}")
    assert(aggregates.exists(_.moduleId == "api"), s"should contain api second, got: ${res.body}")
    assert(aggregates.head.totalTimeMs > 0, s"should contain totalTimeMs, got: ${res.body}")
  }

  test("GET /api/stats/request-statuses returns JSON with state field") {
    val res = quickRequest.get(uri"$baseUrl/api/stats/request-statuses").send()
    assertEquals(res.code.code, 200)
    val statuses = res.body.parseJson[Seq[ApiRequestStatus]]
    assert(statuses.exists(_.state == ApiRequestState.Queued), s"should contain Queued state, got: ${res.body}")
    assert(statuses.exists(_.state == ApiRequestState.AcquiringLocks), s"should contain AcquiringLocks state, got: ${res.body}")
    assert(statuses.exists(_.state == ApiRequestState.Executing), s"should contain Executing state, got: ${res.body}")
  }

  test("POST /api/cancel with valid requestId returns cancelled true") {
    val res = quickRequest.post(uri"$baseUrl/api/cancel?requestId=req-q1").send()
    assertEquals(res.code.code, 200)
    val result = res.body.parseJson[CancelResult]
    assertEquals(result.cancelled, true)
  }

  test("POST /api/cancel with invalid requestId returns cancelled false") {
    val res = quickRequest.post(uri"$baseUrl/api/cancel?requestId=nonexistent").send()
    assertEquals(res.code.code, 200)
    val result = res.body.parseJson[CancelResult]
    assertEquals(result.cancelled, false)
  }

  // --- Tasks tab tests ---
  test("GET /tasks returns HTML page with trigger form") {
    val res = quickRequest.get(uri"$baseUrl/tasks").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("Tasks"), s"body should contain 'Tasks', got: ${res.body.take(300)}")
    assert(res.body.contains("task-list"), s"body should contain task datalist, got: ${res.body.take(300)}")
    assert(res.body.contains("Run"), s"body should contain Run button, got: ${res.body.take(300)}")
  }

  test("GET /tasks/run with valid task returns log table") {
    val res = quickRequest.get(uri"$baseUrl/tasks/run?taskName=compile").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("compile"), s"should contain task name 'compile', got: ${res.body}")
  }

  test("GET /tasks/log-table returns log table HTML") {
    val res = quickRequest.get(uri"$baseUrl/tasks/log-table").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("<table"), s"should return a table, got: ${res.body.take(200)}")
  }

  test("GET /tasks/run initial response shows fallback for pending task") {
    val res = quickRequest.get(uri"$baseUrl/tasks/run?taskName=compile&moduleIds=mod1").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("No output captured yet"), s"should contain fallback message for pending task, got: ${res.body.take(500)}")
  }

  test("GET /tasks/log-table shows output and outcomes after task completes") {
    // trigger a task with moduleIds so the stub produces outcomes
    quickRequest.get(uri"$baseUrl/tasks/run?taskName=compile&moduleIds=mod1").send()
    Thread.sleep(600) // wait for stub to complete
    val res = quickRequest.get(uri"$baseUrl/tasks/log-table").send()
    assertEquals(res.code.code, 200)
    assert(res.body.contains("Running compile"), s"should contain task output, got: ${res.body.take(500)}")
    assert(res.body.contains("mod1"), s"should contain module 'mod1' in outcomes, got: ${res.body.take(500)}")
    assert(res.body.contains("OK"), s"should contain success status in outcomes, got: ${res.body.take(500)}")
    assert(!res.body.contains("No output captured yet"), s"should NOT contain fallback message after completion, got: ${res.body.take(500)}")
  }

  test("GET /api/tasks returns JSON array") {
    // trigger a task first
    quickRequest.get(uri"$baseUrl/tasks/run?taskName=compile").send()
    Thread.sleep(600) // wait for stub to complete
    val res = quickRequest.get(uri"$baseUrl/api/tasks").send()
    assertEquals(res.code.code, 200)
    val tasks = res.body.parseJson[Seq[ApiExecEntry]]
    assert(tasks.nonEmpty, s"should be non-empty JSON array, got: ${res.body.take(200)}")
    assert(tasks.exists(_.taskName == "compile"), s"should contain compile entry, got: ${res.body}")
  }

  test("GET /api/tasks/exec/:id/logs returns JSON for valid execId") {
    quickRequest.get(uri"$baseUrl/tasks/run?taskName=compile").send()
    Thread.sleep(600)
    val tasksRes = quickRequest.get(uri"$baseUrl/api/tasks").send()
    val tasks = tasksRes.body.parseJson[Seq[ApiExecEntry]]
    assert(tasks.nonEmpty, s"should have at least one task entry")
    val execId = tasks.head.execId

    val res = quickRequest.get(uri"$baseUrl/api/tasks/exec/$execId/logs").send()
    assertEquals(res.code.code, 200)
    val exec = res.body.parseJson[ApiExecEntry]
    assertEquals(exec.execId, execId)
    assertEquals(exec.taskName, "compile")
  }

  test("POST /api/tasks/exec with valid task returns execId JSON") {
    val res = quickRequest.post(uri"$baseUrl/api/tasks/exec?taskName=compile").send()
    assertEquals(res.code.code, 200)
    val result = res.body.parseJson[TaskRunResult]
    assert(result.execId.isDefined, s"should contain execId field, got: ${res.body}")
    assert(result.status.isDefined, s"should contain status field, got: ${res.body}")
  }

  test("POST /api/tasks/exec with unknown task returns error") {
    val res = quickRequest.post(uri"$baseUrl/api/tasks/exec?taskName=nonexistent19999").send()
    assertEquals(res.code.code, 200)
    val result = res.body.parseJson[TaskRunResult]
    assert(result.error.isDefined, s"should contain error field, got: ${res.body}")
  }

  test("GET /nonexistent returns 404") {
    val res = quickRequest.get(uri"$baseUrl/nonexistent").send()
    assertEquals(res.code.code, 404)
  }
}
