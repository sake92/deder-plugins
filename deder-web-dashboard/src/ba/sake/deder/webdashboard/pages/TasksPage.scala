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
      <div class="controls-bar">
        <label>
          ${autoRefreshCheckbox(true)}
          <span>Auto-refresh</span>
        </label>
      </div>
      ${triggerForm(allTasks, project)}
      ${logTableContainer(log, refreshMs)}

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
    html"""<div id="log-table" hx-get="/tasks/log-table" hx-trigger="${trigger}" hx-swap="innerHTML"
           hx-on::after-swap="Alpine.initTree(this)" hx-swap-oob="true"></div>"""

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
            class="trigger-form">
        <fieldset class="grid">
          <label>
            Task
            <input list="task-list" name="taskName" placeholder="Search..." autocomplete="off" required />
            <datalist id="task-list">$taskOpts</datalist>
          </label>
          <label>
            Modules
            <input list="module-list" name="moduleIds" placeholder="leave empty for all or comma-separated" autocomplete="off" />
            <datalist id="module-list">$moduleOpts</datalist>
          </label>
          <input type="submit" value="Run ▶" />
        </fieldset>
      </form>
    """

  def logTableContainer(log: TaskExecutionLog, refreshMs: Int): Html =
    html"""
      <div id="log-table" hx-get="/tasks/log-table" hx-trigger="load, every ${refreshMs}ms" hx-swap="innerHTML"
           hx-on::after-swap="Alpine.initTree(this)">
        ${logTable(log)}
      </div>
    """

  def logTable(log: TaskExecutionLog): Html =
    val entries = log.recent(50)
    if entries.isEmpty then
      html"""<p class="no-requests">No web-triggered executions yet.</p>"""
    else
      val rows = entries.zipWithIndex.map { case (e, idx) => logRow(e, idx + 1) }
      html"""
        <table class="compact">
          <thead><tr>
            <th>#</th><th>Task</th><th>Modules</th><th>Start</th><th>Status</th><th>Duration</th><th></th>
          </tr></thead>
          $rows
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
        html"""<button class="cancel-btn compact" hx-post="/tasks/cancel?execId=${e.execId}"
               hx-target="#log-table" hx-swap="outerHTML">Cancel</button>"""
      case _ => Html("")

    html"""
      <tbody x-data="{ level: 'INFO', originalLog: '', filteredLog() { if (!this.originalLog) return ''; const weights = { DEBUG:0, INFO:1, WARN:2, ERROR:3 }; const threshold = weights[this.level]; return this.originalLog.split('\n').filter(line => { for (const l of Object.keys(weights)) { if (line.indexOf('[' + l + ']') === 0) return weights[l] >= threshold; } return true; }).join('\n'); } }" x-init="originalLog = $$refs.logEl ? ($$refs.logEl.dataset.original || '') : ''">
        <tr id="exec-${e.execId}">
          <td>$num</td>
          <td>${e.taskName}</td>
          <td>$modulesStr</td>
          <td>$startStr</td>
          <td>
            <span class="state-badge compact $statusClass">
              ${e.status.toString}
            </span>
            $cancelBtn
          </td>
          <td>$durationStr</td>
          <td></td>
        </tr>
        ${detailSection(e)}
      </tbody>
    """

  private def detailSection(e: ExecEntry): Html =
    val combinedOutput = e.output.trim + e.renderedSummary.map(s => s"\n\n$s").getOrElse("")

    val logFilter = if combinedOutput.contains("[") then
      html"""
        <div class="log-toolbar">
          <label>Log level:</label>
          <select x-model="level">
            <option value="DEBUG">DEBUG</option>
            <option value="INFO" selected>INFO</option>
            <option value="WARN">WARN</option>
            <option value="ERROR">ERROR</option>
          </select>
        </div>
      """
    else Html("")

    val outputSection = if combinedOutput.nonEmpty then
      html"""<pre x-ref="logEl" x-text="filteredLog()" class="log-output" data-original="${combinedOutput.takeRight(10000)}">
${combinedOutput.takeRight(10000)}</pre>"""
    else Html("")

    val resultSection = if e.outcomes.nonEmpty then
      val outcomeRows = e.outcomes.map { o =>
        val status = if o.success then "OK" else "FAIL"
        val cached = if o.fromCache then " (cached)" else ""
        val err = o.error.map(msg => s": $msg").getOrElse("")
        html"""<tr><td>${o.moduleId}</td><td>$status$cached</td><td class="note">$err</td></tr>"""
      }
      html"""
        <div class="outcomes-section">
          <table class="outcomes-table">
            <thead><tr><th>Module</th><th>Outcome</th><th>Error</th></tr></thead>
            <tbody>$outcomeRows</tbody>
          </table>
        </div>
      """
    else Html("")

    val errorSection = e.error.map { msg =>
      html"""<div class="error-msg">Error: $msg</div>"""
    }.getOrElse(Html(""))

    html"""
      <tr>
        <td colspan="7" class="expand-pad">
          <details class="details-box">
            <summary>Details</summary>
            $logFilter
            $outputSection
            $resultSection
            $errorSection
          </details>
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
