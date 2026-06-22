package ba.sake.deder.webdashboard.pages

import java.time.Duration
import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.*
import ba.sake.deder.config.DederProject

object ServerPage {

  def fullPage(internals: DederProjectInternals, project: DederProject, refreshMs: Int): Html =
    html"""
      <div class="controls-bar">
        <label>
          ${autoRefreshCheckbox(true)}
          <span>Auto-refresh</span>
        </label>
      </div>
      <div id="deder-card"
           hx-get="/server/deder-card"
           hx-trigger="load, every ${refreshMs}ms, refresh"
           hx-swap="innerHTML">
        ${dederCard(internals, project)}
      </div>
      ${systemInfo}
      ${jdkInfo}
    """

  def dederCard(internals: DederProjectInternals, project: DederProject): Html = {
    val dederVersion = DederGlobals.version
    val uptimeStr = formatUptime(internals.serverUptime)
    val moduleCount = if project != null && project.modules != null then project.modules.size() else 0
    val plugins = internals.loadedPlugins
    val maxHeapMB = Runtime.getRuntime().maxMemory() / (1024 * 1024)
    val usedHeapMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)

    html"""
      <article>
        <header><h3>Deder</h3></header>
        ${statCard("Version", dederVersion)}
        ${statCard("Uptime", uptimeStr)}
        ${statCard("Modules", moduleCount.toString)}
        ${statCard("Plugins", plugins.size.toString)}
        ${statCard("Heap", s"${usedHeapMB}MB / ${maxHeapMB}MB")}
        ${if plugins.nonEmpty then pluginsSection(plugins) else html""}
      </article>
    """
  }

  def serverInfo(internals: DederProjectInternals, project: DederProject): Html =
    html"""
      ${dederCard(internals, project)}
      ${systemInfo}
      ${jdkInfo}
    """

  private def systemInfo: Html = {
    val osName = System.getProperty("os.name")
    val osArch = System.getProperty("os.arch")
    val processors = Runtime.getRuntime().availableProcessors()
    html"""
      <article>
        <header><h3>System</h3></header>
        ${statCard("OS / Arch", s"$osName $osArch")}
        ${statCard("Processors", processors.toString)}
      </article>
    """
  }

  private def jdkInfo: Html = {
    val jdkVersion = System.getProperty("java.version")
    val jdkVendor = System.getProperty("java.vendor")
    html"""
      <article>
        <header><h3>JDK</h3></header>
        ${statCard("Version", jdkVersion)}
        ${statCard("Vendor", jdkVendor)}
      </article>
    """
  }

  def autoRefreshCheckbox(enabled: Boolean): Html =
    if enabled then
      html"""<input type="checkbox" id="server-auto-refresh-cb" checked
                     hx-get="/server/auto-refresh?enabled=false" hx-trigger="change" hx-swap="outerHTML">"""
    else
      html"""<input type="checkbox" id="server-auto-refresh-cb"
                     hx-get="/server/auto-refresh?enabled=true" hx-trigger="change" hx-swap="outerHTML">"""

  def autoRefreshOob(enabled: Boolean, refreshMs: Int): Html =
    val trigger = if enabled then s"load, every ${refreshMs}ms, refresh" else "load, refresh"
    html"""<div id="deder-card" hx-get="/server/deder-card" hx-trigger="${trigger}" hx-swap="innerHTML" hx-swap-oob="true"></div>"""

  private def pluginsSection(plugins: Seq[LoadedPluginInfo]): Html = {
    val rows = plugins.sortBy(_.id).map { p =>
      html"""<tr><td>${p.id}</td><td>${p.taskNames.size}</td><td>${p.taskNames.mkString(", ")}</td></tr>"""
    }
    html"""
      <hr>
      <h4>Loaded Plugins</h4>
      <table class="compact">
        <thead><tr><th>Plugin ID</th><th>#Tasks</th><th>Task Names</th></tr></thead>
        <tbody>$rows</tbody>
      </table>
    """
  }

  private def statCard(label: String, value: String): Html =
    html"""
      <div>
        <b>$label</b>:
        <span>$value</span>
      </div>
    """

  private def formatUptime(uptime: Duration): String = {
    val days = uptime.toDays
    val hours = uptime.toHours % 24
    val minutes = uptime.toMinutes % 60
    val seconds = uptime.toSeconds % 60
    val parts = Seq(
      if days > 0 then Some(s"${days}d") else None,
      if hours > 0 then Some(s"${hours}h") else None,
      if minutes > 0 then Some(s"${minutes}m") else None,
      Some(s"${seconds}s")
    ).flatten
    parts.mkString(" ")
  }
}
