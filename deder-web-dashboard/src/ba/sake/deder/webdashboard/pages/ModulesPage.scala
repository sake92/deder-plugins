package ba.sake.deder.webdashboard.pages

import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.config.DederProject

object ModulesPage {
  def modulesTable(project: DederProject): Html = {
    val modules = if project == null then java.util.List.of() else project.modules
    if modules == null || modules.isEmpty then
      html"""<p>No modules found in this project.</p>"""
    else
      import scala.jdk.CollectionConverters.*
      val rows = modules.asScala.map { m =>
        val modType = if m.`type` != null then m.`type`.name().toLowerCase else "unknown"
        html"""<tr><td>${m.id}</td><td><mark>${modType}</mark></td></tr>"""
      }
      html"""
        <h2>Project Modules</h2>
        <table>
          <thead><tr><th>Module ID</th><th>Type</th></tr></thead>
          <tbody>${rows}</tbody>
        </table>
      """
  }
}
