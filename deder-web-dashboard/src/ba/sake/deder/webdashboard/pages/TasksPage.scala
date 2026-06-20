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
      refreshMs: Int
  ): Html =
    html"""
      <h3>Tasks</h3>
      ${triggerForm(internals, project)}
      <div id="log-table" hx-get="/tasks/log-table" hx-trigger="every ${refreshMs}ms" hx-swap="outerHTML">
        ${logTable(log)}
      </div>
    """

  def triggerForm(internals: DederProjectInternals, project: DederProject): Html =
    val taskNames = internals.loadedPlugins
      .flatMap(_.taskNames)
      .distinct
      .sorted
    val moduleIds = if project != null && project.modules != null then
      import scala.jdk.CollectionConverters.*
      project.modules.asScala.toSeq.map(_.id).sorted
    else Seq.empty

    val taskOpts = taskNames.map(n => html"""<option value="$n" />""")
    val moduleOpts = moduleIds.map(m => html"""<option value="$m" />""")

    html"""
      <form hx-get="/tasks/run" hx-target="#log-table" hx-swap="afterbegin"
            style="display: flex; gap: 0.5rem; align-items: center; margin-bottom: 0.75rem;">
        <input list="task-list" name="taskName" placeholder="Search task..." autocomplete="off" required
               style="flex: 2;" />
        <datalist id="task-list">$taskOpts</datalist>
        <input list="module-list" name="moduleIds" value="*" placeholder="* (all modules)" autocomplete="off"
               style="flex: 1;" />
        <datalist id="module-list">$moduleOpts</datalist>
        <button type="submit">Run</button>
      </form>
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
    val hasContent = e.output.nonEmpty || e.outcomes.nonEmpty || e.error.isDefined
    if hasContent then
      html"""<span style="cursor: pointer; font-size: 0.8rem;"
             onclick="var r=document.getElementById('$rowId');r.style.display=r.style.display==='none'?'':'none'">▶</span>"""
    else Html("")

  private def expandedRow(e: ExecEntry): Html =
    val rowId = s"exec-${e.execId}-detail"
    val outputSection = if e.output.nonEmpty then
      html"""<div style="max-height: 200px; overflow-y: auto; background: var(--pico-code-background-color);
             padding: 0.3rem; font-family: monospace; font-size: 0.75rem; white-space: pre-wrap; margin-top: 0.25rem;">
             ${e.output.takeRight(10000)}</div>"""
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

    val showRow = e.output.nonEmpty || e.outcomes.nonEmpty || e.error.isDefined
    html"""
      <tr id="$rowId" style="display: none;">
        <td colspan="7" style="padding: 0.3rem 0.6rem;">
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
