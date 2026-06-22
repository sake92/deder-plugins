package ba.sake.deder.webdashboard.server

import java.time.{Duration, Instant}
import java.net.{URL, HttpURLConnection}
import java.util.concurrent.TimeUnit
import ba.sake.deder.*
import ba.sake.deder.config.DederProject
import ba.sake.deder.plugins.WebDashboard.WebDashboardPluginConfig
import munit.FunSuite

class DashboardServerSuite extends FunSuite {

  private val testHost = "localhost"
  private val testPort = 19999
  private val testRefreshMs = 5000

  private def now: Instant = Instant.now()

  private def stubInternals: DederProjectInternals = new DederProjectInternals {
    def currentRequests: Seq[LiveRequest] =
      Seq(LiveRequest("req-001", CallerType.Cli, "compile", Seq("my-module"), now))
    def recentHistory: Seq[CompletedRequest] =
      Seq(
        CompletedRequest("req-000", CallerType.Cli, "compile", Seq("my-module"), now.minusSeconds(10), Duration.ofSeconds(5), true, None),
        CompletedRequest("req-001", CallerType.Bsp, "test", Seq("core-test"), now.minusSeconds(20), Duration.ofSeconds(12), false, Some("test failed")),
        CompletedRequest("req-002", CallerType.Cli, "compile", Seq("core"), now.minusSeconds(30), Duration.ofSeconds(2), true, None),
        CompletedRequest("req-003", CallerType.Cli, "compile", Seq("api"), now.minusSeconds(40), Duration.ofMillis(800), true, None),
        CompletedRequest("req-004", CallerType.Bsp, "compile", Seq("core", "api"), now.minusSeconds(50), Duration.ofSeconds(45), true, None),
        CompletedRequest("req-005", CallerType.Cli, "test", Seq("core-test"), now.minusSeconds(60), Duration.ofSeconds(1), false, Some("assertion error")),
      )
    def taskStats(taskName: String): Option[TaskStats] = None
    def allTaskStats: Seq[(String, TaskStats)] = Seq.empty
    def totalRequestsServed: Long = 100L
    def totalErrors: Long = 5L
    def serverUptime: Duration = Duration.ofSeconds(8130L)
    def workerThreadPoolSize: Int = 10
    def inMemoryCachesStats: Map[String, InMemCacheStats] = Map.empty
    def loadedPlugins: Seq[LoadedPluginInfo] = Seq(
      LoadedPluginInfo("web-dashboard", Seq()),
      LoadedPluginInfo("core", Seq("compile", "test", "run", "jar", "publishLocal"))
    )
    def purgeInMemoryCaches(): PurgeCachesResult = PurgeCachesResult(0, 0, 0, false)
    def cancelRequest(requestId: String): Boolean = requestId != "nonexistent"
    def requestStatus(requestId: String): Option[RequestStatus] = None
    def allRequestStatuses: Seq[RequestStatus] = Seq(
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
    )
  }

  private def stubProject: DederProject =
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

  private val stubTaskInvoker: TaskInvokerApi = new TaskInvokerApi {
    def invoke(
        taskName: String,
        moduleIds: Seq[String],
        args: Seq[String],
        onNotification: ServerNotification => Unit
    ): TaskInvokeResult = {
      onNotification(ServerNotification.Output(s"Running $taskName..."))
      val outcomes = moduleIds.map { m =>
        TaskInvokeOutcome(m, success = true, None, fromCache = false)
      }
      TaskInvokeResult(outcomes, None, None)
    }
  }

  private val stubTaskRegistry: TasksRegistryApi = new TasksRegistryApi {
    def allTasks: Seq[TaskInfo] = Seq(
      TaskInfo("compile", "Compile Scala/Java sources", "Build", TaskKind.Standard, Seq.empty, false, false, false, Seq.empty),
      TaskInfo("test", "Run tests", "Test", TaskKind.Standard, Seq.empty, false, false, false, Seq.empty),
      TaskInfo("run", "Run main class", "Run", TaskKind.Standard, Seq.empty, false, false, false, Seq.empty),
    )
    def tasksFor(moduleType: ba.sake.deder.config.DederProject.ModuleType): Seq[TaskInfo] = Seq.empty
  }

