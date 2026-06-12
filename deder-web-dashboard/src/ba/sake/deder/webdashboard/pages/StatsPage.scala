package ba.sake.deder.webdashboard.pages

import java.time.Duration
import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.*
import ba.sake.deder.webdashboard.server.ApiRoutes.{ApiTaskAggregate, ApiModuleAggregate, ApiErrorSummaryEntry}

object StatsPage {

  def fullPage(refreshMs: Int): Html =
    html"""
      <h2>Aggregates</h2>
      <div x-data="{ autoRefresh: true }">
        <div style="display:flex; align-items:center; gap:0.5rem; margin-bottom:0.5rem;">
          <label style="display:flex; align-items:center; gap:0.25rem; cursor:pointer;">
            <input type="checkbox" x-model="autoRefresh">
            <span>Auto-refresh</span>
          </label>
        </div>

        <h3>Per-Task Statistics</h3>
        <div id="task-aggregates-container"
             hx-get="/stats/task-aggregates" hx-swap="innerHTML"
             hx-trigger="load, refresh"
             :hx-trigger="autoRefresh ? 'load, every ${refreshMs}ms, refresh' : 'load, refresh'">
          <p>Loading stats...</p>
        </div>

        <h3>Top Time Consumers</h3>
        <div id="top-offenders-container"
             hx-get="/stats/top-offenders" hx-swap="innerHTML"
             hx-trigger="load, refresh"
             :hx-trigger="autoRefresh ? 'load, every ${refreshMs}ms, refresh' : 'load, refresh'">
          <p>Loading...</p>
        </div>

        <h3>Error Summary</h3>
        <div id="error-summary-container"
             hx-get="/stats/error-summary" hx-swap="innerHTML"
             hx-trigger="load, refresh"
             :hx-trigger="autoRefresh ? 'load, every ${refreshMs}ms, refresh' : 'load, refresh'">
          <p>Loading...</p>
        </div>
      </div>
    """

  /** Render the per-task aggregates table. */
  def taskAggregatesTable(aggregates: Seq[ApiTaskAggregate]): Html = {
    if aggregates.isEmpty then html"""<p><em>No completed tasks yet.</em></p>"""
    else
      val rows = aggregates.map { agg =>
        html"""
        <tbody hx-get="/stats/module-breakdown?task=${agg.taskName}&expanded=true"
               hx-trigger="click" hx-swap="outerHTML" style="cursor:pointer;">
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
        <tr class="module-row" style="font-size:0.85rem; background:var(--pico-muted-border-color);">
          <td style="padding-left:2rem;">${m.moduleId}</td>
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
      <tbody hx-get="/stats/collapse-task?task=${agg.taskName}"
             hx-trigger="click" hx-swap="outerHTML" style="cursor:pointer;">
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
      <tbody hx-get="/stats/module-breakdown?task=${agg.taskName}&expanded=true"
             hx-trigger="click" hx-swap="outerHTML" style="cursor:pointer;">
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

  /** Render top offenders as a compact card list. */
  def topOffenders(offenders: Seq[ApiTaskAggregate]): Html = {
    if offenders.isEmpty then html"""<p><em>No completed tasks yet.</em></p>"""
    else
      val cards = offenders.zipWithIndex.map { case (agg, idx) =>
        val num = idx + 1
        html"""
          <div class="stat-card" style="text-align:left; min-width:200px;">
            <div class="label">#$num</div>
            <div><strong>${agg.taskName}</strong></div>
            <div style="font-size:0.8rem;">
              ${formatMs(agg.totalTimeMs)} total &middot;
              ${agg.invocations} invoc &middot;
              avg ${formatMs(agg.avgTimeMs)} &middot;
              ${agg.errors} errors
            </div>
          </div>
        """
      }
      html"""<div style="display:flex; flex-wrap:wrap; gap:0.5rem;">${cards}</div>"""
  }

  /** Render error summary table. */
  def errorSummary(errors: Seq[ApiErrorSummaryEntry]): Html = {
    if errors.isEmpty then html"""<p style="color:var(--pico-color-green-400);">No errors recorded.</p>"""
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
