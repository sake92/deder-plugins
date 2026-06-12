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
        CompletedRequest("req-000", CallerType.Cli, "compile", Seq("my-module"), now.minusSeconds(10), Duration.ofSeconds(5), true),
        CompletedRequest("req-001", CallerType.Bsp, "test", Seq("core-test"), now.minusSeconds(20), Duration.ofSeconds(12), false),
        CompletedRequest("req-002", CallerType.Cli, "compile", Seq("core"), now.minusSeconds(30), Duration.ofSeconds(2), true),
        CompletedRequest("req-003", CallerType.Cli, "compile", Seq("api"), now.minusSeconds(40), Duration.ofMillis(800), true),
        CompletedRequest("req-004", CallerType.Bsp, "compile", Seq("core", "api"), now.minusSeconds(50), Duration.ofSeconds(45), true),
        CompletedRequest("req-005", CallerType.Cli, "test", Seq("core-test"), now.minusSeconds(60), Duration.ofSeconds(1), false),
      )
    def taskStats(taskName: String): Option[TaskStats] = None
    def allTaskStats: Seq[(String, TaskStats)] = Seq.empty
    def totalRequestsServed: Long = 100L
    def totalErrors: Long = 5L
    def serverUptime: Duration = Duration.ofSeconds(8130L)
    def workerThreadPoolSize: Int = 10
    def inMemoryCachesStats: Map[String, InMemCacheStats] = Map.empty
    def loadedPlugins: Seq[LoadedPluginInfo] = Seq(
      LoadedPluginInfo("web-dashboard", Seq())
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

  private val config = WebDashboardPluginConfig(true, testHost, testPort.toLong, testRefreshMs.toLong)
  private val server = DashboardServer(config, stubProject, stubInternals)
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

  test("GET / redirects to /modules") {
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
  }

  test("GET /stats/overview returns stat cards HTML fragment") {
    val (code, body) = httpGet("/stats/overview")
    assertEquals(code, 200)
    assert(body.contains("100"), s"should contain total requests (100), got: $body")
    assert(body.contains("5"), s"should contain total errors (5), got: $body")
  }

  test("GET /stats/current returns current requests HTML fragment") {
    val (code, body) = httpGet("/stats/current")
    assertEquals(code, 200)
    assert(body.contains("compile"), s"should contain task name 'compile', got: $body")
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

  test("GET /api/stats/current returns JSON with current request") {
    val (code, body) = httpGet("/api/stats/current")
    assertEquals(code, 200)
    assert(body.contains("\"requestId\": \"req-001\""), s"should contain req-001, got: $body")
    assert(body.contains("\"caller\": \"CLI\""), s"should contain caller CLI, got: $body")
    assert(body.contains("\"taskName\": \"compile\""), s"should contain compile, got: $body")
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

  test("GET /nonexistent returns 404") {
    val (code, _) = httpGet("/nonexistent")
    assertEquals(code, 404)
  }
}
