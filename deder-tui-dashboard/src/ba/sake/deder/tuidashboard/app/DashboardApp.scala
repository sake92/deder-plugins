package ba.sake.deder.tuidashboard.app

import ba.sake.deder.tuidashboard.model.*
import ba.sake.tupson.*
import layoutz.*

case class DashboardState(
    modules: Seq[ApiModule] = Seq.empty,
    overview: Option[StatsOverview] = None,
    currentRequests: Seq[ApiCurrentRequest] = Seq.empty,
    history: Seq[ApiHistoryEntry] = Seq.empty,
    serverUrl: String = "http://localhost:9292",
    lastError: Option[String] = None
)

sealed trait Msg
case class ModulesResp(result: Either[String, String]) extends Msg
case class OverviewResp(result: Either[String, String]) extends Msg
case class CurrentResp(result: Either[String, String]) extends Msg
case class HistoryResp(result: Either[String, String]) extends Msg
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
      safely(state)(_.copy(currentRequests = json.parseJson[Seq[ApiCurrentRequest]], lastError = None))
    case CurrentResp(Left(err)) =>
      state.copy(lastError = Some(s"current: $err"))

    case HistoryResp(Right(json)) =>
      safely(state)(_.copy(history = json.parseJson[Seq[ApiHistoryEntry]], lastError = None))
    case HistoryResp(Left(err)) =>
      state.copy(lastError = Some(s"history: $err"))

    case Quit =>
      (state, Cmd.exit)
  }

  private def safely(state: DashboardState)(f: DashboardState => DashboardState): DashboardState =
    try f(state)
    catch { case e: Exception => state.copy(lastError = Some(e.getMessage)) }

  def subscriptions(state: DashboardState): Sub[Msg] = Sub.batch(
    Sub.http.pollMs(s"${state.serverUrl}/api/modules", pollMs, ModulesResp.apply),
    Sub.http.pollMs(s"${state.serverUrl}/api/stats/overview", pollMs, OverviewResp.apply),
    Sub.http.pollMs(s"${state.serverUrl}/api/stats/current", pollMs, CurrentResp.apply),
    Sub.http.pollMs(s"${state.serverUrl}/api/stats/history", pollMs, HistoryResp.apply),
    Sub.onKeyPress {
      case Key.Char('q') => Some(Quit)
      case _             => None
    }
  )

  def view(state: DashboardState): Element = {
    val statsLine = state.overview match {
      case Some(o) =>
        val h = o.uptimeSecs / 3600
        val m = (o.uptimeSecs % 3600) / 60
        val s = o.uptimeSecs % 60
        s"Served: ${o.totalRequestsServed}  Errors: ${o.totalErrors}  Uptime: ${h}h ${m}m ${s}s"
      case None => "Waiting for data..."
    }

    val modulesTable = table(
      headers = Seq("Module", "Type", "Deps"),
      rows = state.modules.map(m => Seq(m.id, m.`type`, m.deps.toString))
    ).border(Border.Round)

    val currentBox = box("Current Requests")(
      if state.currentRequests.isEmpty then "(none)".color(Color.BrightBlack)
      else {
        val items = state.currentRequests.map(r =>
          rowTight(
            Text(r.requestId),
            Text(s"  ${r.taskName}"),
            Text(s"  [${r.moduleIds.mkString(", ")}]")
          )
        )
        layout(items*)
      }
    ).border(Border.Round).color(Color.BrightBlue)

    val historyBox = box("Recent History")(
      if state.history.isEmpty then "(none)".color(Color.BrightBlack)
      else {
        val items = state.history.map { h =>
          val icon = if h.success then "✓".color(Color.Green) else "✗".color(Color.Red)
          rowTight(
            icon,
            Text(s" ${h.taskName}"),
            Text(s"  ${h.durationMs}ms"),
            Text(s"  [${h.moduleIds.mkString(", ")}]")
          )
        }
        layout(items*)
      }
    ).border(Border.Round).color(Color.BrightMagenta)

    val errorBanner = state.lastError.map { err =>
      banner(s"  $err").color(Color.Red).bg(Color.BrightBlack)
    }

    layout(
      banner(s"Deder TUI Dashboard — ${state.serverUrl} (q to quit)")
        .border(Border.Double)
        .color(Color.Cyan)
        .style(Style.Bold),
      section("Stats")(statsLine.color(Color.BrightGreen)),
      errorBanner.getOrElse(empty),
      columns(
        modulesTable,
        layout(currentBox, historyBox)
      )
    )
  }
}
