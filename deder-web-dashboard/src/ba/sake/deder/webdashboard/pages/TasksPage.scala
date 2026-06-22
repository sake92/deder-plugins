package ba.sake.deder.webdashboard.pages

import java.time.{Duration, Instant}
import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.*
import ba.sake.deder.config.DederProject
import ba.sake.deder.webdashboard.server.{TaskExecutionLog, ExecEntry, ExecStatus}

object TasksPage {

  def fullPage(
      log: TaskExecutionLog,
      internals: DederProjectInternals,
      project: DederProject,
      refreshMs: Int,
      allTasks: Seq[TaskInfo]
  ): Html =
    html"""
      <div style="display:flex; align-items:center; gap:0.5rem; margin-bottom:0.5rem;">
        <label style="cursor:pointer;">
          ${autoRefreshCheckbox(true)}
          <span>Auto-refresh</span>
        </label>
      </div>
      ${triggerForm(allTasks, project)}
      ${logTableContainer(log, refreshMs)}
      <script>
        function filterLogLines(preId, level) {
          var pre = document.getElementById(preId);
          if (!pre.getAttribute('data-original')) pre.setAttribute('data-original', pre.textContent);
          var original = pre.getAttribute('data-original');
          var weights = { DEBUG:0, INFO:1, WARN:2, ERROR:3 };
          var threshold = weights[level];
          var lines = original.split('\n');
          pre.textContent = lines.filter(function(line) {
            for (var l in weights) {
              if (line.indexOf('[' + l + ']') === 0) return weights[l] >= threshold;
            }
            return true;
          }).join('\n');
        }
      </script>
    """

  def autoRefreshCheckbox(enabled: Boolean): Html =
    if enabled then
      html"""<input type="checkbox" id="tasks-auto-refresh-cb" checked
                     hx-get="/tasks/auto-refresh?enabled=false" hx-trigger="change" hx-swap="outerHTML">"""
    else
      html"""<input type="checkbox" id="tasks-auto-refresh-cb"
                     hx-get="/tasks/auto-refresh?enabled=true" hx-trigger="change" hx-swap="outerHTML">"""

  def autoRefreshOob(enabled: Boolean, refreshMs: Int): Html =
    val trigger = if enabled then s"load, every ${refreshMs}ms" else "load, refresh"
    html"""<div id="log-table" hx-get="/tasks/log-table" hx-trigger="${trigger}" hx-swap="innerHTML" hx-swap-oob="true"></div>"""

  def triggerForm(allTasks: Seq[TaskInfo], project: DederProject): Html =
    val tasks = allTasks
      .filter(t => !t.internal)
      .sortBy(t => t.name)
    val taskOpts = tasks.map { t =>
      html"""<option value="${t.name}">${t.name} — ${t.description}</option>"""
    }
    val moduleIds = if project != null && project.modules != null then
      import scala.jdk.CollectionConverters.*
      project.modules.asScala.toSeq.map(_.id).sorted
    else Seq.empty
    val moduleOpts = moduleIds.map(m => html"""<option value="$m" />""")

    html"""
      <form hx-get="/tasks/run" hx-target="#log-table" hx-swap="innerHTML"
            style="display: flex; gap: 0.5rem; align-items: end; margin-bottom: 0.75rem; flex-wrap: wrap;">
        <div style="flex: 2; min-width: 180px;">
          <label for="task-input" style="font-size: 0.75rem; display: block; margin-bottom: 0.15rem;">Task</label>
          <input list="task-list" id="task-input" name="taskName" placeholder="Search..." autocomplete="off" required
                 style="width: 100%;" />
          <datalist id="task-list">$taskOpts</datalist>
        </div>
        <div style="flex: 2; min-width: 180px;">
          <label for="module-input" style="font-size: 0.75rem; display: block; margin-bottom: 0.15rem;">Modules</label>
          <input list="module-list" id="module-input" name="moduleIds" value="*"
                 placeholder="* (all) or comma-separated" autocomplete="off"
                 style="width: 100%;" />
          <datalist id="module-list">$moduleOpts</datalist>
        </div>
        <button type="submit" style="white-space: nowrap; margin-bottom: 0px;">Run</button>
      </form>
      ${if moduleIds.nonEmpty then
        html"""<div style="font-size: 0.75rem; color: var(--pico-muted-color); margin-bottom: 0.5rem;">
          Known modules: ${moduleIds.map(m => html"""<code style="font-size:0.72rem; margin-right:0.2rem;">$m</code>""")}
        </div>"""
      else Html("")}
    """

  def logTableContainer(log: TaskExecutionLog, refreshMs: Int): Html =
    html"""
      <div id="log-table" hx-get="/tasks/log-table" hx-trigger="load, every ${refreshMs}ms" hx-swap="innerHTML">
        ${logTable(log)}
      </div>
    """

  def logTable(log: TaskExecutionLog): Html =
    val entries = log.recent(50)
    if entries.isEmpty then
      html"""<p style="color: var(--pico-muted-color); font-style: italic;">No web-triggered executions yet.</p>"""
    else
      val rows = entries.zipWithIndex.map { case (e, idx) => logRow(e, idx + 1) }
      html"""
        <table style="font-size: 0.85rem;">
          <thead><tr>
            <th>#</th><th>Task</th><th>Modules</th><th>Start</th><th>Status</th><th>Duration</th><th></th>
          </tr></thead>
          <tbody>$rows</tbody>
        </table>
      """

