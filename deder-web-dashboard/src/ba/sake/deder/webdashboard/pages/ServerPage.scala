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
    val projectRootDir = DederGlobals.projectRootDir.toString
    val uptimeStr = formatUptime(internals.serverUptime)
    val moduleCount = if project != null && project.modules != null then project.modules.size() else 0
    val caches = internals.inMemoryCachesStats
    val plugins = internals.loadedPlugins

    html"""
      <h2>Server Properties</h2>
      <div style="display: flex; flex-wrap: wrap;">
        ${statCard("Deder Version", dederVersion)}
        ${statCard("Project Root", projectRootDir)}
        ${statCard("Uptime", uptimeStr)}
        ${statCard("Modules", moduleCount.toString)}
        ${statCard("Plugins Loaded", plugins.size.toString)}
        ${statCard("In-Memory Caches", caches.size.toString)}
        ${statCard("Heap", s"${usedHeapMB}MB / ${maxHeapMB}MB")}
        ${statCard("JDK", s"$jdkVersion")}
        ${statCard("JDK Vendor", jdkVendor)}
        ${statCard("OS / Arch", s"$osName $osArch")}
        ${statCard("Processors", processors.toString)}
      </div>

      ${
        if plugins.nonEmpty then pluginsSection(plugins)
        else html""
      }

      ${
        if caches.nonEmpty then cacheSection(caches)
        else html"""<small style="color: var(--pico-muted-color);">No in-memory caches active.</small>"""
      }
    """
  }

  private def cacheSection(caches: Map[String, InMemCacheStats]): Html = {
    val rows = caches.toSeq.sortBy(_._1).map { case (name, stats) =>
      html"""<tr><td>$name</td><td>${stats.estimatedSize}</td><td>${stats.hitCount}</td><td>${stats.missCount}</td></tr>"""
    }
    html"""
      <h4 style="margin-top:0.75rem;">In-Memory Caches</h4>
      <table style="font-size:0.85rem;">
        <thead><tr><th>Name</th><th>Size</th><th>Hits</th><th>Misses</th></tr></thead>
        <tbody>$rows</tbody>
      </table>
    """
  }

  private def pluginsSection(plugins: Seq[LoadedPluginInfo]): Html = {
    val rows = plugins.sortBy(_.id).map { p =>
      html"""<tr><td>${p.id}</td><td>${p.taskNames.size}</td><td>${p.taskNames.mkString(", ")}</td></tr>"""
    }
    html"""
      <h4 style="margin-top:0.75rem;">Loaded Plugins</h4>
      <table style="font-size:0.85rem;">
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
