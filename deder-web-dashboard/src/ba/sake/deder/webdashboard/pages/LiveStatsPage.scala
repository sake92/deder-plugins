package ba.sake.deder.webdashboard.pages

import java.time.{Duration, Instant}
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import scala.jdk.DurationConverters.*
import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.*

object LiveStatsPage {
  def fullStatsPage(internals: DederProjectInternals, refreshMs: Int): Html =
    html"""
      <h2>Live Stats</h2>
      <div hx-get="/stats/overview" hx-trigger="load, every ${refreshMs}ms" hx-swap="innerHTML">
        <p>Loading overview...</p>
      </div>
      <h3>Current Requests</h3>
      <div hx-get="/stats/current" hx-trigger="load, every ${refreshMs}ms" hx-swap="innerHTML">
        <p>Loading current requests...</p>
      </div>
      <h3>Recent History</h3>
      <div hx-get="/stats/history" hx-trigger="load, every ${refreshMs}ms" hx-swap="innerHTML">
        <p>Loading recent history...</p>
      </div>
    """

  def overviewCards(internals: DederProjectInternals): Html = {
    val uptime = internals.serverUptime
    // TODO if days > 0 etc..
    val totalSecs = uptime.toSeconds
    val days = uptime.toDays
    val hours = uptime.toHours
    val minutes = uptime.toMinutes
    val seconds = uptime.toSeconds
    val uptimeStr = s"${days}d ${hours}h ${minutes}m ${seconds}s"
    html"""
      <div style="display: flex; flex-wrap: wrap;">
        <div class="stat-card">
          <div class="label">Total Requests</div>
          <div class="value">${internals.totalRequestsServed}</div>
        </div>
        <div class="stat-card">
          <div class="label">Total Errors</div>
          <div class="value" style="color: ${
        if internals.totalErrors > 0 then "red" else "green"
      }">${internals.totalErrors}</div>
        </div>
        <div class="stat-card">
          <div class="label">Uptime</div>
          <div class="value" style="font-size:1.2rem">${uptimeStr}</div>
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
        html"""<tr><td>${startedStr}</td><td>${req.taskName}</td><td>${req.moduleIds.mkString(
            ", "
          )}</td><td>${elapsedStr}</td></tr>"""
      }
      html"""
        <table>
          <thead><tr><th>Started</th><th>Task</th><th>Modules</th><th>Elapsed</th></tr></thead>
          <tbody>${rows}</tbody>
        </table>
      """
  }

  def historyTable(internals: DederProjectInternals): Html = {
    val history = internals.recentHistory
    if history.isEmpty then html"""<p><em>No completed requests yet.</em></p>"""
    else
      val rows = history.map { req =>
        val statusClass = if req.success then "success" else "failure"
        val statusText = if req.success then "OK" else "FAIL"
        val startedStr = formatDateTime(req.startTime)
        val durStr = formatElapsed(req.duration)
        html"""<tr><td>${startedStr}</td><td>${req.taskName}</td><td>${req.moduleIds.mkString(
            ", "
          )}</td><td>${durStr}</td><td class="${statusClass}">${statusText}</td></tr>"""
      }
      html"""
        <table>
          <thead><tr><th>Started</th><th>Task</th><th>Modules</th><th>Duration</th><th>Status</th></tr></thead>
          <tbody>${rows}</tbody>
        </table>
      """
  }

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
