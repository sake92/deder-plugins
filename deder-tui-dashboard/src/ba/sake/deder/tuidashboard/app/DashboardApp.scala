package ba.sake.deder.tuidashboard.app

import ba.sake.deder.tuidashboard.model.*
import ba.sake.tupson.*
import layoutz.*

enum Tab { case Modules, Live, History, Aggregates, Info }
enum SortOrder { case Newest, Oldest, Longest, Shortest }

case class DashboardState(
    modules: Seq[ApiModule] = Seq.empty,
    overview: Option[StatsOverview] = None,
    currentRequests: Seq[ApiRequestStatus] = Seq.empty,
    history: Seq[ApiHistoryEntry] = Seq.empty,
    taskStats: Seq[ApiTaskAggregate] = Seq.empty,
    errors: Seq[ApiErrorSummaryEntry] = Seq.empty,
    serverInfo: Option[ApiServerInfo] = None,
    serverUrl: String = "http://localhost:9292",
    lastError: Option[String] = None,
    activeTab: Tab = Tab.Modules,
    historySort: SortOrder = SortOrder.Newest
)

sealed trait Msg
case class ModulesResp(result: Either[String, String]) extends Msg
case class OverviewResp(result: Either[String, String]) extends Msg
case class CurrentResp(result: Either[String, String]) extends Msg
case class HistoryResp(result: Either[String, String]) extends Msg
case class TaskStatsResp(result: Either[String, String]) extends Msg
case class ErrorsResp(result: Either[String, String]) extends Msg
case class ServerInfoResp(result: Either[String, String]) extends Msg
case class ChangeTab(tab: Tab) extends Msg
case object ToggleSort extends Msg
case object Quit extends Msg

class DashboardApp(serverUrl: String, pollMs: Int) extends LayoutzApp[DashboardState, Msg] {

  def init: (DashboardState, Cmd[Msg]) = (
    DashboardState(serverUrl = serverUrl),
    Cmd.none
  )

  def update(msg: Msg, state: DashboardState): (DashboardState, Cmd[Msg]) = msg match {
    case ModulesResp(Right(json)) =>
      safely(state)(_.copy(modules = json.parseJson[Seq[ApiModule]], lastError = None))
    case ModulesResp(Left(err)) =>
      state.copy(lastError = Some(s"modules: $err"))

    case OverviewResp(Right(json)) =>
      safely(state)(_.copy(overview = Some(json.parseJson[StatsOverview]), lastError = None))
    case OverviewResp(Left(err)) =>
      state.copy(lastError = Some(s"overview: $err"))

    case CurrentResp(Right(json)) =>
      safely(state)(_.copy(currentRequests = json.parseJson[Seq[ApiRequestStatus]], lastError = None))
    case CurrentResp(Left(err)) =>
      state.copy(lastError = Some(s"current: $err"))

    case HistoryResp(Right(json)) =>
      safely(state)(_.copy(history = json.parseJson[Seq[ApiHistoryEntry]], lastError = None))
    case HistoryResp(Left(err)) =>
      state.copy(lastError = Some(s"history: $err"))

    case TaskStatsResp(Right(json)) =>
      safely(state)(_.copy(
        taskStats = json.parseJson[Seq[ApiTaskAggregate]].sortBy(-_.totalTimeMs),
        lastError = None
      ))
    case TaskStatsResp(Left(err)) =>
      state.copy(lastError = Some(s"task stats: $err"))

    case ErrorsResp(Right(json)) =>
      safely(state)(_.copy(errors = json.parseJson[Seq[ApiErrorSummaryEntry]], lastError = None))
    case ErrorsResp(Left(err)) =>
      state.copy(lastError = Some(s"errors: $err"))

    case ServerInfoResp(Right(json)) =>
      safely(state)(_.copy(serverInfo = Some(json.parseJson[ApiServerInfo]), lastError = None))
    case ServerInfoResp(Left(err)) =>
      state.copy(lastError = Some(s"server info: $err"))

    case ChangeTab(tab) =>
      (state.copy(activeTab = tab, lastError = None), Cmd.none)

    case ToggleSort =>
      val next = state.historySort match
        case SortOrder.Newest  => SortOrder.Oldest
        case SortOrder.Oldest  => SortOrder.Longest
        case SortOrder.Longest => SortOrder.Shortest
        case SortOrder.Shortest => SortOrder.Newest
      (state.copy(historySort = next), Cmd.none)

    case Quit =>
      (state, Cmd.exit)
  }

  private def safely(state: DashboardState)(f: DashboardState => DashboardState): DashboardState =
    try f(state)
    catch { case e: Exception => state.copy(lastError = Some(e.getMessage)) }

