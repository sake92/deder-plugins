package ba.sake.deder.webdashboard.pages

import java.time.{Duration, Instant}
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.*
import ba.sake.deder.webdashboard.ApiHistoryEntry

object HistoryPage {
  def fullPage(refreshMs: Int): Html =
    html"""
      <div class="controls-bar">
        <label>
          ${autoRefreshCheckbox(true)}
          <span>Auto-refresh</span>
        </label>
      </div>

      <form id="history-filters" class="grid">
        <div>
          <label for="hist-search">Search</label>
          <input type="text" id="hist-search" name="search" placeholder="request/task/module..."
                 hx-get="/stats/history-table" hx-trigger="keyup changed delay:300ms"
                 hx-include="#history-filters" hx-target="#history-table-wrapper" hx-swap="innerHTML"
                 class="search-box">
        </div>
        <div>
          <label for="hist-caller">Caller</label>
          <input type="text" id="hist-caller" name="caller" placeholder="CLI, BSP..."
                 hx-get="/stats/history-table" hx-trigger="keyup changed delay:300ms"
                 hx-include="#history-filters" hx-target="#history-table-wrapper" hx-swap="innerHTML"
                 class="caller-box">
        </div>
        <div>
          <label for="hist-status">Status</label>
          <select id="hist-status" name="status"
                  hx-get="/stats/history-table" hx-trigger="change"
                  hx-include="#history-filters" hx-target="#history-table-wrapper" hx-swap="innerHTML">
            <option value="all">All</option>
            <option value="success">Success</option>
            <option value="failure">Failure</option>
          </select>
        </div>
        <div>
          <label for="hist-sort">Sort</label>
          <select id="hist-sort" name="sort"
                  hx-get="/stats/history-table" hx-trigger="change"
                  hx-include="#history-filters" hx-target="#history-table-wrapper" hx-swap="innerHTML">
            <option value="newest">Newest</option>
            <option value="oldest">Oldest</option>
            <option value="longest">Longest</option>
            <option value="shortest">Shortest</option>
          </select>
        </div>
        <input type="hidden" name="limit" value="50">
        <input type="hidden" name="offset" value="0">
      </form>

      <div id="history-table-wrapper"
           hx-get="/stats/history-table" hx-include="#history-filters" hx-swap="innerHTML"
           hx-trigger="load, every ${refreshMs}ms, refresh">
        <p>Loading history...</p>
      </div>

      <div id="history-load-more" class="load-more"></div>
    """

  def autoRefreshCheckbox(enabled: Boolean): Html =
    if enabled then
      html"""<input type="checkbox" id="auto-refresh-cb" checked
                     hx-get="/stats/auto-refresh/history?enabled=false" hx-trigger="change" hx-swap="outerHTML">"""
    else
      html"""<input type="checkbox" id="auto-refresh-cb"
                     hx-get="/stats/auto-refresh/history?enabled=true" hx-trigger="change" hx-swap="outerHTML">"""

  def autoRefreshOob(enabled: Boolean, refreshMs: Int): Html = {
    val trigger = if enabled then s"load, every ${refreshMs}ms, refresh" else "load, refresh"
    html"""<div id="history-table-wrapper" hx-get="/stats/history-table" hx-include="#history-filters" hx-swap="innerHTML" hx-trigger="${trigger}" hx-swap-oob="true"></div>"""
  }

  /** Render full table (including thead) for a page of history entries. */
  def historyTable(entries: Seq[ApiHistoryEntry]): Html = {
    if entries.isEmpty then html"""<p><em>No matching requests found.</em></p>"""
    else
      val rows = entries.map { entry =>
        val statusClass = if entry.success then "pico-color-green-400" else "pico-color-red-400"
        val statusText = if entry.success then "✅ OK" else "❌ FAIL"
        val startedStr = formatDateTime(Instant.ofEpochMilli(entry.startTimeMs))
        val durStr = formatElapsed(Duration.ofMillis(entry.durationMs))
        html"""
          <tr>
            <td>${startedStr}</td>
            <td>${entry.caller}</td>
            <td>${entry.taskName}</td>
            <td>${entry.moduleIds.mkString(", ")}</td>
            <td>${durStr}</td>
            <td class="${statusClass}">${statusText}</td>
          </tr>
          <tr>
            <td colspan="6" class="no-pad">
              <details>
                <summary>Details</summary>
                <div>
                  Request ID: ${entry.requestId}<br>
                  Caller: ${entry.caller}<br>
                  Modules: ${entry.moduleIds.mkString(", ")}<br>
                  Started: ${startedStr}<br>
                  Duration: ${durStr}
                </div>
              </details>
            </td>
          </tr>
        """
      }
       html"""
         <table class="striped compact">
           <thead><tr><th>Started</th><th>Client</th><th>Task</th><th>Modules</th><th>Duration</th><th>Status</th></tr></thead>
          <tbody>${rows}</tbody>
        </table>
      """
  }

  /** Render only the table rows (no thead, no table wrapper) for "load more". */
  def historyTableRows(entries: Seq[ApiHistoryEntry]): Html = {
    if entries.isEmpty then Html("")
    else
      val rows = entries.map { entry =>
        val statusClass = if entry.success then "pico-color-green-400" else "pico-color-red-400"
        val statusText = if entry.success then "✅ OK" else "❌ FAIL"
        val startedStr = formatDateTime(Instant.ofEpochMilli(entry.startTimeMs))
        val durStr = formatElapsed(Duration.ofMillis(entry.durationMs))
        html"""
          <tr>
            <td>${startedStr}</td>
            <td>${entry.caller}</td>
            <td>${entry.taskName}</td>
            <td>${entry.moduleIds.mkString(", ")}</td>
            <td>${durStr}</td>
            <td class="${statusClass}">${statusText}</td>
          </tr>
          <tr>
            <td colspan="6" class="no-pad">
              <details>
                <summary>Details</summary>
                <div>
                  Request ID: ${entry.requestId}<br>
                  Caller: ${entry.caller}<br>
                  Modules: ${entry.moduleIds.mkString(", ")}<br>
                  Started: ${startedStr}<br>
                  Duration: ${durStr}
                </div>
              </details>
            </td>
          </tr>
        """
      }
      html"${rows}"
  }

  /** Load-more button fragment, swaps OOB into #history-load-more. */
  def loadMoreButton(hasMore: Boolean, limit: Int, nextOffset: Int): Html =
    if hasMore then
      html"""
        <button id="history-load-more"
                hx-get="/stats/more-history" hx-trigger="click"
                hx-include="#history-filters" hx-swap-oob="true"
                hx-target="#history-table-wrapper table tbody" hx-swap="beforeend"
                hx-vals='{"offset": "${nextOffset}"}'
                class="outline secondary">
          Load more...
        </button>
      """
    else
      html"""<span id="history-load-more" hx-swap-oob="true"></span>"""

  def noMoreContent(): Html =
    html"""<em id="history-load-more" hx-swap-oob="true">No more results.</em>"""

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