  val dashboardService = new DashboardService(stubInternals, stubTaskRegistry)
  val executionLog = TaskExecutionLog(config.tasksMaxHistory.toInt)
  val taskRunner = TaskRunner(
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
  //private val server = DashboardServer(config, stubProject, stubInternals, stubTaskInvoker, stubTaskRegistry)
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

  // TODO WTF use sttp..
  private def httpGet(path: String): (Int, String) = {
    val url = s"$baseUrl$path"
    val conn = URL(url).openConnection().asInstanceOf[HttpURLConnection]
    conn.setInstanceFollowRedirects(false)
    conn.setConnectTimeout(2000)
    conn.setReadTimeout(2000)
    try {
      val code = conn.getResponseCode
      val stream = if code >= 400 then conn.getErrorStream else conn.getInputStream
      val body = if stream != null then scala.io.Source.fromInputStream(stream).mkString else ""
      conn.disconnect()
      (code, body)
    } catch {
      case e: Exception =>
        conn.disconnect()
        throw e
    }
  }

  private def httpPost(path: String): (Int, String) = {
    val url = s"$baseUrl$path"
    val conn = URL(url).openConnection().asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("POST")
    conn.setDoOutput(true)
    conn.setInstanceFollowRedirects(false)
    conn.setConnectTimeout(2000)
    conn.setReadTimeout(2000)
    try {
      val code = conn.getResponseCode
      val stream = if code >= 400 then conn.getErrorStream else conn.getInputStream
      val body = if stream != null then scala.io.Source.fromInputStream(stream).mkString else ""
      conn.disconnect()
      (code, body)
    } catch {
      case e: Exception =>
        conn.disconnect()
        throw e
    }
  }

  test("GET / redirects to /server") {
    val (code, _) = httpGet("/")
    assertEquals(code, 301)
  }

  test("GET /modules returns HTML page") {
    val (code, body) = httpGet("/modules")
    assertEquals(code, 200)
    assert(body.contains("Modules"), s"body should contain 'Modules', got: ${body.take(300)}")
  }

  test("GET /modules/graph returns HTML page") {
    val (code, body) = httpGet("/modules/graph")
    assertEquals(code, 200)
    assert(body.contains("Modules graph"), s"body should contain 'Modules graph', got: ${body.take(300)}")
  }

  test("GET /server returns HTML page with server info") {
    val (code, body) = httpGet("/server")
    assertEquals(code, 200)
    assert(body.contains("Deder"), s"body should contain 'Deder', got: ${body.take(300)}")
    assert(body.contains("JDK"), s"body should contain 'JDK', got: ${body.take(300)}")
    assert(body.contains("OS"), s"body should contain 'OS', got: ${body.take(300)}")
  }

  // --- Live tab ---
  test("GET /live returns HTML page with Live tab") {
    val (code, body) = httpGet("/live")
    assertEquals(code, 200)
    assert(body.contains("Live"), s"body should contain 'Live', got: ${body.take(300)}")
    assert(body.contains("Auto-refresh"), s"body should contain auto-refresh toggle, got: ${body.take(300)}")
    assert(body.contains("Requests"), s"body should contain 'Requests' section, got: ${body.take(300)}")
  }

  test("GET /stats/overview returns stat cards HTML fragment") {
    val (code, body) = httpGet("/stats/overview")
    assertEquals(code, 200)
    assert(body.contains("100"), s"should contain total requests (100), got: $body")
    assert(body.contains("5"), s"should contain total errors (5), got: $body")
  }

  test("GET /stats/requests returns state-grouped request sections") {
    val (code, body) = httpGet("/stats/requests")
    assertEquals(code, 200)
    assert(body.contains("Queued"), s"should contain Queued section, got: $body")
    assert(body.contains("Acquiring Locks"), s"should contain Acquiring Locks section, got: $body")
    assert(body.contains("Executing"), s"should contain Executing section, got: $body")
    assert(body.contains("Lock 1/3"), s"should contain lock progress, got: $body")
    assert(body.contains("Stage 2/5"), s"should contain stage progress, got: $body")
  }

  test("POST /stats/cancel with valid requestId returns cancelled badge") {
    val (code, body) = httpPost("/stats/cancel?requestId=req-q1")
    assertEquals(code, 200)
    assert(body.contains("Cancelled"), s"should contain 'Cancelled', got: $body")
  }

  test("GET /stats/caches returns caches table HTML") {
    val (code, body) = httpGet("/stats/caches")
    assertEquals(code, 200)
    assert(body.contains("No in-memory caches active"), s"should show empty caches message, got: $body")
  }

  test("POST /stats/caches/clear returns result summary and updated table") {
    val (code, body) = httpPost("/stats/caches/clear")
    assertEquals(code, 200)
    assert(body.contains("No caches were active"), s"should contain 'No caches were active', got: $body")
    assert(body.contains("No in-memory caches active"), s"should show updated empty caches message, got: $body")
  }

  // --- History tab ---
  test("GET /history returns HTML page with History tab") {
    val (code, body) = httpGet("/history")
    assertEquals(code, 200)
    assert(body.contains("History"), s"body should contain 'History', got: ${body.take(300)}")
    assert(body.contains("history-filters"), s"body should contain filter form, got: ${body.take(300)}")
  }

  test("GET /stats/history-table returns filtered history HTML table") {
    val (code, body) = httpGet("/stats/history-table?limit=50&offset=0")
    assertEquals(code, 200)
    assert(body.contains("compile"), s"should contain task name 'compile', got: $body")
    assert(body.contains("Client"), s"should contain Client column header, got: $body")
    assert(body.contains("OK"), s"should contain success marker, got: $body")
  }

  test("GET /stats/history-table filters by status=success") {
    val (code, body) = httpGet("/stats/history-table?status=success&limit=50&offset=0")
    assertEquals(code, 200)
    assert(body.contains("OK"), s"should contain OK, got: $body")
    assert(!body.contains("FAIL"), s"should not contain FAIL when filtering success, got: $body")
  }

  test("GET /stats/history-table filters by search=core") {
    val (code, body) = httpGet("/stats/history-table?search=core&limit=50&offset=0")
    assertEquals(code, 200)
    assert(body.contains("core"), s"should contain 'core', got: $body")
  }

  test("GET /stats/history-table filters by caller=CLI") {
    val (code, body) = httpGet("/stats/history-table?caller=CLI&limit=50&offset=0")
    assertEquals(code, 200)
    assert(body.contains("compile"), s"should contain results, got: $body")
  }

  // --- Stats tab ---
  test("GET /stats returns HTML page with Aggregates tab") {
    val (code, body) = httpGet("/stats")
    assertEquals(code, 200)
    assert(body.contains("Aggregates"), s"body should contain 'Aggregates', got: ${body.take(300)}")
  }

  test("GET /stats/task-aggregates returns per-task stats HTML") {
    val (code, body) = httpGet("/stats/task-aggregates")
    assertEquals(code, 200)
    assert(body.contains("compile"), s"should contain task 'compile', got: $body")
    assert(body.contains("test"), s"should contain task 'test', got: $body")
  }

  test("GET /stats/module-breakdown returns module rows for a task") {
    val (code, body) = httpGet("/stats/module-breakdown?task=compile&expanded=true")
    assertEquals(code, 200)
    assert(body.contains("core"), s"should contain module 'core', got: $body")
    assert(body.contains("api"), s"should contain module 'api', got: $body")
  }

  test("GET /stats/top-offenders returns top tasks by time") {
    val (code, body) = httpGet("/stats/top-offenders?n=3")
    assertEquals(code, 200)
    assert(body.contains("#1"), s"should contain '#1' ranking, got: $body")
  }

  test("GET /stats/module-aggregates returns heaviest modules HTML") {
    val (code, body) = httpGet("/stats/module-aggregates?n=3")
    assertEquals(code, 200)
    // core has 2+45=47s, api has 0.8+45=45.8s, core-test has 12+1=13s
    assert(body.contains("#1"), s"should contain '#1' ranking, got: $body")
    assert(body.contains("core"), s"should contain 'core' as heaviest, got: $body")
    assert(body.contains("api"), s"should contain 'api', got: $body")
  }

  test("GET /stats/error-summary returns error summary") {
    val (code, body) = httpGet("/stats/error-summary")
    assertEquals(code, 200)
    assert(body.contains("test"), s"should contain task with errors, got: $body")
  }

  // --- JSON APIs ---
  test("GET /api/modules returns JSON") {
    val (code, body) = httpGet("/api/modules")
    assertEquals(code, 200)
    assert(body.startsWith("["), s"should be a JSON array, got: ${body.take(200)}")
  }

  test("GET /api/stats/overview returns JSON with totals") {
    val (code, body) = httpGet("/api/stats/overview")
    assertEquals(code, 200)
    assert(body.contains("\"totalRequestsServed\": 100"), s"should contain 100, got: $body")
    assert(body.contains("\"totalErrors\": 5"), s"should contain 5, got: $body")
    assert(body.contains("\"uptimeSecs\": 8130"), s"should contain 8130, got: $body")
  }

  test("GET /api/stats/history returns JSON with history entries") {
    val (code, body) = httpGet("/api/stats/history")
    assertEquals(code, 200)
    assert(body.contains("\"requestId\": \"req-000\""), s"should contain req-000, got: $body")
    assert(body.contains("\"caller\": \"CLI\""), s"should contain caller CLI, got: $body")
    assert(body.contains("\"success\": true"), s"should contain success:true, got: $body")
  }

  test("GET /api/stats/task-aggregates returns JSON") {
    val (code, body) = httpGet("/api/stats/task-aggregates")
    assertEquals(code, 200)
    assert(body.contains("\"taskName\": \"compile\""), s"should contain compile, got: $body")
    assert(body.contains("\"invocations\""), s"should contain invocations, got: $body")
  }

  test("GET /api/stats/module-breakdown returns JSON") {
    val (code, body) = httpGet("/api/stats/module-breakdown?task=compile")
    assertEquals(code, 200)
    assert(body.contains("\"moduleId\""), s"should contain moduleId, got: $body")
  }

  test("GET /api/stats/error-summary returns JSON") {
    val (code, body) = httpGet("/api/stats/error-summary")
    assertEquals(code, 200)
    assert(body.startsWith("["), s"should be a JSON array, got: ${body.take(200)}")
  }

  test("GET /api/stats/module-aggregates returns JSON with modules ranked by time") {
    val (code, body) = httpGet("/api/stats/module-aggregates?n=3")
    assertEquals(code, 200)
    assert(body.contains("\"moduleId\": \"core\""), s"should contain core as heaviest, got: $body")
    assert(body.contains("\"moduleId\": \"api\""), s"should contain api second, got: $body")
    assert(body.contains("\"totalTimeMs\""), s"should contain totalTimeMs, got: $body")
  }

  test("GET /api/stats/request-statuses returns JSON with state field") {
    val (code, body) = httpGet("/api/stats/request-statuses")
    assertEquals(code, 200)
    assert(body.contains("\"state\":"), s"should contain state field, got: $body")
    assert(body.contains("\"Queued\""), s"should contain Queued state, got: $body")
    assert(body.contains("\"AcquiringLocks\""), s"should contain AcquiringLocks state, got: $body")
    assert(body.contains("\"Executing\""), s"should contain Executing state, got: $body")
  }

  test("POST /api/cancel with valid requestId returns cancelled true") {
    val (code, body) = httpPost("/api/cancel?requestId=req-q1")
    assertEquals(code, 200)
    assert(body.contains("\"cancelled\": true"), s"should be cancelled true, got: $body")
  }

  test("POST /api/cancel with invalid requestId returns cancelled false") {
    val (code, body) = httpPost("/api/cancel?requestId=nonexistent")
    assertEquals(code, 200)
    assert(body.contains("\"cancelled\": false"), s"should be cancelled false, got: $body")
  }

  // --- Tasks tab tests ---
  test("GET /tasks returns HTML page with trigger form") {
    val (code, body) = httpGet("/tasks")
    assertEquals(code, 200)
    assert(body.contains("Tasks"), s"body should contain 'Tasks', got: ${body.take(300)}")
    assert(body.contains("task-list"), s"body should contain task datalist, got: ${body.take(300)}")
    assert(body.contains("Run"), s"body should contain Run button, got: ${body.take(300)}")
  }

  test("GET /tasks/run with valid task returns log table") {
    val (code, body) = httpGet("/tasks/run?taskName=compile")
    assertEquals(code, 200)
    assert(body.contains("compile"), s"should contain task name 'compile', got: $body")
  }

  test("GET /tasks/log-table returns log table HTML") {
    val (code, body) = httpGet("/tasks/log-table")
    assertEquals(code, 200)
    assert(body.contains("<table"), s"should return a table, got: ${body.take(200)}")
  }

  test("GET /api/tasks returns JSON array") {
    // trigger a task first
    httpGet("/tasks/run?taskName=compile")
    Thread.sleep(600) // wait for stub to complete
    val (code, body) = httpGet("/api/tasks")
    assertEquals(code, 200)
    assert(body.startsWith("["), s"should be JSON array, got: ${body.take(200)}")
    assert(body.contains("\"taskName\": \"compile\""), s"should contain compile entry, got: $body")
  }

  test("GET /api/tasks/exec returns JSON for valid execId") {
    httpGet("/tasks/run?taskName=compile")
    Thread.sleep(600)
    val (_, tasksBody) = httpGet("/api/tasks")
    // extract execId from JSON array
    val execId = extractExecId(tasksBody)
    assert(execId.nonEmpty, s"should have an execId")

    val (code, body) = httpGet(s"/api/tasks/exec?execId=$execId")
    assertEquals(code, 200)
    assert(body.contains(execId), s"should contain the execId, got: $body")
    assert(body.contains("compile"), s"should contain task name 'compile', got: $body")
  }

  test("POST /api/tasks/run with valid task returns execId JSON") {
    val (code, body) = httpPost("/api/tasks/run?taskName=compile")
    assertEquals(code, 200)
    assert(body.contains("\"execId\""), s"should contain execId field, got: $body")
    assert(body.contains("\"status\""), s"should contain status field, got: $body")
  }

  test("POST /api/tasks/run with unknown task returns error") {
    val (code, body) = httpPost("/api/tasks/run?taskName=nonexistent19999")
    assertEquals(code, 200)
    assert(body.contains("\"error\""), s"should contain error field, got: $body")
  }

  private def extractExecId(json: String): String =
    try
      val idx = json.indexOf("\"execId\": \"")
      if idx >= 0 then
        val start = idx + 11
        val end = json.indexOf("\"", start)
        json.substring(start, end)
      else ""
    catch case _: Exception => ""

  test("GET /nonexistent returns 404") {
    val (code, _) = httpGet("/nonexistent")
    assertEquals(code, 404)
  }
}
