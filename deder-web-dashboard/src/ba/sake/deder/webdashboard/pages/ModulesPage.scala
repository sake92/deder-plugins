package ba.sake.deder.webdashboard.pages

import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.config.DederProject
import ba.sake.deder.config.DederProject.{DederModule, JavaModule, JavaTestModule, ScalaModule, ScalaTestModule, ScalaJsModule, ScalaJsTestModule, ScalaNativeModule, ScalaNativeTestModule}
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
        <div x-data="{ filter: '' }">
          <input type="search" x-model="filter" autofocus placeholder="Filter modules..."
                  class="module-filter"
                 aria-label="Filter modules">
           <small>$total modules</small>
          <table class="striped compact">
            <thead><tr><th>Module ID</th><th>Type</th><th>Version</th><th>#Deps</th></tr></thead>
            <tbody>$rows</tbody>
          </table>
        </div>
      """
  }

  private def versionStr(scalaVer: Option[String], platformVer: Option[String]): String = {
    val sv = scalaVer.filter(_.nonEmpty).getOrElse("-")
    platformVer.filter(_.nonEmpty).map(pv => s"$sv / $pv").getOrElse(sv)
  }

  private def buildRow(m: DederModule): Html = {
    val (typeLabel, typeClass) = m match
      case _: ScalaJsTestModule => ("SCALAJS_TEST", "pico-color-jade-300")
      case _: ScalaNativeTestModule => ("SCALANATIVE_TEST", "pico-color-lime-300")
      case _: ScalaTestModule => ("SCALA_TEST", "pico-color-pink-300")
      case _: JavaTestModule => ("JAVA_TEST", "pico-color-cyan-300")
      case _: ScalaJsModule => ("SCALAJS", "pico-color-orange-400")
      case _: ScalaNativeModule => ("SCALANATIVE", "pico-color-green-400")
      case _: ScalaModule => ("SCALA", "pico-color-red-400")
      case _: JavaModule => ("JAVA", "pico-color-blue-400")
      case _ => (if m.`type` != null then m.`type`.name().toLowerCase else "unknown", "")
    val lang = m match
      case sm: ScalaJsTestModule => versionStr(Option(sm.scalaVersion), Option(sm.scalaJsVersion))
      case sm: ScalaJsModule => versionStr(Option(sm.scalaVersion), Option(sm.scalaJsVersion))
      case sm: ScalaNativeTestModule => versionStr(Option(sm.scalaVersion), Option(sm.scalaNativeVersion))
      case sm: ScalaNativeModule => versionStr(Option(sm.scalaVersion), Option(sm.scalaNativeVersion))
      case sm: ScalaTestModule => Option(sm.scalaVersion).filter(_.nonEmpty).getOrElse("Scala 3")
      case sm: ScalaModule => Option(sm.scalaVersion).filter(_.nonEmpty).getOrElse("Scala 3")
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
