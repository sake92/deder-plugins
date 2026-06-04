package ba.sake.deder.webdashboard.pages


import play.twirl.api.HtmlFormat
import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.config.DederProject
import ba.sake.deder.config.DederProject.{DederModule, JavaModule, JavaTestModule, ScalaModule, ScalaTestModule}
import scala.jdk.CollectionConverters.*

object ModulesGraphPage {
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
          showScalaTest: true,
          showJavaTest: true,
          toggleScala(show)   { window.toggleGraphType(window.__cy, 'SCALA', show) },
          toggleJava(show)    { window.toggleGraphType(window.__cy, 'JAVA', show) },
          toggleScalaTest(show) { window.toggleGraphType(window.__cy, 'SCALA_TEST', show) },
          toggleJavaTest(show)  { window.toggleGraphType(window.__cy, 'JAVA_TEST', show) },
          search(term) { window.graphSearch(window.__cy, term) },
          reset() { window.graphReset(window.__cy); showScala=true; showJava=true; showScalaTest=true; showJavaTest=true; }
        }">
          <div class="graph-controls">
            <label><input type="checkbox" x-model="showScala" @change="toggleScala(showScala)"> Scala</label>
            <label><input type="checkbox" x-model="showJava" @change="toggleJava(showJava)"> Java</label>
            <label><input type="checkbox" x-model="showScalaTest" @change="toggleScalaTest(showScalaTest)"> Scala Test</label>
            <label><input type="checkbox" x-model="showJavaTest" @change="toggleJavaTest(showJavaTest)"> Java Test</label>
            <input type="search" @input="search($$el.value)" placeholder="Search module..." style="max-width:200px; margin-left:auto;" aria-label="Search graph">
            <button type="button" class="outline secondary" @click="reset()" style="font-size:0.8rem; padding:0.2rem 0.5rem;">Reset</button>
          </div>
          <div id="cy"></div>
          <div style="font-size: 0.72rem; margin-top: 0.25rem; color: var(--pico-muted-color);">
            <span style="color:#e74c3c;">● Scala</span>
            <span style="color:#3498db; margin-left:0.5rem;">● Java</span>
            <span style="color:#f8a5c2; margin-left:0.5rem;">● Scala Test</span>
            <span style="color:#85d0e7; margin-left:0.5rem;">● Java Test</span>
            &mdash; Arrow = depends on. Tap a node to highlight its neighborhood.
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
                'background-color': 'data(color)',
                'label': 'data(label)',
                'font-size': '10px',
                'text-valign': 'center',
                'text-halign': 'center',
                'color': '#fff',
                'text-outline-width': 1,
                'text-outline-color': '#000',
                'width': 28,
                'height': 28,
                'border-width': 1,
                'border-color': '#fff'
              }
            },
            { selector: 'node[type="SCALA"]', style: { 'shape': 'ellipse' } },
            { selector: 'node[type="JAVA"]', style: { 'shape': 'ellipse' } },
            { selector: 'node[type="SCALA_TEST"]', style: { 'shape': 'round-rectangle' } },
            { selector: 'node[type="JAVA_TEST"]', style: { 'shape': 'round-rectangle' } },
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
      val (typ, color) = m match
        case _: ScalaTestModule => ("SCALA_TEST", "#f8a5c2")
        case _: JavaTestModule => ("JAVA_TEST", "#85d0e7")
        case _: ScalaModule => ("SCALA", "#e74c3c")
        case _: JavaModule => ("JAVA", "#3498db")
        case _ => ("UNKNOWN", "#95a5a6")
      s"""{ "data": { "id": "${esc(m.id)}", "type": "$typ", "label": "${esc(m.id)}", "color": "$color" } }"""
    }

    val edges = modules.flatMap { m =>
      val deps = m.moduleDeps.asScala.toSeq.map(_.id)
      deps.map(dep =>
        s"""{ "data": { "id": "${esc(dep)}->${esc(m.id)}", "source": "${esc(dep)}", "target": "${esc(m.id)}" } }"""
      )
    }

    val elements = if edges.nonEmpty then nodes.mkString(",") + "," + edges.mkString(",") else nodes.mkString(",")
    s"""[$elements] """
  }

  private def esc(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
}
