package ba.sake.deder.tuidashboard.app

import ba.sake.deder.tuidashboard.model.*
import ba.sake.tupson.*
import layoutz.*

enum Tab { case Modules, Tasks, Live, History, Aggregates, Info }
enum SortOrder { case Newest, Oldest, Longest, Shortest }
enum FocusField { case TaskInput, ModuleList, ExecLog }

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
    historySort: SortOrder = SortOrder.Newest,
    // --- Tasks tab ---
    taskInput: String = "compile",
    cursorPos: Int = 7,
    selectedTaskName: Option[String] = Some("compile"),
    toggledModuleIds: Set[String] = Set.empty,
    focus: FocusField = FocusField.TaskInput,
    taskExecutions: Seq[ApiExecEntry] = Seq.empty,
    expandedExecId: Option[String] = None,
    selectedModuleIdx: Int = 0,
    selectedExecIdx: Int = 0
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
// --- Tasks tab ---
case class TaskNameInput(char: Char) extends Msg
case object TaskNameBackspace extends Msg
case object TaskNameClear extends Msg
case object TaskNameConfirm extends Msg
case object FocusNext extends Msg
case class ToggleModule(moduleId: String) extends Msg
case object SelectAllModules extends Msg
case object DeselectAllModules extends Msg
case object RunTask extends Msg
case class TaskExecsResp(result: Either[String, String]) extends Msg
case class RunTaskResp(result: Either[String, String]) extends Msg
case class CancelExec(execId: String) extends Msg
case class ExpandExec(execId: String) extends Msg
case object CursorLeft extends Msg
case object CursorRight extends Msg
case object CursorHome extends Msg
case object CursorEnd extends Msg
case object ModuleUp extends Msg
case object ModuleDown extends Msg
case object ExecUp extends Msg
case object ExecDown extends Msg
case object ToggleCurrentModule extends Msg

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

    // --- Tasks tab ---
    case TaskNameInput(c) =>
      val (before, after) = state.taskInput.splitAt(state.cursorPos)
      val newInput = before + c + after
      (state.copy(taskInput = newInput, cursorPos = state.cursorPos + 1, lastError = None), Cmd.none)

    case TaskNameBackspace =>
      if state.cursorPos > 0 && state.cursorPos <= state.taskInput.length then
        val (before, after) = state.taskInput.splitAt(state.cursorPos)
        val newInput = before.init + after
        (state.copy(taskInput = newInput, cursorPos = state.cursorPos - 1, lastError = None), Cmd.none)
      else (state, Cmd.none)

    case TaskNameClear =>
      (state.copy(taskInput = "", cursorPos = 0, selectedTaskName = None, lastError = None), Cmd.none)

    case TaskNameConfirm =>
      val tasks = state.serverInfo.toSeq.flatMap(_.plugins).flatMap(_.taskNames)
      val filtered = if state.taskInput.isEmpty then tasks
      else tasks.filter(_.toLowerCase.contains(state.taskInput.toLowerCase))
      filtered.headOption match
        case Some(name) => (state.copy(selectedTaskName = Some(name), taskInput = name, cursorPos = name.length, lastError = None), Cmd.none)
        case None       => (state, Cmd.none)

    case FocusNext =>
      val next = state.focus match
        case FocusField.TaskInput   => FocusField.ModuleList
        case FocusField.ModuleList  => FocusField.ExecLog
        case FocusField.ExecLog     => FocusField.TaskInput
      (state.copy(focus = next, lastError = None), Cmd.none)

    case ToggleModule(moduleId) =>
      val newSet = if state.toggledModuleIds.contains(moduleId) then state.toggledModuleIds - moduleId
      else state.toggledModuleIds + moduleId
      (state.copy(toggledModuleIds = newSet, lastError = None), Cmd.none)

    case SelectAllModules =>
      val allIds = state.modules.map(_.id).toSet
      (state.copy(toggledModuleIds = allIds, lastError = None), Cmd.none)

    case DeselectAllModules =>
      (state.copy(toggledModuleIds = Set.empty, lastError = None), Cmd.none)

    case RunTask =>
      state.selectedTaskName match
        case Some(taskName) =>
          val moduleIdsQs = if state.toggledModuleIds.isEmpty then ""
          else state.toggledModuleIds.map(id => s"moduleIds=$id").mkString("&", "&", "")
          val url = s"${state.serverUrl}/api/tasks/run?taskName=$taskName$moduleIdsQs&logLevel=INFO"
          (state, Cmd.http.post(url, "", RunTaskResp.apply))
        case None =>
          (state.copy(lastError = Some("No task selected")), Cmd.none)

    case TaskExecsResp(Right(json)) =>
      safely(state)(_.copy(taskExecutions = json.parseJson[Seq[ApiExecEntry]], lastError = None))
    case TaskExecsResp(Left(err)) =>
      state.copy(lastError = Some(s"task execs: $err"))

    case RunTaskResp(Right(json)) =>
      safely(state) { s =>
        val result = json.parseJson[TaskRunResult]
        val err = result.error.map(e => s"Task error: $e")
        s.copy(lastError = err)
      }
    case RunTaskResp(Left(err)) =>
      state.copy(lastError = Some(s"run task: $err"))

    case CancelExec(execId) =>
      val url = s"${state.serverUrl}/api/tasks/cancel?execId=$execId"
      (state, Cmd.http.post(url, "", (_: Either[String, String]) => TaskExecsResp(Right("[]"))))

    case ExpandExec(execId) =>
      val newExpanded = if state.expandedExecId.contains(execId) then None else Some(execId)
      (state.copy(expandedExecId = newExpanded), Cmd.none)

    case ModuleUp =>
      val newIdx = if state.modules.isEmpty then 0 else Math.max(0, state.selectedModuleIdx - 1)
      (state.copy(selectedModuleIdx = newIdx), Cmd.none)

    case ModuleDown =>
      val newIdx = if state.modules.isEmpty then 0 else Math.min(state.modules.size - 1, state.selectedModuleIdx + 1)
      (state.copy(selectedModuleIdx = newIdx), Cmd.none)

    case ExecUp =>
      val newIdx = if state.taskExecutions.isEmpty then 0 else Math.max(0, state.selectedExecIdx - 1)
      (state.copy(selectedExecIdx = newIdx), Cmd.none)

    case ExecDown =>
      val newIdx = if state.taskExecutions.isEmpty then 0 else Math.min(state.taskExecutions.size - 1, state.selectedExecIdx + 1)
      (state.copy(selectedExecIdx = newIdx), Cmd.none)

    case ToggleCurrentModule =>
      state.modules.lift(state.selectedModuleIdx) match
        case Some(m) =>
          val newSet = if state.toggledModuleIds.contains(m.id) then state.toggledModuleIds - m.id
          else state.toggledModuleIds + m.id
          (state.copy(toggledModuleIds = newSet), Cmd.none)
        case None => (state, Cmd.none)

    case CursorLeft =>
      val newPos = Math.max(0, state.cursorPos - 1)
      (state.copy(cursorPos = newPos), Cmd.none)

    case CursorRight =>
      val newPos = Math.min(state.taskInput.length, state.cursorPos + 1)
      (state.copy(cursorPos = newPos), Cmd.none)

    case CursorHome =>
      (state.copy(cursorPos = 0), Cmd.none)

    case CursorEnd =>
      (state.copy(cursorPos = state.taskInput.length), Cmd.none)
  }

  private def taskCharOr(state: DashboardState, c: Char, otherwise: Msg): Option[Msg] =
    if state.activeTab == Tab.Tasks && state.focus == FocusField.TaskInput then Some(TaskNameInput(c))
    else Some(otherwise)

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
    Sub.http.pollMs(s"${state.serverUrl}/api/tasks", pollMs, TaskExecsResp.apply),
    Sub.onKeyPress {
      case Key.Char('1')       => taskCharOr(state, '1', ChangeTab(Tab.Modules))
      case Key.Char('2')       => taskCharOr(state, '2', ChangeTab(Tab.Tasks))
      case Key.Char('3')       => taskCharOr(state, '3', ChangeTab(Tab.Live))
      case Key.Char('4')       => taskCharOr(state, '4', ChangeTab(Tab.History))
      case Key.Char('5')       => taskCharOr(state, '5', ChangeTab(Tab.Aggregates))
      case Key.Char('6')       => taskCharOr(state, '6', ChangeTab(Tab.Info))
      case Key.Char('s')       => taskCharOr(state, 's', ToggleSort)
      case Key.Char('q')       => taskCharOr(state, 'q', Quit)
      case Key.Ctrl('c')       => Some(Quit)
      case Key.Ctrl('x')       => Some(Quit)
      // --- Tasks tab keys ---
      case Key.Tab             => if state.activeTab == Tab.Tasks then Some(FocusNext) else None
      case Key.Enter           => if state.activeTab == Tab.Tasks then
        state.expandedExecId match
          case Some(id) => Some(ExpandExec(id))
          case None =>
            state.focus match
              case FocusField.TaskInput  => Some(TaskNameConfirm)
              case FocusField.ExecLog    => Some(ExpandExec(state.taskExecutions.lift(state.selectedExecIdx).map(_.execId).getOrElse("")))
              case _                     => None
      else None
      case Key.Char('a')       => if state.activeTab == Tab.Tasks then
        if state.focus == FocusField.TaskInput then Some(TaskNameInput('a')) else if state.focus == FocusField.ModuleList then Some(SelectAllModules) else None
      else None
      case Key.Char('n')       => if state.activeTab == Tab.Tasks then
        if state.focus == FocusField.TaskInput then Some(TaskNameInput('n')) else if state.focus == FocusField.ModuleList then Some(DeselectAllModules) else None
      else None
      case Key.Char('r')       => if state.activeTab == Tab.Tasks then
        if state.focus == FocusField.TaskInput then Some(TaskNameInput('r')) else Some(RunTask)
      else None
      case Key.Char('c')       => if state.activeTab == Tab.Tasks then
        if state.focus == FocusField.TaskInput then Some(TaskNameInput('c'))
        else state.expandedExecId.map(id => CancelExec(id))
          .orElse(state.taskExecutions.lift(state.selectedExecIdx).map(e => CancelExec(e.execId)))
      else None
      case Key.Escape          => if state.activeTab == Tab.Tasks then
        state.expandedExecId match
          case Some(id) => Some(ExpandExec(id))
          case None     => if state.focus == FocusField.TaskInput then Some(TaskNameClear) else None
      else None
      case Key.Backspace       => if state.activeTab == Tab.Tasks && state.focus == FocusField.TaskInput then Some(TaskNameBackspace) else None
      case Key.Left            => if state.activeTab == Tab.Tasks && state.focus == FocusField.TaskInput then Some(CursorLeft) else None
      case Key.Right           => if state.activeTab == Tab.Tasks && state.focus == FocusField.TaskInput then Some(CursorRight) else None
      case Key.Home            => if state.activeTab == Tab.Tasks && state.focus == FocusField.TaskInput then Some(CursorHome) else None
      case Key.End             => if state.activeTab == Tab.Tasks && state.focus == FocusField.TaskInput then Some(CursorEnd) else None
      case Key.Up              => if state.activeTab == Tab.Tasks then
        state.focus match
          case FocusField.TaskInput  => None
          case FocusField.ModuleList => Some(ModuleUp)
          case FocusField.ExecLog    => Some(ExecUp)
      else None
      case Key.Down            => if state.activeTab == Tab.Tasks then
        state.focus match
          case FocusField.TaskInput  => None
          case FocusField.ModuleList => Some(ModuleDown)
          case FocusField.ExecLog    => Some(ExecDown)
      else None
      case Key.Char(' ')       => if state.activeTab == Tab.Tasks then
        if state.focus == FocusField.TaskInput then Some(TaskNameInput(' ')) else if state.focus == FocusField.ModuleList then Some(ToggleCurrentModule) else None
      else None
      case Key.Char(c)         => if state.activeTab == Tab.Tasks && state.focus == FocusField.TaskInput then Some(TaskNameInput(c)) else None
      case _                   => None
    }
  )

  def view(state: DashboardState): Element = {
    val tabBar = tabBarView(state.activeTab)

    val content = state.activeTab match
      case Tab.Modules    => modulesView(state)
      case Tab.Tasks      => tasksView(state)
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
      tab(2, "Tasks", Tab.Tasks),
      Text("  "),
      tab(3, "Live", Tab.Live),
      Text("  "),
      tab(4, "History", Tab.History),
      Text("  "),
      tab(5, "Aggregates", Tab.Aggregates),
      Text("  "),
      tab(6, "Info", Tab.Info),
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

  // --- Tab 2: Tasks ---

  private def tasksView(state: DashboardState): Element = {
    state.expandedExecId match
      case Some(execId) =>
        state.taskExecutions.find(_.execId == execId) match
          case Some(e) => execDetailView(state, e)
          case None    => splitTaskView(state)
      case None => splitTaskView(state)
  }

  private def splitTaskView(state: DashboardState): Element = {
    val triggerBox = taskTriggerView(state)
    val logBox = taskLogView(state)
    layout(triggerBox, logBox)
  }

  private def execDetailView(state: DashboardState, e: ApiExecEntry): Element = {
    val statusColor = e.status match
      case "SUCCESS"  => Color.Green
      case "FAILURE"  => Color.Red
      case "RUNNING"  => Color.Blue
      case "PENDING"  => Color.Yellow
      case _          => Color.Red

    val modulesStr = if e.moduleIds.isEmpty then "*" else e.moduleIds.mkString(", ")

    val outputSection = if e.output.nonEmpty then
      val lines = e.output.split('\n')
      val debugCount = lines.count(_.startsWith("[DEBUG]"))
      val filtered = lines.filter(l => !l.startsWith("[DEBUG]")).mkString("\n")
      val debugNote = if debugCount > 0 then Text(s"($debugCount debug lines hidden)").color(Color.BrightBlack) else empty
      layout(
        section("Output:")(
          layout(
            Text(filtered.takeRight(5000)).color(Color.BrightBlack),
            debugNote
          )
        )
      )
    else empty

    val errorSection = e.error.map { msg =>
      section("Error:")(msg.color(Color.Red))
    }.getOrElse(empty)

    val cancelHint = if e.status == "RUNNING" || e.status == "PENDING" then
      Text("  Esc/Enter = back  c = cancel").color(Color.BrightBlack)
    else
      Text("  Esc/Enter = back").color(Color.BrightBlack)

    box(s"Execution: ${e.taskName} ($modulesStr)")(
      layout(
        Text(s"Status: ${e.status}").color(statusColor),
        Text(s"Exec ID: ${e.execId}").color(Color.BrightBlack),
        outputSection,
        errorSection,
        Text("─" * 40).color(Color.BrightBlack),
        cancelHint
      )
    ).border(Border.Round)
  }

  private def taskTriggerView(state: DashboardState): Element = {
    val allTasks = state.serverInfo.toSeq.flatMap(_.plugins).flatMap(_.taskNames).distinct.sorted
    val filteredTasks = if state.taskInput.isEmpty then allTasks
    else allTasks.filter(_.toLowerCase.contains(state.taskInput.toLowerCase))

    val taskFieldLine = if state.focus == FocusField.TaskInput then
      val (before, after) = state.taskInput.splitAt(state.cursorPos)
      s"Task: $before▌$after  (Tab=modules, r=run, Esc=clear)"
    else
      s"Task: ${state.taskInput}  (Tab=modules, r=run, Esc=clear)"

    val taskList = if state.focus == FocusField.TaskInput && filteredTasks.nonEmpty then
      val items = filteredTasks.map(name =>
        if state.selectedTaskName.contains(name) then Text(s"  ▶ $name").color(Color.BrightGreen)
        else Text(s"    $name")
      )
      layout(items*)
    else empty

    val moduleHeader = if state.focus == FocusField.ModuleList then
      Text("Modules: [up/down=nav  space=toggle  a=all  n=none  r=run]").color(Color.Cyan)
    else if state.focus == FocusField.ExecLog then
      Text("Modules: [Tab=switch]").color(Color.BrightBlack)
    else
      Text("Modules: [Tab=switch  r=run]").color(Color.BrightBlack)

    val moduleList = if state.focus == FocusField.ModuleList then
      if state.modules.isEmpty then "(no modules loaded)".color(Color.BrightBlack)
      else
        val moduleItems = state.modules.zipWithIndex.map { case (m, i) =>
          val checked = if state.toggledModuleIds.contains(m.id) then "[x]" else "[ ]"
          val cursor = if i == state.selectedModuleIdx then "▶" else " "
          val elem = Text(s"$cursor $checked ${m.id}  (${m.`type`})")
          if i == state.selectedModuleIdx then elem.color(Color.Cyan) else elem
        }
        layout(moduleItems*)
    else
      val summary = if state.toggledModuleIds.isEmpty then "all modules" else s"${state.toggledModuleIds.size} selected"
      Text(s"  $summary").color(Color.BrightBlack)

    val runHint = state.selectedTaskName match
      case Some(name) => s"Press 'r' to run '$name'"
      case None       => "Select a task first"

    box("Trigger Task")(
      layout(
        Text(taskFieldLine),
        taskList,
        moduleHeader,
        moduleList,
        Text(s"  $runHint").color(Color.BrightBlack)
      )
    ).border(Border.Round).color(Color.BrightBlue)
  }

  private def taskLogView(state: DashboardState): Element = {
    if state.taskExecutions.isEmpty then
      box("Executions")("No executions yet.".color(Color.BrightBlack))
        .border(Border.Round)
    else
      val header = Text("  #  Task          Modules                              Status   Duration").style(Style.Bold)

      val items = state.taskExecutions.zipWithIndex.map { case (e, i) =>
        val cursor = if i == state.selectedExecIdx then "▶" else " "
        val modulesStr = if e.moduleIds.isEmpty then "*"
        else
          val s = e.moduleIds.mkString(", ")
          if s.length > 34 then s.take(31) + "..." else s
        val statusColor = e.status match
          case "SUCCESS"  => Color.Green
          case "FAILURE"  => Color.Red
          case "RUNNING"  => Color.Blue
          case "PENDING"  => Color.Yellow
          case _          => Color.Red
        val durationStr = e.endTimeMs match
          case Some(end) => s"${end - e.startTimeMs}ms"
          case None      => s"${System.currentTimeMillis() - e.startTimeMs}ms"

        val idxStr = f"${i + 1}%2d"
        val taskNamePadded = e.taskName.padTo(13, ' ')
        val modPadded = modulesStr.padTo(37, ' ')
        val statusPadded = e.status.padTo(9, ' ')

        val row = Text(s" $cursor$idxStr. $taskNamePadded$modPadded$statusPadded$durationStr").color(statusColor)
        if i == state.selectedExecIdx then row.color(Color.Cyan) else row
      }

      val runCancelHint =
        if state.focus == FocusField.ExecLog then
          Text("  enter=expand  c=cancel  up/down=navigate  Tab=switch").color(Color.Cyan)
        else if state.taskExecutions.exists(e => e.status == "RUNNING" || e.status == "PENDING") then
          Text("  Tab=switch  enter=expand  c=cancel").color(Color.BrightBlack)
        else
          Text("  Tab=switch  enter=expand").color(Color.BrightBlack)

      section("Executions")(
        layout(header +: items :+ runCancelHint*)
      )
  }

  // --- Tab 3: Live ---

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