  def subscriptions(state: DashboardState): Sub[Msg] = Sub.batch(
    Sub.http.pollMs(s"${state.serverUrl}/api/modules", pollMs, ModulesResp.apply),
    Sub.http.pollMs(s"${state.serverUrl}/api/stats/overview", pollMs, OverviewResp.apply),
    Sub.http.pollMs(s"${state.serverUrl}/api/stats/request-statuses", pollMs, CurrentResp.apply),
    Sub.http.pollMs(s"${state.serverUrl}/api/stats/history", pollMs, HistoryResp.apply),
    Sub.http.pollMs(s"${state.serverUrl}/api/stats/task-aggregates", pollMs, TaskStatsResp.apply),
    Sub.http.pollMs(s"${state.serverUrl}/api/stats/error-summary", pollMs, ErrorsResp.apply),
    Sub.http.pollMs(s"${state.serverUrl}/api/server", pollMs, ServerInfoResp.apply),
    Sub.onKeyPress {
      case Key.Char('1')       => Some(ChangeTab(Tab.Modules))
      case Key.Char('2')       => Some(ChangeTab(Tab.Live))
      case Key.Char('3')       => Some(ChangeTab(Tab.History))
      case Key.Char('4')       => Some(ChangeTab(Tab.Aggregates))
      case Key.Char('5')       => Some(ChangeTab(Tab.Info))
      case Key.Char('s')       => Some(ToggleSort)
      case Key.Char('q')       => Some(Quit)
      case Key.Ctrl('c')       => Some(Quit)
      case Key.Ctrl('x')       => Some(Quit)
      case _                   => None
    }
  )

  def view(state: DashboardState): Element = {
    val tabBar = tabBarView(state.activeTab)

    val content = state.activeTab match
      case Tab.Modules    => modulesView(state)
      case Tab.Live       => liveView(state)
      case Tab.History    => historyView(state)
      case Tab.Aggregates => aggregatesView(state)
      case Tab.Info       => infoView(state)

    val errorBanner = state.lastError.map { err =>
      banner(s"  $err").color(Color.Red).bg(Color.BrightBlack)
    }

    val projectLine = state.serverInfo.map(si => s"Project: ${si.projectRoot}").getOrElse("")
    val webDashLine = s"Web dashboard: ${state.serverUrl}"

    layout(
      banner(
        layout(
          Text("Deder TUI Dashboard").style(Style.Bold),
          Text(projectLine),
          Text(webDashLine)
        )
      )
        .border(Border.Double)
        .color(Color.Cyan),
      tabBar,
      errorBanner.getOrElse(empty),
      content
    )
  }

  // --- Tab bar ---

  private def tabBarView(active: Tab): Element = {
    def tab(n: Int, label: String, t: Tab): Element = {
      val text = s"[$n] $label"
      if active == t then text.color(Color.Cyan).style(Style.Bold)
      else text
    }
    rowTight(
      tab(1, "Modules", Tab.Modules),
      Text("  "),
      tab(2, "Live", Tab.Live),
      Text("  "),
      tab(3, "History", Tab.History),
      Text("  "),
      tab(4, "Aggregates", Tab.Aggregates),
      Text("  "),
      tab(5, "Info", Tab.Info),
      Text("    q quit")
    )
  }

  // --- Tab 1: Modules ---

  private def modulesView(state: DashboardState): Element = {
    if state.modules.isEmpty then "(no data)".color(Color.BrightBlack)
    else table(
      headers = Seq("Module", "Type", "Deps"),
      rows = state.modules.map(m => Seq(Text(m.id), Text(m.`type`), Text(m.deps.toString)))
    ).border(Border.Round)
  }

  // --- Tab 2: Live ---

  private def liveView(state: DashboardState): Element = {
    val statsLine = state.overview match {
      case Some(o) =>
        val h = o.uptimeSecs / 3600
        val m = (o.uptimeSecs % 3600) / 60
        val s = o.uptimeSecs % 60
        s"Served: ${o.totalRequestsServed}  Errors: ${o.totalErrors}  Uptime: ${h}h ${m}m ${s}s"
      case None => "Waiting for data..."
    }

    val currentBox = box("Current Requests")(
      if state.currentRequests.isEmpty then "(none)".color(Color.BrightBlack)
      else {
        val items = state.currentRequests.map { r =>
          val stateLabel = r.state.label
          val stateColor = r.state match
            case ApiRequestState.Queued          => Color.BrightBlack
            case ApiRequestState.AcquiringLocks  => Color.BrightYellow
            case ApiRequestState.Executing       => Color.BrightGreen
            case _                               => Color.BrightBlack
          val stateText = Text(s" $stateLabel").color(stateColor)
          val lockText = r.lockProgress.map(l => Text(s" Lock ${l.acquired}/${l.total}").color(Color.BrightYellow)).getOrElse(empty)
          val stageText = r.taskProgress.map(t => Text(s" Stage ${t.currentStage}/${t.totalStages}").color(Color.BrightGreen)).getOrElse(empty)
          rowTight(
            Text(r.requestId),
            Text(s"  ${r.taskName}"),
            Text(s"  [${r.moduleIds.mkString(", ")}]"),
            stateText,
            lockText,
            stageText
          )
        }
        layout(items*)
      }
    ).border(Border.Round).color(Color.BrightBlue)

    layout(
      section("Stats")(statsLine.color(Color.BrightGreen)),
      currentBox
    )
  }

