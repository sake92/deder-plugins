package ba.sake.deder.webdashboard.pages

import java.time.{Duration, Instant}
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import scala.jdk.DurationConverters.*
import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.*

object LivePage {
  def fullPage(refreshMs: Int): Html =
    html"""
      <div style="display:flex; align-items:center; gap:0.5rem; margin-bottom:0.5rem;">
        <label style="cursor:pointer;">
          ${autoRefreshCheckbox(true)}
          <span>Auto-refresh</span>
        </label>
      </div>

      <div id="live-overview"
           hx-get="/stats/overview"
           hx-trigger="load, every ${refreshMs}ms, refresh"
           hx-swap="innerHTML">
        <p>Loading overview...</p>
      </div>

      <h3>In-Memory Caches</h3>
      <div id="live-caches"
           hx-get="/stats/caches"
           hx-trigger="load, every ${refreshMs}ms, refresh"
           hx-swap="innerHTML">
        <p>Loading caches...</p>
      </div>

      <h3>Current Requests</h3>
      <div id="live-current"
           hx-get="/stats/current"
           hx-trigger="load, every ${refreshMs}ms, refresh"
           hx-swap="innerHTML">
        <p>Loading current requests...</p>
      </div>
    """

  def autoRefreshCheckbox(enabled: Boolean): Html =
    if enabled then
      html"""<input type="checkbox" id="auto-refresh-cb" checked
                     hx-get="/stats/auto-refresh/live?enabled=false" hx-trigger="change" hx-swap="outerHTML">"""
    else
      html"""<input type="checkbox" id="auto-refresh-cb"
                     hx-get="/stats/auto-refresh/live?enabled=true" hx-trigger="change" hx-swap="outerHTML">"""

  def autoRefreshOob(enabled: Boolean, refreshMs: Int): Html = {
    val trigger = if enabled then s"load, every ${refreshMs}ms, refresh" else "load, refresh"
    html"""
      <div id="live-overview" hx-get="/stats/overview" hx-trigger="${trigger}" hx-swap="innerHTML" hx-swap-oob="true"></div>
      <div id="live-caches" hx-get="/stats/caches" hx-trigger="${trigger}" hx-swap="innerHTML" hx-swap-oob="true"></div>
      <div id="live-current" hx-get="/stats/current" hx-trigger="${trigger}" hx-swap="innerHTML" hx-swap-oob="true"></div>
    """
  }

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

  def cachesTable(internals: DederProjectInternals): Html = {
    val caches = internals.inMemoryCachesStats
    if caches.isEmpty then html"""<p><em>No in-memory caches active.</em></p>"""
    else
      val rows = caches.toSeq.sortBy(_._1).map { case (name, stats) =>
        html"""<tr><td>$name</td><td>${stats.estimatedSize}</td><td>${stats.hitCount}</td><td>${stats.missCount}</td></tr>"""
      }
      html"""
        <table>
          <thead><tr><th>Name</th><th>Size</th><th>Hits</th><th>Misses</th></tr></thead>
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
