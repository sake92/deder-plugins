package ba.sake.deder.webdashboard.pages

import java.time.Duration
import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.*
import ba.sake.deder.config.DederProject

object ServerPage {
  def serverInfo(internals: DederProjectInternals, project: DederProject): Html = {
    val jdkVersion = System.getProperty("java.version")
    val jdkVendor = System.getProperty("java.vendor")
    val osName = System.getProperty("os.name")
    val osArch = System.getProperty("os.arch")
    val processors = Runtime.getRuntime().availableProcessors()
    val maxHeapMB = Runtime.getRuntime().maxMemory() / (1024 * 1024)
    val usedHeapMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
    val dederVersion = DederGlobals.version
    val uptimeStr = formatUptime(internals.serverUptime)
    val moduleCount = if project != null && project.modules != null then project.modules.size() else 0
    val plugins = internals.loadedPlugins

    html"""
      <h3>Deder</h3>
      <div class="stat-row">
        ${statCard("Version", dederVersion)}
        ${statCard("Uptime", uptimeStr)}
        ${statCard("Modules", moduleCount.toString)}
        ${statCard("Plugins", plugins.size.toString)}
        ${statCard("Heap", s"${usedHeapMB}MB / ${maxHeapMB}MB")}
      </div>
      ${
        if plugins.nonEmpty then pluginsSection(plugins)
        else html""
      }

      <h3 class="section-head">OS</h3>
      <div class="stat-row">
        ${statCard("OS / Arch", s"$osName $osArch")}
        ${statCard("Processors", processors.toString)}
      </div>

      <h3 class="section-head">JDK</h3>
      <div class="stat-row">
        ${statCard("Version", jdkVersion)}
        ${statCard("Vendor", jdkVendor)}
      </div>
    """
  }

  private def pluginsSection(plugins: Seq[LoadedPluginInfo]): Html = {
    val rows = plugins.sortBy(_.id).map { p =>
      html"""<tr><td>${p.id}</td><td>${p.taskNames.size}</td><td>${p.taskNames.mkString(", ")}</td></tr>"""
    }
    html"""
      <h4 class="section-head">Loaded Plugins</h4>
      <table class="compact">
        <thead><tr><th>Plugin ID</th><th>#Tasks</th><th>Task Names</th></tr></thead>
        <tbody>$rows</tbody>
      </table>
    """
  }

  private def statCard(label: String, value: String): Html =
    html"""
      <div class="stat-card">
        <div class="label">$label</div>
        <div class="value">$value</div>
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
