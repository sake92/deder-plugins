package ba.sake.deder.webdashboard.pages

import java.time.{Duration, Instant}
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import scala.jdk.DurationConverters.*
import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.*

object LivePage {
  def fullPage(internals: DederProjectInternals, refreshMs: Int, projectRoot: String): Html =
    html"""
      <h2>Live</h2>
      <div x-data="{ autoRefresh: true }">
        <div style="display:flex; align-items:center; gap:0.5rem; margin-bottom:0.5rem;">
          <label style="display:flex; align-items:center; gap:0.25rem; cursor:pointer;">
            <input type="checkbox" x-model="autoRefresh">
            <span>Auto-refresh</span>
          </label>
        </div>

        <div id="live-overview"
             hx-get="/stats/overview"
             hx-trigger="load, refresh"
             hx-swap="innerHTML"
             :hx-trigger="autoRefresh ? 'load, every ${refreshMs}ms, refresh' : 'load, refresh'">
          <p>Loading overview...</p>
        </div>

        <h3>Current Requests</h3>
        <div id="live-current"
             hx-get="/stats/current"
             hx-trigger="load, refresh"
             hx-swap="innerHTML"
             :hx-trigger="autoRefresh ? 'load, every ${refreshMs}ms, refresh' : 'load, refresh'">
          <p>Loading current requests...</p>
        </div>
      </div>
    """

  def overviewCards(internals: DederProjectInternals): Html = {
    val uptime = internals.serverUptime
    val days = uptime.toDays
    val hours = uptime.toHours % 24
    val minutes = uptime.toMinutes % 60
    val seconds = uptime.toSeconds % 60
    val parts = Seq(
      if days > 0 then Some(s"${days}d") else None,
      if hours > 0 || days > 0 then Some(s"${hours}h") else None,
      if minutes > 0 || days > 0 || hours > 0 then Some(s"${minutes}m") else None,
      Some(s"${seconds}s")
    ).flatten
    val uptimeStr = parts.mkString(" ")
    html"""
      <div style="display: flex; flex-wrap: wrap;">
        <div class="stat-card">
          <div class="label">Total Requests</div>
          <div class="value">${internals.totalRequestsServed}</div>
        </div>
        <div class="stat-card">
          <div class="label">Total Errors</div>
          <div class="value" style="color: ${
        if internals.totalErrors > 0 then "var(--pico-color-red-400)" else "var(--pico-color-green-400)"
      }">${internals.totalErrors}</div>
        </div>
        <div class="stat-card">
          <div class="label">Uptime</div>
          <div class="value" style="font-size:1rem">$uptimeStr</div>
        </div>
      </div>
    """
  }

  def currentRequestsTable(internals: DederProjectInternals): Html = {
    val requests = internals.currentRequests
    if requests.isEmpty then html"""<p><em>No requests currently executing.</em></p>"""
    else
      val rows = requests.map { req =>
        val elapsed = Duration.between(req.startTime, Instant.now())
        val elapsedStr = formatElapsed(elapsed)
        val startedStr = formatDateTime(req.startTime)
        val clientStr = formatCallerType(req.caller)
        html"""<tr><td>${startedStr}</td><td>$clientStr</td><td>${req.taskName}</td><td>${req.moduleIds.mkString(
            ", "
          )}</td><td>${elapsedStr}</td></tr>"""
      }
      html"""
        <table>
          <thead><tr><th>Started</th><th>Client</th><th>Task</th><th>Modules</th><th>Elapsed</th></tr></thead>
          <tbody>${rows}</tbody>
        </table>
      """
  }

  private def formatCallerType(ct: CallerType): String = ct match
    case CallerType.Cli => "CLI"
    case CallerType.Bsp => "BSP"
    case null => ct.toString

  private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault())

  private def formatDateTime(instant: Instant): String =
    dateTimeFormatter.format(instant)

  private def formatElapsed(duration: Duration): String = {
    val totalSecs = duration.getSeconds
    val h = totalSecs / 3600
    val m = (totalSecs % 3600) / 60
    val s = totalSecs % 60
    val ms = duration.toMillisPart
    if h > 0 then f"${h}h ${m}m ${s}s"
    else if m > 0 then f"${m}m ${s}s"
    else if s > 0 then f"${s}.${ms / 100}%01ds"
    else f"${ms}ms"
  }
}
