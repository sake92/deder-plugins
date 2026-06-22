package ba.sake.deder.webdashboard.pages

import java.time.Duration
import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.*
import ba.sake.deder.webdashboard.{ApiTaskAggregate, ApiModuleAggregate, ApiErrorSummaryEntry}

object StatsPage {

  def fullPage(refreshMs: Int): Html =
    html"""
      <div class="controls-bar">
        <label>
          ${autoRefreshCheckbox(true)}
          <span>Auto-refresh</span>
        </label>
      </div>

      <h3>Per-Task Statistics</h3>
      <div id="task-aggregates-container"
           hx-get="/stats/task-aggregates" hx-swap="innerHTML"
           hx-trigger="load, every ${refreshMs}ms, refresh">
        <p>Loading stats...</p>
      </div>

      <h3>Heaviest Modules</h3>
      <div id="module-aggregates-container"
           hx-get="/stats/module-aggregates" hx-swap="innerHTML"
           hx-trigger="load, every ${refreshMs}ms, refresh">
        <p>Loading...</p>
      </div>

      <h3>Top Time Consumers</h3>
      <div id="top-offenders-container"
           hx-get="/stats/top-offenders" hx-swap="innerHTML"
           hx-trigger="load, every ${refreshMs}ms, refresh">
        <p>Loading...</p>
      </div>

      <h3>Error Summary</h3>
      <div id="error-summary-container"
           hx-get="/stats/error-summary" hx-swap="innerHTML"
           hx-trigger="load, every ${refreshMs}ms, refresh">
        <p>Loading...</p>
      </div>
    """

  def autoRefreshCheckbox(enabled: Boolean): Html =
    if enabled then
      html"""<input type="checkbox" id="auto-refresh-cb" checked
                     hx-get="/stats/auto-refresh/stats?enabled=false" hx-trigger="change" hx-swap="outerHTML">"""
    else
      html"""<input type="checkbox" id="auto-refresh-cb"
                     hx-get="/stats/auto-refresh/stats?enabled=true" hx-trigger="change" hx-swap="outerHTML">"""

  def autoRefreshOob(enabled: Boolean, refreshMs: Int): Html = {
    val trigger = if enabled then s"load, every ${refreshMs}ms, refresh" else "load, refresh"
    html"""
      <div id="task-aggregates-container" hx-get="/stats/task-aggregates" hx-swap="innerHTML" hx-trigger="${trigger}" hx-swap-oob="true"></div>
      <div id="module-aggregates-container" hx-get="/stats/module-aggregates" hx-swap="innerHTML" hx-trigger="${trigger}" hx-swap-oob="true"></div>
      <div id="top-offenders-container" hx-get="/stats/top-offenders" hx-swap="innerHTML" hx-trigger="${trigger}" hx-swap-oob="true"></div>
      <div id="error-summary-container" hx-get="/stats/error-summary" hx-swap="innerHTML" hx-trigger="${trigger}" hx-swap-oob="true"></div>
    """
  }

  /** Render the per-task aggregates table. */
  def taskAggregatesTable(aggregates: Seq[ApiTaskAggregate]): Html = {
    if aggregates.isEmpty then html"""<p><em>No completed tasks yet.</em></p>"""
    else
      val rows = aggregates.map { agg =>
        html"""
        <tbody class="stats-clickable" hx-get="/stats/module-breakdown?task=${agg.taskName}&expanded=true"
               hx-trigger="click" hx-swap="outerHTML">
          <tr>
            <td>▶ ${agg.taskName}</td>
            <td>${agg.invocations}</td>
            <td>${agg.errors}</td>
            <td>${formatMs(agg.totalTimeMs)}</td>
            <td>${formatMs(agg.avgTimeMs)}</td>
            <td>${formatMs(agg.minTimeMs)}</td>
            <td>${formatMs(agg.maxTimeMs)}</td>
          </tr>
        </tbody>
        """
      }
      html"""
        <table>
          <thead><tr><th>Task</th><th>Invocations</th><th>Errors</th><th>Total</th><th>Avg</th><th>Min</th><th>Max</th></tr></thead>
          ${rows}
        </table>
      """
  }

