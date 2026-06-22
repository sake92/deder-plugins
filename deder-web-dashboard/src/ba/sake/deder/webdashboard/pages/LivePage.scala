package ba.sake.deder.webdashboard.pages

import java.time.{Duration, Instant}
import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.*
import ba.sake.deder.webdashboard.server.ApiRoutes.{ApiRequestStatus, ApiLockProgress, ApiTaskStageProgress, ApiRequestState}

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

      <h3>Requests</h3>
      <div id="live-requests"
           hx-get="/stats/requests"
           hx-trigger="load, every ${refreshMs}ms, refresh"
           hx-swap="innerHTML">
        <p>Loading requests...</p>
      </div>

      <div style="display:flex; align-items:center; gap:1rem; margin-bottom:0.5rem;">
        <h3 style="margin:0">In-Memory Caches</h3>
        <button class="outline secondary"
                hx-post="/stats/caches/clear"
                hx-target="#live-caches"
                hx-swap="innerHTML">
          Clear All
        </button>
      </div>
      <div id="live-caches"
           hx-get="/stats/caches"
           hx-trigger="load, every ${refreshMs}ms, refresh"
           hx-swap="innerHTML">
        <p>Loading caches...</p>
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
      <div id="live-requests" hx-get="/stats/requests" hx-trigger="${trigger}" hx-swap="innerHTML" hx-swap-oob="true"></div>
      <div id="live-caches" hx-get="/stats/caches" hx-trigger="${trigger}" hx-swap="innerHTML" hx-swap-oob="true"></div>
    """
  }

  def requestSections(statuses: Seq[ApiRequestStatus]): Html = {
    val queued = statuses.filter(_.state == ApiRequestState.Queued)
    val acquiring = statuses.filter(_.state == ApiRequestState.AcquiringLocks)
    val executing = statuses.filter(_.state == ApiRequestState.Executing)

    if statuses.isEmpty then html"""<p><em>No requests in progress.</em></p>"""
    else
      html"""
        ${renderSection(queued, "Queued", "queued", renderQueuedRow)}
        ${renderSection(acquiring, "Acquiring Locks", "locks", renderAcquiringRow)}
        ${renderSection(executing, "Executing", "executing", renderExecutingRow)}
      """
  }

  private def renderSection(
    reqs: Seq[ApiRequestStatus],
    title: String,
    badgeClass: String,
    rowRenderer: ApiRequestStatus => Html
  ): Html = {
    if reqs.isEmpty then html""
    else
      val rows = reqs.map(rowRenderer)
      html"""
        <div class="request-section">
          <div class="section-header">
            <span class="state-badge $badgeClass">${title} (${reqs.size})</span>
          </div>
          <table>
            <thead><tr><th>Started</th><th>Client</th><th>Task</th><th style="width:20%">Modules</th><th>Details</th><th></th></tr></thead>
            <tbody>$rows</tbody>
          </table>
        </div>
      """
  }

  private def renderQueuedRow(req: ApiRequestStatus): Html = {
    val startedStr = formatDateTime(Instant.ofEpochMilli(req.startTimeMs))
    html"""
      <tr>
        <td style="white-space:nowrap">$startedStr</td>
        <td>${req.caller}</td>
        <td>${req.taskName}</td>
        <td style="font-size:0.8rem">${req.moduleIds.mkString(", ")}</td>
        <td>—</td>
        <td>${cancelButton(req.requestId)}</td>
      </tr>
    """
  }

  private def renderAcquiringRow(req: ApiRequestStatus): Html = {
    val startedStr = formatDateTime(Instant.ofEpochMilli(req.startTimeMs))
    val lockInfo = req.lockProgress.map { l =>
      val pct = if l.total > 0 then (l.acquired * 100) / l.total else 0
      val waitingOn = l.blockingOn.map(w => s"waiting on: $w").getOrElse("")
      val heldBy = l.heldBy.map(h => s" [held by $h]").getOrElse("")
      html"""
        <div class="progress-bar"><div class="progress-bar-fill locks" style="width:${pct}%"></div></div>
        <div class="progress-detail">Lock ${l.acquired}/${l.total}${if waitingOn.nonEmpty then s" — $waitingOn$heldBy" else ""}</div>
      """
    }.getOrElse(html"""<div class="progress-detail">Acquiring locks...</div>""")
    html"""
      <tr>
        <td style="white-space:nowrap">$startedStr</td>
        <td>${req.caller}</td>
        <td>${req.taskName}</td>
        <td style="font-size:0.8rem">${req.moduleIds.mkString(", ")}</td>
        <td>$lockInfo</td>
        <td>${cancelButton(req.requestId)}</td>
      </tr>
    """
  }

  private def renderExecutingRow(req: ApiRequestStatus): Html = {
    val startedStr = formatDateTime(Instant.ofEpochMilli(req.startTimeMs))
    val stageInfo = req.taskProgress.map { p =>
      val pct = if p.totalStages > 0 then (p.currentStage * 100) / p.totalStages else 100
      html"""
        <div class="progress-bar"><div class="progress-bar-fill stages" style="width:${pct}%"></div></div>
        <div class="progress-detail">Stage ${p.currentStage}/${p.totalStages} — done: ${p.completed}, fail: ${p.failed}, run: ${p.running}, pending: ${p.pending}</div>
      """
    }.getOrElse(html"""<div class="progress-detail">Executing...</div>""")
    html"""
      <tr>
        <td style="white-space:nowrap">$startedStr</td>
        <td>${req.caller}</td>
        <td>${req.taskName}</td>
        <td style="font-size:0.8rem">${req.moduleIds.mkString(", ")}</td>
        <td>$stageInfo</td>
        <td>${cancelButton(req.requestId)}</td>
      </tr>
    """
  }

  def cancelButton(requestId: String): Html =
    html"""
      <button class="cancel-btn"
              hx-post="/stats/cancel?requestId=$requestId"
              hx-swap="outerHTML"
              title="Cancel request">
        Cancel
      </button>
    """

  def cancelledBadge: Html =
    html"""<span style="font-size:0.75rem; color: var(--pico-muted-color);">Cancelled</span>"""

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

  def cachesClearedResponse(result: PurgeCachesResult, internals: DederProjectInternals): Html = {
    val summary = if result.cachesCleared == 0 && result.bspEntriesRemoved == 0 && result.historyEntriesRemoved == 0 then
      html"""<p><em>No caches were active — nothing to clear.</em></p>"""
    else
      val parts = Seq(
        Some(s"Cleared ${result.cachesCleared} cache(s)."),
        if result.bspEntriesRemoved > 0 then Some(s"${result.bspEntriesRemoved} BSP entries removed.") else None,
        if result.historyEntriesRemoved > 0 then Some(s"${result.historyEntriesRemoved} history entries removed.") else None
      ).flatten
      html"""<p style="color: var(--pico-color-green-400);">✅ ${parts.mkString(" ")}</p>"""
    val table = cachesTable(internals)
    html"""$summary$table"""
  }

  private val dateTimeFormatter: java.time.format.DateTimeFormatter =
    java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(java.time.ZoneId.systemDefault())

  private def formatDateTime(instant: Instant): String =
    dateTimeFormatter.format(instant)
}
