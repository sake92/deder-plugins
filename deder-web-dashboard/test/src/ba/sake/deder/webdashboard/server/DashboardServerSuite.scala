package ba.sake.deder.webdashboard.server

import java.time.{Duration, Instant}
import java.net.{URL, HttpURLConnection}
import java.util.concurrent.TimeUnit
import ba.sake.deder.*
import ba.sake.deder.config.DederProject
import WebDashboard.WebDashboardPluginConfig
import munit.FunSuite

class DashboardServerSuite extends FunSuite {

  private val testHost = "localhost"
  private val testPort = 19999
  private val testRefreshMs = 5000

  private def stubInternals: DederProjectInternals = new DederProjectInternals {
    def currentRequests: Seq[LiveRequest] =
      Seq(LiveRequest("req-001", CallerType.Cli, "compile", Seq("my-module"), Instant.now()))
    def recentHistory: Seq[CompletedRequest] =
      Seq(
        CompletedRequest("req-000", CallerType.Cli, "compile", Seq("my-module"),
          Instant.now().minusSeconds(10), Duration.ofSeconds(5), true)
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
      true
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
      case None => System.clearProperty(projectRootProperty)
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

  test("GET /server returns HTML page with server properties") {
    val (code, body) = httpGet("/server")
    assertEquals(code, 200)
    assert(body.contains("Server Properties"), s"body should contain 'Server Properties', got: ${body.take(300)}")
    assert(body.contains("JDK"), s"body should contain 'JDK', got: ${body.take(300)}")
    assert(body.contains("Project Root"), s"body should contain 'Project Root', got: ${body.take(300)}")
  }

  test("GET /live returns HTML page with live stats") {
    val (code, body) = httpGet("/live")
    assertEquals(code, 200)
    assert(body.contains("Live Stats"), s"body should contain 'Live Stats', got: ${body.take(300)}")
  }

  test("GET /stats/overview returns stat cards HTML fragment") {
    val (code, body) = httpGet("/stats/overview")
    assertEquals(code, 200)
    assert(body.contains("100"), s"should contain total requests (100), got: ${body}")
    assert(body.contains("5"), s"should contain total errors (5), got: ${body}")
  }

  test("GET /stats/current returns current requests HTML fragment") {
    val (code, body) = httpGet("/stats/current")
    assertEquals(code, 200)
    assert(body.contains("compile"), s"should contain task name 'compile', got: ${body}")
  }

  test("GET /stats/history returns recent history HTML fragment") {
    val (code, body) = httpGet("/stats/history")
    assertEquals(code, 200)
    assert(body.contains("compile"), s"should contain task name 'compile', got: ${body}")
    assert(body.contains("OK"), s"should contain success marker 'OK', got: ${body}")
  }
}