  /** Render expanded task row with module breakdown sub-rows. */
  def expandedTaskRow(agg: ApiTaskAggregate, modules: Seq[ApiModuleAggregate]): Html = {
    val moduleRows = modules.map { m =>
      html"""
        <tr>
          <td class="module-id-col">${m.moduleId}</td>
          <td>${m.invocations}</td>
          <td>${m.errors}</td>
          <td>${formatMs(m.totalTimeMs)}</td>
          <td>${formatMs(m.avgTimeMs)}</td>
          <td>${formatMs(m.minTimeMs)}</td>
          <td>${formatMs(m.maxTimeMs)}</td>
        </tr>
      """
    }
    html"""
      <tbody class="stats-clickable" hx-get="/stats/collapse-task?task=${agg.taskName}"
             hx-trigger="click" hx-swap="outerHTML">
        <tr>
          <td>▼ ${agg.taskName}</td>
          <td>${agg.invocations}</td>
          <td>${agg.errors}</td>
          <td>${formatMs(agg.totalTimeMs)}</td>
          <td>${formatMs(agg.avgTimeMs)}</td>
          <td>${formatMs(agg.minTimeMs)}</td>
          <td>${formatMs(agg.maxTimeMs)}</td>
        </tr>
        ${moduleRows}
      </tbody>
    """
  }

  /** Render collapsed task row (returned from /stats/collapse-task). */
  def collapsedTaskRow(agg: ApiTaskAggregate): Html = {
    html"""
      <tbody class="stats-clickable" hx-get="/stats/module-breakdown?task=${agg.taskName}&expanded=true"
             hx-trigger="click" hx-swap="outerHTML">
        <tr>
          <td>▶ ${agg.taskName}</td>
          <td>${agg.invocations}</td>
          <td>${agg.errors}</td>
          <td>${formatMs(agg.totalTimeMs)}</td>
          <td>${formatMs(agg.avgTimeMs)}</td>
          <td>${formatMs(agg.minTimeMs)}</td>
          <td>${formatMs(agg.maxTimeMs)}</td>
        </tr>
      </tbody>
    """
  }

  /** Render heaviest modules as a compact card list (cross-task). */
  def moduleAggregatesSection(aggregates: Seq[ApiModuleAggregate]): Html = {
    if aggregates.isEmpty then html"""<p><em>No completed tasks yet.</em></p>"""
    else
      val cards = aggregates.zipWithIndex.map { case (agg, idx) =>
        val num = idx + 1
        html"""
          <article>
            <small>#$num</small>
            <div><strong>${agg.moduleId}</strong></div>
            <small>
              ${formatMs(agg.totalTimeMs)} total &middot;
              ${agg.invocations} invoc &middot;
              avg ${formatMs(agg.avgTimeMs)} &middot;
              ${agg.errors} errors
            </small>
          </article>
        """
      }
      html"""<div class="grid">${cards}</div>"""
  }

  /** Render top offenders as a compact card list. */
  def topOffenders(offenders: Seq[ApiTaskAggregate]): Html = {
    if offenders.isEmpty then html"""<p><em>No completed tasks yet.</em></p>"""
    else
      val cards = offenders.zipWithIndex.map { case (agg, idx) =>
        val num = idx + 1
        html"""
          <article>
            <small>#$num</small>
            <div><strong>${agg.taskName}</strong></div>
            <small>
              ${formatMs(agg.totalTimeMs)} total &middot;
              ${agg.invocations} invoc &middot;
              avg ${formatMs(agg.avgTimeMs)} &middot;
              ${agg.errors} errors
            </small>
          </article>
        """
      }
      html"""<div class="grid">${cards}</div>"""
  }

  /** Render error summary table. */
  def errorSummary(errors: Seq[ApiErrorSummaryEntry]): Html = {
    if errors.isEmpty then html"""<p><em>No errors recorded.</em></p>"""
    else
      val rows = errors.map { e =>
        html"""<tr><td>${e.taskName}</td><td>${e.errorCount}</td><td>${e.moduleIds.mkString(", ")}</td></tr>"""
      }
      html"""
        <table>
          <thead><tr><th>Task</th><th>Count</th><th>Modules</th></tr></thead>
          <tbody>${rows}</tbody>
        </table>
      """
  }

  private def formatMs(ms: Long): String = {
    if ms < 1000 then s"${ms}ms"
    else if ms < 60000 then f"${ms / 1000.0}%.1fs"
    else {
      val totalSecs = ms / 1000
      val m = totalSecs / 60
      val s = totalSecs % 60
      f"${m}m ${s}s"
    }
  }
}
