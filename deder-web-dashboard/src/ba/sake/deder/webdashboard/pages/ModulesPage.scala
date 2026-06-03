package ba.sake.deder.webdashboard.pages

import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.config.DederProject
import ba.sake.deder.config.DederProject.{DederModule, JavaModule, JavaTestModule, ScalaModule, ScalaTestModule}
import scala.jdk.CollectionConverters.*

object ModulesPage {
  def modulesTable(project: DederProject): Html = {
    val modules = if project == null then java.util.List.of() else project.modules
    if modules == null || modules.isEmpty then
      html"""<p>No modules found in this project.</p>"""
    else
      val modList = modules.asScala.toSeq
      val rows = modList.map(buildRow)
      val total = modList.size
      html"""
        <h2>Project Modules</h2>
        <div x-data="{ filter: '' }">
          <input type="search" x-model="filter" placeholder="Filter modules..."
                 style="max-width: 300px; margin-bottom: 0.5rem;"
                 aria-label="Filter modules">
          <span style="font-size: 0.8rem; color: var(--pico-muted-color);">$total modules</span>
          <table class="module-table" style="font-size:0.85rem; margin-top:0.25rem;">
            <thead><tr><th>Module ID</th><th>Type</th><th>Language</th><th>#Deps</th></tr></thead>
            <tbody>$rows</tbody>
          </table>
        </div>
      """
  }

  private def buildRow(m: DederModule): Html = {
    val (typeLabel, typeClass) = m match
      case _: ScalaModule => ("SCALA", "pico-color-red-400")
      case _: ScalaTestModule => ("SCALA_TEST", "pico-color-pink-300")
      case _: JavaModule => ("JAVA", "pico-color-blue-400")
      case _: JavaTestModule => ("JAVA_TEST", "pico-color-cyan-300")
      case _ => (if m.`type` != null then m.`type`.name().toLowerCase else "unknown", "")
    val lang = m match
      case sm: ScalaModule => Option(sm.scalaVersion).filter(_.nonEmpty).getOrElse("Scala 3")
      case sm: ScalaTestModule => Option(sm.scalaVersion).filter(_.nonEmpty).getOrElse("Scala 3")
      case _: JavaModule | _: JavaTestModule => "Java"
      case _ => "?"
    val depCount = try m.moduleDeps.size() catch case _: Throwable => 0
    html"""
      <tr x-show="filter === '' || $$el.textContent.toLowerCase().includes(filter.toLowerCase())">
        <td>${m.id}</td>
        <td><mark class="$typeClass">$typeLabel</mark></td>
        <td>$lang</td>
        <td>$depCount</td>
      </tr>
    """
  }
}