  // --- Tab 3: History (with client-side sorting) ---

  private def historyView(state: DashboardState): Element = {
    val sortLabel = state.historySort match
      case SortOrder.Newest  => "Newest"
      case SortOrder.Oldest  => "Oldest"
      case SortOrder.Longest => "Longest"
      case SortOrder.Shortest => "Shortest"

    val sorted = state.historySort match
      case SortOrder.Newest  => state.history.sortBy(-_.startTimeMs)
      case SortOrder.Oldest  => state.history.sortBy(_.startTimeMs)
      case SortOrder.Longest => state.history.sortBy(-_.durationMs)
      case SortOrder.Shortest => state.history.sortBy(_.durationMs)

    val header = s"(sorted by: $sortLabel, press 's' to change)".color(Color.BrightBlack)

    val historyBox = box("History")(
      if sorted.isEmpty then "(none)".color(Color.BrightBlack)
      else {
        val items = sorted.map { h =>
          val icon = if h.success then "+".color(Color.Green) else "x".color(Color.Red)
          rowTight(
            icon,
            Text(s" [${h.caller}]"),
            Text(s" ${h.taskName}"),
            Text(s"  ${h.durationMs}ms"),
            Text(s"  [${h.moduleIds.mkString(", ")}]")
          )
        }
        layout(items*)
      }
    ).border(Border.Round).color(Color.BrightMagenta)

    layout(header, historyBox)
  }

  // --- Tab 4: Aggregates ---

  private def aggregatesView(state: DashboardState): Element = {
    val taskStatsTable = if state.taskStats.isEmpty then "(no data)".color(Color.BrightBlack)
    else {
      val rows = state.taskStats.map { s =>
        Seq(Text(s.taskName), Text(s.invocations.toString), Text(s.errors.toString),
          Text(s.avgTimeMs.toString + "ms"), Text(s.maxTimeMs.toString + "ms"), Text(s.longestModuleId))
      }
      table(
        headers = Seq("Task Name", "Invocations", "Errors", "Avg", "Max", "Longest Module"),
        rows = rows
      ).border(Border.Round)
    }

    val errorTable = if state.errors.isEmpty then empty
    else {
      val rows = state.errors.map { e =>
        Seq(Text(e.taskName), Text(e.moduleIds.mkString(", ")), Text(e.errorCount.toString))
      }
      box("Errors")(
        table(
          headers = Seq("Task Name", "Module IDs", "Error Count"),
          rows = rows
        ).border(Border.Round)
      ).border(Border.Round).color(Color.Red)
    }

    layout(taskStatsTable, errorTable)
  }

  // --- Tab 5: Info ---

  private def infoView(state: DashboardState): Element = state.serverInfo match {
    case None => "(loading...)".color(Color.BrightBlack)
    case Some(si) =>
      val h = si.uptimeSecs / 3600
      val m = (si.uptimeSecs % 3600) / 60
      val sec = si.uptimeSecs % 60
      val uptimeStr = s"${h}h ${m}m ${sec}s"

      val dederSection = box("Deder")(
        layout(
          Text(s"Version: ${si.dederVersion}"),
          Text(s"Uptime: $uptimeStr"),
          Text(s"Modules: ${si.moduleCount}"),
          Text(s"Plugins: ${si.pluginCount}"),
          Text(s"Heap: ${si.usedHeapMB}MB / ${si.maxHeapMB}MB")
        )
      ).border(Border.Round)

      val jdkSection = box("JDK")(
        layout(
          Text(s"Version: ${si.jdkVersion}"),
          Text(s"Vendor: ${si.jdkVendor}")
        )
      ).border(Border.Round)

      val osSection = box("OS")(
        layout(
          Text(s"Name / Arch: ${si.osName} ${si.osArch}"),
          Text(s"Processors: ${si.processors}")
        )
      ).border(Border.Round)

      val projectSection = box("Project")(
        Text(s"Folder: ${si.projectRoot}")
      ).border(Border.Round)

      val pluginsSection = if si.plugins.nonEmpty then
        box("Loaded Plugins")(
          table(
            headers = Seq(Text("Plugin ID"), Text("#Tasks"), Text("Task Names")),
            rows = si.plugins.map(p => Seq(Text(p.id), Text(p.taskCount.toString), Text(p.taskNames.mkString(", "))))
          ).border(Border.Round)
        ).border(Border.Round)
      else empty

      layout(dederSection, jdkSection, osSection, projectSection, pluginsSection)
  }
}
