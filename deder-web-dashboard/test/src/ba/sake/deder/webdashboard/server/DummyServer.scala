package ba.sake.deder.webdashboard.server

import java.time.{Duration, Instant}
import java.util.concurrent.CountDownLatch
import scala.jdk.CollectionConverters.*
import ba.sake.deder.*
import ba.sake.deder.config.DederProject
import ba.sake.deder.plugins.WebDashboard.WebDashboardPluginConfig
import ba.sake.deder.webdashboard.server.stubs.*

private class DummyModule(id: String, mt: DederProject.ModuleType)
    extends DederProject.DederModule(id, ".", java.util.List.of(), java.util.List.of(), true, mt)

@main def dummyServer(): Unit =

  val now = Instant.now()
  val host = "127.0.0.1"
  val port = 19980

  // --- modules: one of each type ---
  val modules: Seq[DederProject.DederModule] = Seq(
    DummyModule("java-lib", DederProject.ModuleType.JAVA),
    DummyModule("java-lib-test", DederProject.ModuleType.JAVA_TEST),
    DummyModule("scala-core", DederProject.ModuleType.SCALA),
    DummyModule("scala-core-test", DederProject.ModuleType.SCALA_TEST),
    DummyModule("scala-js-app", DederProject.ModuleType.SCALA_JS),
    DummyModule("scala-js-app-test", DederProject.ModuleType.SCALA_JS_TEST),
    DummyModule("scala-native-app", DederProject.ModuleType.SCALA_NATIVE),
    DummyModule("scala-native-app-test", DederProject.ModuleType.SCALA_NATIVE_TEST),
  )

  val project = new DederProject(
    modules.asJava,
    java.util.List.of(), // plugins
    java.util.List.of(), // repositories
    true,                // includeDefaultRepos
    java.util.List.of(), // watchIgnore
    true,                // bspEnabled
    1L,                  // maxActiveCompilers
    1L,                  // maxConcurrentTestForks
    java.util.Map.of(),  // tools
  )

  // --- internals ---
  val internals = StubInternals(
    currentRequests = Seq(
      LiveRequest("req-101", CallerType.Cli, "compile", Seq("scala-core"), now),
      LiveRequest("req-102", CallerType.Bsp, "test", Seq("scala-core-test"), now.minusSeconds(3)),
    ),
    recentHistory = Seq(
      CompletedRequest("req-100", CallerType.Cli, "compile", Seq("java-lib", "scala-core"),
        now.minusSeconds(90), Duration.ofSeconds(12), true, None),
      CompletedRequest("req-099", CallerType.Bsp, "test", Seq("scala-core-test"),
        now.minusSeconds(180), Duration.ofSeconds(8), false, Some("assertion failed: 2 != 3")),
      CompletedRequest("req-098", CallerType.Cli, "run", Seq("scala-js-app"),
        now.minusSeconds(300), Duration.ofMillis(450), true, None),
    ),
    totalRequestsServed = 2042L,
    totalErrors = 23L,
    serverUptime = Duration.ofHours(3).plusMinutes(17).plusSeconds(42),
    loadedPlugins = Seq(
      LoadedPluginInfo("web-dashboard", Seq()),
      LoadedPluginInfo("protobuf", Seq("protobufGenerate")),
      LoadedPluginInfo("build-info", Seq("buildInfo")),
    ),
    cancelFn = _ => true,
    allRequestStatuses = Seq(
      RequestStatus("req-101", CallerType.Cli, "compile", Seq("scala-core"), now,
        RequestState.EXECUTING, None,
        Some(TaskStageProgress(1, 3, Seq(), Seq(), Seq(), Seq("scala-core"), Seq("java-lib", "scala-js-app")))),
      RequestStatus("req-102", CallerType.Bsp, "test", Seq("scala-core-test"), now.minusSeconds(3),
        RequestState.ACQUIRING_LOCKS,
        Some(LockProgress(1, 2, Some("scala-core.compile"), Some("req-101"))),
        None),
    ),
  )

  // --- tasks ---
  val taskInvoker = StubTaskInvoker { (taskName, moduleIds, _, onNotification) =>
    onNotification(ServerNotification.Output(s"[$taskName] starting on ${moduleIds.mkString(", ")}..."))
    Thread.sleep(300)
    val outcomes = moduleIds.map { m =>
      TaskInvokeOutcome(m, success = true, None, fromCache = false)
    }
    TaskInvokeResult(outcomes, Some(s"$taskName completed for ${moduleIds.size} module(s)"), None)
  }

  val taskRegistry = StubTaskRegistry(tasks = Seq(
    TaskInfo("compile", "Compile Scala/Java sources", "Build", TaskKind.Standard, Seq.empty, false, false, false, Seq.empty),
    TaskInfo("test", "Run tests", "Test", TaskKind.Standard, Seq.empty, false, false, false, Seq.empty),
    TaskInfo("run", "Run main class", "Run", TaskKind.Standard, Seq.empty, false, false, false, Seq.empty),
    TaskInfo("publishLocal", "Publish locally", "Publish", TaskKind.Standard, Seq.empty, false, false, false, Seq.empty),
    TaskInfo("jar", "Package JAR", "Build", TaskKind.Standard, Seq.empty, false, false, false, Seq.empty),
  ))

  // --- server ---
  val config = WebDashboardPluginConfig(true, host, port.toLong, 5000L, 3L, 200L, 500L)
  val dashboardService = new DashboardService(internals, taskRegistry)
  val executionLog = TaskExecutionLog(config.tasksMaxHistory.toInt)
  val taskRunner = TaskRunner(project, taskInvoker, internals, executionLog, config.tasksMaxConcurrent.toInt, taskRegistry)
  val apiRoutes = new ApiRoutes(dashboardService, project, internals, executionLog, taskRunner)
  val htmlRoutes = new HtmlRoutes(config, dashboardService, project, internals, executionLog, taskRunner)
  val server = DashboardServer(config, apiRoutes, htmlRoutes)

  System.setProperty("DEDER_PROJECT_ROOT_DIR", os.pwd.toString)
  server.start()
  Thread.sleep(500)

  Runtime.getRuntime.addShutdownHook(new Thread(() => server.stop()))

  println(s"Dummy server running at http://$host:$port")
  println(s"Modules (${modules.size}): ${modules.map(_.id).mkString(", ")}")
  println("Press Ctrl+C to stop...")