  private def logRow(e: ExecEntry, num: Int): Html =
    val startStr = formatTime(e.startTime)
    val durationStr = e.endTime match
      case Some(end) => formatDuration(Duration.between(e.startTime, end))
      case None =>
        val running = Duration.between(e.startTime, Instant.now())
        formatDuration(running)

    val modulesStr = if e.moduleIds.isEmpty then "*" else e.moduleIds.mkString(", ")

    val statusClass = e.status match
      case ExecStatus.SUCCESS  => "success"
      case ExecStatus.FAILURE  => "failure"
      case ExecStatus.RUNNING  => "running"
      case ExecStatus.PENDING  => "pending"
      case ExecStatus.CANCELLED => "failure"

    val cancelBtn = e.status match
      case ExecStatus.RUNNING | ExecStatus.PENDING =>
        html"""<button class="cancel-btn" hx-post="/tasks/cancel?execId=${e.execId}"
               hx-target="#log-table" hx-swap="outerHTML"
               style="font-size: 0.7rem; padding: 0.05rem 0.4rem;">Cancel</button>"""
      case _ => Html("")

    html"""
      <tr id="exec-${e.execId}">
        <td>$num</td>
        <td>${e.taskName}</td>
        <td>$modulesStr</td>
        <td>$startStr</td>
        <td>
          <span class="state-badge $statusClass" style="font-size: 0.7rem;">
            ${e.status.toString}
          </span>
          $cancelBtn
        </td>
        <td>$durationStr</td>
        <td>
          ${expandBtn(e)}
        </td>
      </tr>
      ${expandedRow(e)}
    """

  private def expandBtn(e: ExecEntry): Html =
    val rowId = s"exec-${e.execId}-detail"
    val preId = s"exec-${e.execId}-log"
    val hasContent = e.output.nonEmpty || e.outcomes.nonEmpty || e.error.isDefined
    if hasContent then
      html"""<span style="cursor: pointer; font-size: 0.75rem;"
             onclick="var r=document.getElementById('${rowId}');var v=r.style.display==='none';r.style.display=v?'':'none';if(v)filterLogLines('${preId}','INFO')">📋</span>"""
    else Html("")

  private def expandedRow(e: ExecEntry): Html =
    val rowId = s"exec-${e.execId}-detail"
    val preId = s"exec-${e.execId}-log"
    val combinedOutput = e.output.trim + e.renderedSummary.map(s => s"\n\n$s").getOrElse("")

    val logFilter = if combinedOutput.contains("[") then
      html"""
        <div style="display:flex; align-items:center; gap:0.3rem; margin-bottom:0.15rem;">
          <label style="font-size:0.7rem;">Log level:</label>
          <select onchange="filterLogLines('$preId', this.value)" style="font-size:0.7rem; padding:0.05rem 0.2rem;">
            <option value="DEBUG">DEBUG</option>
            <option value="INFO" selected>INFO</option>
            <option value="WARN">WARN</option>
            <option value="ERROR">ERROR</option>
          </select>
        </div>
      """
    else Html("")

    val outputSection = if combinedOutput.nonEmpty then
      html"""<pre id="$preId" data-original style="max-height: 200px; overflow-y: auto; background: var(--pico-code-background-color);
               padding: 0.3rem; font-family: monospace; font-size: 0.75rem; margin-top: 0.25rem;">
${combinedOutput.takeRight(10000)}</pre>"""
    else Html("")

    val resultSection = if e.outcomes.nonEmpty then
      val outcomeRows = e.outcomes.map { o =>
        val status = if o.success then "OK" else "FAIL"
        val cached = if o.fromCache then " (cached)" else ""
        val err = o.error.map(msg => s": $msg").getOrElse("")
        html"""<tr><td>${o.moduleId}</td><td>$status$cached</td><td style="font-size:0.7rem;">$err</td></tr>"""
      }
      html"""
        <div style="margin-top: 0.25rem;">
          <table style="font-size: 0.78rem; width: 100%;">
            <thead><tr><th>Module</th><th>Outcome</th><th>Error</th></tr></thead>
            <tbody>$outcomeRows</tbody>
          </table>
        </div>
      """
    else Html("")

    val errorSection = e.error.map { msg =>
      html"""<div style="color: var(--pico-color-red-400); margin-top: 0.25rem;">Error: $msg</div>"""
    }.getOrElse(Html(""))

    html"""
      <tr id="$rowId" style="display: none;">
        <td colspan="7" style="padding: 0.3rem 0.6rem;">
          $logFilter
          $outputSection
          $resultSection
          $errorSection
        </td>
      </tr>
    """

  private def formatTime(instant: Instant): String =
    val lt = java.time.LocalTime.ofInstant(instant, java.time.ZoneId.systemDefault())
    f"${lt.getHour}%02d:${lt.getMinute}%02d:${lt.getSecond}%02d"

  private def formatDuration(d: Duration): String =
    if d.toHours > 0 then
      s"${d.toHours}h ${d.toMinutes % 60}m ${d.getSeconds % 60}s"
    else if d.toMinutes > 0 then
      s"${d.toMinutes}m ${d.getSeconds % 60}s"
    else if d.getSeconds > 0 then
      s"${d.getSeconds}.${d.toMillis % 1000 / 100}s"
    else
      s"${d.toMillis}ms"
}
