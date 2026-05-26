package ba.sake.deder.webdashboard.server

import java.time.Instant
import java.net.{URL, HttpURLConnection}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{FiniteDuration, Duration}
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
          Instant.now().minusSeconds(10), FiniteDuration(5L, TimeUnit.SECONDS), true)
      )
    def taskStats(taskName: String): Option[TaskStats] = None
    def allTaskStats: Seq[(String, TaskStats)] = Seq.empty
    def totalRequestsServed: Long = 100L
    def totalErrors: Long = 5L
    def serverUptime: Duration = FiniteDuration(8130L, TimeUnit.SECONDS)
    def workerThreadPoolSize: Int = 10
  }

  private def stubProject: DederProject =
    new DederProject(
      java.util.List.of(),
      java.util.List.of(),
      java.util.List.of(),
      true
    )

  private val config = WebDashboardPluginConfig(testHost, testPort, testRefreshMs)
  private val server = DashboardServer(config, stubProject, stubInternals)
  private val baseUrl = s"http://$testHost:$testPort"

  override def beforeAll(): Unit = {
    server.start()
    Thread.sleep(500)
  }

  override def afterAll(): Unit = {
    server.stop()
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

  test("GET /stats returns HTML page") {
    val (code, body) = httpGet("/stats")
    assertEquals(code, 200)
    assert(body.contains("Live Stats"), s"body should contain 'Live Stats', got: ${body.take(300)}")
  }

  test("GET /stats/overview returns stat cards HTML fragment") {
    val (code, body) = httpGet("/stats/overview")
    assertEquals(code, 200)
    assert(body.contains("100"), s"should contain total requests (100), got: ${body}")
    assert(body.contains("5"), s"should contain total errors (5), got: ${body}")
    assert(body.contains("10"), s"should contain thread pool (10), got: ${body}")
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
