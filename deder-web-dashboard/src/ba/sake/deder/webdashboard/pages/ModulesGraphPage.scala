package ba.sake.deder.webdashboard.pages


import play.twirl.api.HtmlFormat
import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.config.DederProject
import ba.sake.deder.config.DederProject.{DederModule, JavaModule, JavaTestModule, ScalaModule, ScalaTestModule, ScalaJsModule, ScalaJsTestModule, ScalaNativeModule, ScalaNativeTestModule}
import scala.jdk.CollectionConverters.*

object ModulesGraphPage {
  val scalaIcon = "/icons/scala.png"
  val scalaJsIcon = "/icons/scalajs.png"
  val scalaNativeIcon = "/icons/scalanative.png"
  val javaIcon = "/icons/java.svg"
  def dependencyGraph(project: DederProject): Html = {
    val modules = if project == null then java.util.List.of() else project.modules
    if modules == null || modules.isEmpty then
      html"""<p>No modules found in this project.</p>"""
    else
      val modList = modules.asScala.toSeq
      val json = buildGraphJson(modList)
      html"""
        <h2>Module Dependency Graph</h2>
        <div x-data="{
          showScala: true,
          showJava: true,
          showScalaJs: true,
          showScalaNative: true,
          showTest: true,
          applyFilters() { window.applyFilters(window.__cy) },
          search(term) { window.graphSearch(window.__cy, term) },
          reset() {
            window.graphReset(window.__cy);
            showScala = true;
            showJava = true;
            showScalaJs = true;
            showScalaNative = true;
            showTest = true;
            window.applyFilters(window.__cy);
          }
        }">
          <div class="graph-controls">
            <label><input type="checkbox" x-model="showScala" @change="applyFilters()"> Scala</label>
            <label><input type="checkbox" x-model="showScalaJs" @change="applyFilters()"> Scala.js</label>
            <label><input type="checkbox" x-model="showScalaNative" @change="applyFilters()"> Scala Native</label>
            <label><input type="checkbox" x-model="showJava" @change="applyFilters()"> Java</label>
            <label><input type="checkbox" x-model="showTest" @change="applyFilters()"> Test</label>
            <input type="search" @input="search($$el.value)" placeholder="Search module..." style="max-width:200px; margin-left:auto;" aria-label="Search graph">
            <button type="button" class="outline secondary" @click="reset()" style="font-size:0.8rem; padding:0.2rem 0.5rem;">Reset</button>
          </div>
          <div id="cy"></div>
          <div style="font-size: 0.72rem; margin-top: 0.25rem; color: var(--pico-muted-color); display: flex; align-items: center; gap: 0.6rem; flex-wrap: wrap;">
            <span><img src="${scalaIcon}" style="width:14px;height:14px;vertical-align:middle;"> Scala</span>
            <span><img src="${scalaJsIcon}" style="width:14px;height:14px;vertical-align:middle;"> Scala.js</span>
            <span><img src="${scalaNativeIcon}" style="width:14px;height:14px;vertical-align:middle;"> Scala Native</span>
            <span><img src="${javaIcon}" style="width:14px;height:14px;vertical-align:middle;"> Java</span>
            <span style="display:inline-block;width:14px;height:14px;border:2px dashed #999;border-radius:3px;vertical-align:middle;"></span> Test
            &mdash; Arrow = depends on. Click a node to highlight its neighborhood.
          </div>
        </div>
        <script>
        cytoscape.use(cytoscapeDagre);
        window.__cy = cytoscape({
          container: document.getElementById('cy'),
          elements: ${HtmlFormat.raw(json)},
          style: [
            {
              selector: 'node',
              style: {
                'background-color': '#f8f9fa',
                'background-image': 'data(icon)',
                'background-fit': 'contain',
                'background-width': '60%',
                'background-height': '60%',
                'background-position-x': '50%',
                'background-position-y': '50%',
                'label': 'data(label)',
                'font-size': '10px',
                'text-valign': 'center',
                'text-halign': 'center',
                'color': '#222',
                'text-outline-color': '#fff',
                'text-outline-width': 1,
                'width': 32,
                'height': 32,
                'border-width': 1,
                'border-color': '#ccc'
              }
            },
            // All non-test modules: ellipse shape
            { selector: 'node[type="SCALA"]',         style: { 'shape': 'ellipse' } },
            { selector: 'node[type="JAVA"]',          style: { 'shape': 'ellipse' } },
            { selector: 'node[type="SCALA_JS"]',      style: { 'shape': 'ellipse' } },
            { selector: 'node[type="SCALA_NATIVE"]',  style: { 'shape': 'ellipse' } },
            // All test modules: round-rectangle + dashed border
            { selector: 'node[type${"$"}="_TEST"]', style: {
              'shape': 'round-rectangle',
              'border-style': 'dashed',
              'border-width': 2,
              'border-color': '#999'
            }},
            {
              selector: 'edge',
              style: {
                'width': 1,
                'line-color': '#aaa',
                'target-arrow-color': '#aaa',
                'target-arrow-shape': 'triangle',
                'curve-style': 'bezier',
              }
            }
          ],
          layout: { name: 'dagre', rankDir: 'TB', spacingFactor: 1.2 },
          wheelSensitivity: 0.2
        });

        window.__cy.on('tap', 'node', function(evt) {
          var node = evt.target;
          window.__cy.elements().removeClass('highlighted');
          node.addClass('highlighted');
          node.neighborhood().addClass('highlighted');
        });

        window.__cy.on('tap', function(evt) {
          if (evt.target === window.__cy) {
            window.__cy.elements().removeClass('highlighted');
          }
        });
        </script>
      """
  }

  private def buildGraphJson(modules: Seq[DederModule]): String = {
    val nodes = modules.map { m =>
      val (typ, iconPath) = m match
        case _: ScalaTestModule         => ("SCALA_TEST", scalaIcon)
        case _: ScalaJsTestModule       => ("SCALA_JS_TEST", scalaJsIcon)
        case _: ScalaNativeTestModule   => ("SCALA_NATIVE_TEST", scalaNativeIcon)
        case _: JavaTestModule          => ("JAVA_TEST", javaIcon)
        case _: ScalaJsModule           => ("SCALA_JS", scalaJsIcon)
        case _: ScalaNativeModule       => ("SCALA_NATIVE", scalaNativeIcon)
        case _: ScalaModule             => ("SCALA", scalaIcon)
        case _: JavaModule              => ("JAVA", javaIcon)
        case _                          => ("UNKNOWN", "")
      s"""{ "data": { "id": "${esc(m.id)}", "type": "$typ", "label": "${esc(m.id)}", "icon": "$iconPath" } }"""
    }

    val edges = modules.flatMap { m =>
      val deps = m.moduleDeps.asScala.toSeq.map(_.id)
      deps.map(dep =>
        s"""{ "data": { "id": "${esc(m.id)}->${esc(dep)}", "source": "${esc(m.id)}", "target": "${esc(dep)}" } }"""
      )
    }

    val elements = if edges.nonEmpty then nodes.mkString(",") + "," + edges.mkString(",") else nodes.mkString(",")
    s"""[$elements] """
  }

  private def esc(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
}
