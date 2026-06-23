package ba.sake.deder.webdashboard.pages


import play.twirl.api.HtmlFormat
import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.config.DederProject
import ba.sake.deder.config.DederProject.{DederModule}
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
        <div x-data="{
          showScala: true,
          showJava: true,
          showScalaJs: true,
          showScalaNative: true,
          showTest: true,
          applyFilters() {
            const cy = window.__cy;
            if (!cy) return;
            cy.nodes().forEach(node => {
              let type = node.data('type');
              let show = false;
              if (type === 'SCALA' || type === 'SCALA_TEST') {
                show = this.showScala;
              } else if (type === 'JAVA' || type === 'JAVA_TEST') {
                show = this.showJava;
              } else if (type === 'SCALA_JS' || type === 'SCALA_JS_TEST') {
                show = this.showScalaJs;
              } else if (type === 'SCALA_NATIVE' || type === 'SCALA_NATIVE_TEST') {
                show = this.showScalaNative;
              } else {
                show = true;
              }
              if (show && !this.showTest && type.includes('_TEST')) {
                show = false;
              }
              node.style('display', show ? 'element' : 'none');
            });
          },
          search(term) {
            const cy = window.__cy;
            cy.elements().removeClass('dimmed');
            if (!term || term.trim() === '') {
              cy.nodes().style('display', 'element');
              this.applyFilters();
              cy.fit();
              cy.center();
              return;
            }
            const lower = term.toLowerCase();
            cy.nodes().forEach(node => {
              if (node.data('id').toLowerCase().includes(lower)) {
                node.style('display', 'element');
                node.removeClass('dimmed');
              } else {
                node.style('display', 'none');
              }
            });
          },
          reset() {
            this.showScala = true;
            this.showJava = true;
            this.showScalaJs = true;
            this.showScalaNative = true;
            this.showTest = true;
            window.__cy.nodes().style('display', 'element');
            this.applyFilters();
            window.__cy.fit();
            window.__cy.center();
          }
        }">
          <div class="graph-controls">
            <label><input type="checkbox" x-model="showScala" @change="applyFilters()"> <img src="${scalaIcon}" class="legend-icon"> Scala</label>
            <label><input type="checkbox" x-model="showScalaJs" @change="applyFilters()"> <img src="${scalaJsIcon}" class="legend-icon"> Scala.js</label>
            <label><input type="checkbox" x-model="showScalaNative" @change="applyFilters()"> <img src="${scalaNativeIcon}" class="legend-icon"> Scala Native</label>
            <label><input type="checkbox" x-model="showJava" @change="applyFilters()"> <img src="${javaIcon}" class="legend-icon"> Java</label>
            <label><input type="checkbox" x-model="showTest" @change="applyFilters()"> <span class="legend-icon-test"></span> Test</label>
            <button type="button" class="outline secondary" @click="reset()">🔄 Reset</button>
            <button type="button" class="outline secondary" @click="window.__cy.fit(); window.__cy.center()">🔍 Fit</button>
            <input type="search" @input="search($$el.value)" placeholder="Search module..." class="graph-search" aria-label="Search graph">
          </div>
          <div id="cy"></div>

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
      val typ = if m.`type` == null then "UNKNOWN" else m.`type`.name()
      val iconPath = typ match
        case s if s.startsWith("SCALA_JS")     => scalaJsIcon
        case s if s.startsWith("SCALA_NATIVE") => scalaNativeIcon
        case s if s.startsWith("SCALA")        => scalaIcon
        case _                                 => javaIcon
      s"""{ "data": { "id": "${esc(m.id)}", "type": "$typ", "label": "${esc(m.id)}", "icon": "$iconPath" } }"""
    }

    val edges = modules.flatMap { m =>
      val deps = m.moduleDeps.asScala.toSeq.map(_.id)
      deps.map(dep =>
        s"""{ "data": { "id": "${esc(m.id)}->${esc(dep)}", "source": "${esc(m.id)}", "target": "${esc(dep)}" } }"""
      )
    }

    val elements = if edges.nonEmpty then nodes.mkString(",") + "," + edges.mkString(",") else nodes.mkString(",")
    s"""[ $elements ]"""
  }

  private def esc(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
}
