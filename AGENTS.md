# Copilot instructions for `deder-plugins`

## Build and test commands

Run commands from the repository root. The root `deder.pkl` defines the real build; it generates the `deder-protobuf` and `deder-protobuf-test` modules even though the code lives under `deder-protobuf/`.

- Compile the main plugin module:
  ```bash
  deder exec -t compile -m deder-protobuf
  ```
- Compile the test module:
  ```bash
  deder exec -t compile -m deder-protobuf-test
  ```
- List discovered test suites:
  ```bash
  deder exec -t testClasses -m deder-protobuf-test --json
  ```
- Run the documented full test task, and use `testClasses` above as the check for what Deder actually discovered in the current workspace:
  ```bash
  deder exec -t test -m deder-protobuf-test
  ```
- Run a single MUnit suite with the generated Deder classpath:
  ```bash
  deder exec -t test -m deder-protobuf-test ba.sake.deder.protobuf.protoc.BinaryMavenDependencySuite
  ```
- After editing `deder-protobuf/resources/ProtobufPlugin.pkl`, regenerate the committed bindings:
  ```bash
  ./scripts/gen-plugin-bindings.sh
  ```

Prerequisites called out by the repo docs: JDK 21+, `deder`, `pkl-codegen-java`, and `protoc` when using the `system-path` resolver.

## High-level architecture

- `deder.pkl` at the repo root is the build entrypoint. It uses `CreateScalaModules` to create a publishable `deder-protobuf` module and its paired `deder-protobuf-test` module, both rooted in `deder-protobuf/`.
- `deder-protobuf/src/ba/sake/deder/protobuf/ProtobufPluginImpl.scala` is the plugin entrypoint. It evaluates the typed Pkl config from `ProtobufPlugin.pkl` and wires exactly three Deder tasks: `protobufSourceFiles`, `protobufGenerate`, and `protobufGenerateResources`.
- `config/ProtobufConfigNormalizer.scala` is the normalization boundary between the generated Pkl model and the runtime tasks. It merges repo-wide defaults with per-module overrides, infers proto source directories from the module layout, and rejects deferred parity options such as URL resolvers and digest verification instead of silently ignoring them.
- `sources/ProtoInputResolver.scala` is responsible for the full proto input graph. It collects local `.proto` files, extra import paths, configured dependency artifacts, and project dependency artifacts; `ArchiveProtoExtractor.scala` unpacks `.jar` and `.zip` imports into task-local workspaces so `protoc` can consume them as import roots.
- `tasks/ProtobufGenerationTasks.scala` orchestrates generation. It normalizes config, resolves imports, resolves `protoc` plus plugin executables, builds the command line, runs `protoc`, and returns task-local output directories. Optional descriptor output is the only generated artifact that is copied through the resource-generator task.
- The executable-resolution layer lives under `protoc/`. `ExecutableResolverSupport.scala` handles `system-path`, `path`, `binary-maven`, and `jvm-maven` resolvers; `ProtocCommandBuilder.scala` turns the resolved model into CLI flags; `ProtocRunner.scala` runs the command and surfaces stdout/stderr through Deder notifications.
- `deder-protobuf/examples/consumer/` is the integration example for the local publish flow after `deder exec -t publishLocal -m deder-protobuf`.

## Key conventions

- Treat `deder-protobuf/resources/ProtobufPlugin.pkl` as the source of truth for plugin configuration. The generated Java bindings under `deder-protobuf/src/ba/sake/deder` (for example `Protobuf.java`) and extra generated resources are committed and must be regenerated when the Pkl schema changes.
- Keep build assumptions anchored to the root `deder.pkl`, not to the `deder-protobuf/` directory layout. The module source roots are nested, but task execution is driven from the repository root.
- New tests should follow the existing subsystem split under `deder-protobuf/test/src/ba/sake/deder/protobuf/`: `config`, `sources`, `protoc`, and `tasks`. The suites are MUnit `FunSuite`s named after the runtime component they cover.
- Configuration fields that are intentionally out of MVP scope are still present in the Pkl schema for forward compatibility, but the Scala layer is expected to fail fast when they are set. Preserve that explicit rejection behavior instead of adding silent fallbacks.
- Proto files are treated as build inputs, not generic resources. Keep source discovery, import resolution, and generated output handling inside the Deder task graph rather than writing generated files back into tracked source directories.

---

# Plan: Web Dashboard v2 (+ Version Bump to 0.15.0)

> **DO NOT COMMIT THIS SECTION.** Implementation plan only. Remove before PR.

## Overview

1. Bump Deder config + plugin API from `v0.13.x` to `v0.15.0`
2. Replace Simple.css with Pico.css + density overrides
3. Add Alpine.js for client-side filtering
4. Add Cytoscape.js for interactive module dependency graph
5. Add Server Properties tab (JDK, OS, heap, uptime, Deder version)
6. Consolidate live stats into Server tab
7. HTMX-powered tab routing (back/forward/bookmarks)

## Tab Structure (HTMX-Routed)

| Route | Page Object | Description |
|-------|-----------|-------------|
| `GET /` | — | redirect → `/modules` |
| `GET /modules` | `ModulesPage.modulesTable(project)` | Filterable module table |
| `GET /modules/graph` | `ModulesGraphPage.dependencyGraph(project)` | Cytoscape dep graph |
| `GET /server` | `ServerPage.serverInfo(internals, project, refreshMs)` | Static props + live stat fragments |
| `GET /stats/overview` | `LiveStatsPage.overviewCards(internals)` | HTMX fragment (unchanged) |
| `GET /stats/current` | `LiveStatsPage.currentRequestsTable(internals)` | HTMX fragment (unchanged) |
| `GET /stats/history` | `LiveStatsPage.historyTable(internals)` | HTMX fragment (unchanged) |

Old `/stats` page route removed.

## Files to Change

### A. Version bump (mechanical)
| File | Change |
|------|--------|
| `deder.pkl` | amends `v0.15.0`, `pluginApiVersion` = `0.15.0` |
| `deder-web-dashboard/resources/WebDashboardPlugin.pkl:3` | import `v0.15.0` |
| `deder-protobuf/resources/ProtobufPlugin.pkl` | import `v0.15.0` |
| `deder-build-info/resources/BuildInfoPlugin.pkl` | import `v0.15.0` |

### B. Static resources
| File | Action | Purpose |
|------|--------|---------|
| `resources/public/simple.min.css` | DELETE | Replaced by Pico.css |
| `resources/public/pico.min.css` | ADD | New CSS framework (~7KB) |
| `resources/public/alpine.min.js` | ADD | Client-side filtering (~15KB) |
| `resources/public/cytoscape.min.js` | ADD | Graph rendering (~200KB) |
| `resources/public/dashboard.js` | ADD | Cytoscape init + graph filters (~100 lines) |

`htmx.min.js` stays.

### C. Scala: Modified
| File | Change |
|------|--------|
| `Layout.scala` | Navbar: 3 hx-boosted links, Pico.css `<link>`, density override `<style>`, new script refs |
| `ModulesPage.scala` | Rewrite: Alpine `x-model` filter input, richer table (id, type badge, language, #deps) |
| `LiveStatsPage.scala` | Remove `fullStatsPage()`. Keep `overviewCards`, `currentRequestsTable`, `historyTable` |
| `DashboardServer.scala` | New routes: `/modules/graph`, `/server`. Remove `/stats` page route. Fragment routes stay. |

### D. Scala: New
| File | Change |
|------|--------|
| `ModulesGraphPage.scala` | Render Cytoscape `<div>` + inline `<script>` with `DederProject` modules + `moduleDeps` as JSON graph |
| `ServerPage.scala` | Render static server props cards (JDK, OS, heap, uptime, DederVersion) + embed htmx-polled live fragments |

### E. Tests
| File | Change |
|------|--------|
| `DashboardServerSuite.scala` | Test `GET /` → redirect 301, `GET /modules`, `GET /modules/graph`, `GET /server`. Update fragment test assertions. Remove `/stats` page test. |

## Design Details per Page

### `Layout.scala`
- `<link>` to `pico.min.css` instead of `simple.min.css`
- Custom `<style>` reducing `--pico-spacing` to `0.75rem`, compact tables, smaller nav padding
- Navbar: `hx-boost="true"` links: `Modules`, `Dependency Graph`, `Server`
- Active link highlighted via CSS `:has()` or inline class
- Scripts: `htmx.min.js`, `alpine.min.js`, `cytoscape.min.js`, `dashboard.js`

### `ModulesPage.scala`
- Table columns: `Module ID`, `Type` (Pico badge), `Language` (Scala version or "Java"), `#Deps` (count of `moduleDeps`)
- Search input with Alpine: `x-data="{ filter: '' }"`, `x-model="filter"`, rows have `x-show="id.includes(filter)"`
- Server-side iterates `project.modules`, serializes fields into HTML with Alpine data attributes

### `ModulesGraphPage.scala`
- Full-height `<div id="cy">` for Cytoscape canvas
- Filter bar (Alpine): checkboxes per ModuleType + text search
- Inline `<script>`: `const graphData = { elements: [...] }` built server-side from `project.modules` and `moduleDeps`
- Node colors: SCALA=red, JAVA=blue, test variants=lighter
- Layout: `cose-bilkent` (force-directed)
- `dashboard.js` reads `graphData` from global, initializes Cytoscape, wires Alpine filter events to `cy.style()` / `cy.filter()`

### `ServerPage.scala`
- Static props section:
  | Property | Source |
  |----------|--------|
  | JDK Version | `System.getProperty("java.version")` |
  | JDK Vendor | `System.getProperty("java.vendor")` |
  | OS / Arch | `System.getProperty("os.name")` + `os.arch` |
  | Available Processors | `Runtime.getRuntime().availableProcessors()` |
  | Max Heap (MB) | `Runtime.getRuntime().maxMemory()` |
  | Used Heap (MB) | `total - free` |
  | Deder Version | `DederGlobals.version` |
  | Uptime | `internals.serverUptime` |

- Dynamic section: Embeds three HTMX `hx-get` divs for `/stats/overview`, `/stats/current`, `/stats/history`
- No `workerThreadPoolSize` (deprecated)

## Implementation Order

1. Version bump (deder.pkl + all .pkl files)
2. Replace CSS + add JS libraries to `resources/public/`
3. Rewrite `Layout.scala` (navbar, links, scripts)
4. Rewrite `ModulesPage.scala` (Alpine filter + richer table)
5. Create `ModulesGraphPage.scala` + `dashboard.js`
6. Create `ServerPage.scala` + wire to `DashboardServer.scala`
7. Update `DashboardServer.scala` routes
8. Update all Pkl bindings (`./scripts/gen-plugin-bindings.sh`)
9. Update `DashboardServerSuite.scala` tests
10. Compile + test
11. (Optional) Check visual companion for CSS rendering

## Open Questions

- ~~Cytoscape vs Mermaid~~ → Cytoscape.js confirmed
- ~~CSS framework~~ → Pico.css confirmed
- ~~Server props depth~~ → Lightweight (System.getProperty + Runtime + DederGlobals)
- ~~Tab routing~~ → HTMX hx-boost confirmed
- ~~Alpine.JS~~ → Confirmed for filtering

---

# Implementation Tasks

> Task-by-task instructions. Work in `.worktrees/web-dashboard-v2/`. Run commands from repo root.

---

### Task 1: Version bump

**Files:** `deder.pkl`, `deder-web-dashboard/resources/WebDashboardPlugin.pkl`, `deder-protobuf/resources/ProtobufPlugin.pkl`, `deder-build-info/resources/BuildInfoPlugin.pkl`

- [ ] **Step 1: Update `deder.pkl` lines 1 and 4**

```pkl
amends "https://sake92.github.io/deder/config/v0.15.0/DederProject.pkl"
...
local const pluginApiVersion = "0.15.0"
```

- [ ] **Step 2: Update `deder-web-dashboard/resources/WebDashboardPlugin.pkl` line 3**

```pkl
import "https://sake92.github.io/deder/config/v0.15.0/DederProject.pkl" as P
```

- [ ] **Step 3: Update `deder-protobuf/resources/ProtobufPlugin.pkl` — find and replace `v0.13` with `v0.15`**

```bash
rg "v0\.13" deder-protobuf/resources/ProtobufPlugin.pkl
```

Replace any `v0.13.x` references with `v0.15.0`.

- [ ] **Step 4: Update `deder-build-info/resources/BuildInfoPlugin.pkl`**

```bash
rg "v0\.13" deder-build-info/resources/BuildInfoPlugin.pkl
```

Replace any `v0.13.x` references with `v0.15.0`.

---

### Task 2: Download static resources

**Files:** `deder-web-dashboard/resources/public/`

- [ ] **Step 1: Remove Simple.css**

```bash
rm deder-web-dashboard/resources/public/simple.min.css
```

- [ ] **Step 2: Download Pico.css v2.1.1**

```bash
curl -L -o deder-web-dashboard/resources/public/pico.min.css \
  https://cdn.jsdelivr.net/npm/@picocss/pico@2.1.1/css/pico.min.css
```

- [ ] **Step 3: Download Alpine.js v3.14.9**

```bash
curl -L -o deder-web-dashboard/resources/public/alpine.min.js \
  https://cdn.jsdelivr.net/npm/alpinejs@3.14.9/dist/cdn.min.js
```

- [ ] **Step 4: Download Cytoscape.js v3.31.1**

```bash
curl -L -o deder-web-dashboard/resources/public/cytoscape.min.js \
  https://cdn.jsdelivr.net/npm/cytoscape@3.31.1/dist/cytoscape.min.js
```

- [ ] **Step 5: Verify all files exist**

```bash
ls -la deder-web-dashboard/resources/public/
```

Expected: `alpine.min.js`, `cytoscape.min.js`, `htmx.min.js`, `pico.min.css`

---

### Task 3: Create `dashboard.js`

**Files:** Create `deder-web-dashboard/resources/public/dashboard.js`

- [ ] **Step 1: Write `dashboard.js`**

```javascript
// Deder Dashboard — Cytoscape module dependency graph
document.addEventListener('alpine:init', function() {
  const cyEl = document.getElementById('cy');
  if (!cyEl || !window.__graphData) return;

  const cy = cytoscape({
    container: cyEl,
    elements: window.__graphData.elements,
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
          'text-outline-width': 0,
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
          'curve-style': 'bezier'
        }
      }
    ],
    layout: { name: 'cose-bilkent', animate: false, nodeRepulsion: 5000 }
  });

  window.__cy = cy;

  cy.on('tap', 'node', function(evt) {
    const node = evt.target;
    cy.elements().removeClass('highlighted');
    node.addClass('highlighted');
    node.neighborhood().addClass('highlighted');
  });

  cy.on('tap', function(evt) {
    if (evt.target === cy) {
      cy.elements().removeClass('highlighted');
    }
  });
});

// Called from Alpine.js @change on checkboxes
window.toggleGraphType = function(cy, type, show) {
  cy.nodes('[type="' + type + '"]').style('display', show ? 'element' : 'none');
};

// Called from Alpine.js @input on search field
window.graphSearch = function(cy, term) {
  cy.elements().removeClass('dimmed');
  if (!term || term.trim() === '') {
    cy.nodes().style('display', 'element');
    cy.fit();
    cy.center();
    return;
  }
  var lower = term.toLowerCase();
  cy.nodes().forEach(function(node) {
    if (node.data('id').toLowerCase().includes(lower)) {
      node.style('display', 'element');
      node.removeClass('dimmed');
    } else {
      node.style('display', 'none');
    }
  });
};

window.graphReset = function(cy) {
  cy.nodes().style('display', 'element');
  cy.fit();
  cy.center();
};
```

---

### Task 4: Rewrite `Layout.scala`

**Files:** Modify `deder-web-dashboard/src/ba/sake/deder/webdashboard/pages/Layout.scala`

- [ ] **Step 1: Write new `Layout.scala`**

```scala
package ba.sake.deder.webdashboard.pages

import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}

object Layout {
  def htmlPage(title: String, activeTab: String, content: Html): Html =
    html"""
      <!DOCTYPE html>
      <html lang="en" data-theme="light">
      <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>$title</title>
        <link rel="stylesheet" href="/pico.min.css">
        <script src="/htmx.min.js"></script>
        <script src="/alpine.min.js" defer></script>
        <script src="/cytoscape.min.js"></script>
        <script src="/dashboard.js" defer></script>
        <style>
          :root {
            --pico-font-size: 88%;
            --pico-spacing: 0.6rem;
            --pico-line-height: 1.4;
          }
          body { max-width: 1100px; margin: 0 auto; padding: 0.6rem 0.75rem; }
          .navbar { display: flex; gap: 0.25rem; margin-bottom: 0.75rem; border-bottom: 1px solid var(--pico-muted-border-color); padding-bottom: 0.4rem; }
          .navbar a { text-decoration: none; padding: 0.25rem 0.5rem; border-radius: 4px; font-size: 0.85rem; font-weight: 600; }
          .navbar a:hover { background: var(--pico-muted-border-color); }
          .navbar a.active { background: var(--pico-primary); color: var(--pico-primary-inverse); }
          .stat-card { display: inline-block; border: 1px solid var(--pico-muted-border-color); border-radius: 4px; padding: 0.4rem 0.6rem; margin: 0.2rem; min-width: 140px; text-align: center; }
          .stat-card .label { font-size: 0.7rem; color: var(--pico-muted-color); text-transform: uppercase; letter-spacing: 0.5px; }
          .stat-card .value { font-size: 1.1rem; font-weight: 700; }
          .success { color: var(--pico-color-green-400); }
          .failure { color: var(--pico-color-red-400); }
          #cy { width: 100%; height: 520px; border: 1px solid var(--pico-muted-border-color); border-radius: 4px; }
          .graph-controls { display: flex; gap: 0.4rem; flex-wrap: wrap; align-items: center; margin-bottom: 0.4rem; font-size: 0.8rem; }
          .graph-controls label { display: flex; align-items: center; gap: 0.15rem; cursor: pointer; }
          .graph-controls input[type="checkbox"] { margin: 0; }
          .highlighted { border-width: 3px !important; border-color: var(--pico-color-yellow-300) !important; }
          .dimmed { opacity: 0.15; }
        </style>
      </head>
      <body>
        <nav class="navbar" hx-boost="true">
          <a href="/modules" class="${
            if activeTab == "modules" then "active" else ""
          }">Modules</a>
          <a href="/modules/graph" class="${
            if activeTab == "graph" then "active" else ""
          }">Dependency Graph</a>
          <a href="/server" class="${
            if activeTab == "server" then "active" else ""
          }">Server</a>
        </nav>
        <main>$content</main>
      </body>
      </html>
    """
}
```

---

### Task 5: Rewrite `ModulesPage.scala`

**Files:** Modify `deder-web-dashboard/src/ba/sake/deder/webdashboard/pages/ModulesPage.scala`

- [ ] **Step 1: Write new `ModulesPage.scala`**

```scala
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
      <tr x-show="textContent.toLowerCase().includes(filter.toLowerCase())">
        <td>${m.id}</td>
        <td><mark class="$typeClass">$typeLabel</mark></td>
        <td>$lang</td>
        <td>$depCount</td>
      </tr>
    """
  }
}
```

---

### Task 6: Create `ModulesGraphPage.scala`

**Files:** Create `deder-web-dashboard/src/ba/sake/deder/webdashboard/pages/ModulesGraphPage.scala`

- [ ] **Step 1: Write `ModulesGraphPage.scala`**

```scala
package ba.sake.deder.webdashboard.pages

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
            <input type="search" @input="search(${symbol_dollar}el.value)" placeholder="Search module..." style="max-width:200px; margin-left:auto;" aria-label="Search graph">
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
        <script>window.__graphData = $json;</script>
      """
  }

  private def buildGraphJson(modules: Seq[DederModule]): String = {
    val nodes = modules.map { m =>
      val (typ, color) = m match
        case _: ScalaModule => ("SCALA", "#e74c3c")
        case _: ScalaTestModule => ("SCALA_TEST", "#f8a5c2")
        case _: JavaModule => ("JAVA", "#3498db")
        case _: JavaTestModule => ("JAVA_TEST", "#85d0e7")
        case _ => ("UNKNOWN", "#95a5a6")
      s"""{ "data": { "id": "${esc(m.id)}", "type": "$typ", "label": "${esc(m.id)}", "color": "$color" } }"""
    }

    val edges = modules.flatMap { m =>
      val deps: Seq[String] = try {
        m.moduleDeps.asInstanceOf[java.util.List[String]].asScala.toSeq
      } catch { case _: Throwable => Seq.empty }
      deps.map(dep =>
        s"""{ "data": { "id": "${esc(dep)}->${esc(m.id)}", "source": "${esc(dep)}", "target": "${esc(m.id)}" } }"""
      )
    }

    val elements = if edges.nonEmpty then nodes.mkString(",") + "," + edges.mkString(",") else nodes.mkString(",")
    s"""{ "elements": [$elements] }"""
  }

  private def esc(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
}
```

---

### Task 7: Create `ServerPage.scala`

**Files:** Create `deder-web-dashboard/src/ba/sake/deder/webdashboard/pages/ServerPage.scala`

- [ ] **Step 1: Write `ServerPage.scala`**

```scala
package ba.sake.deder.webdashboard.pages

import java.time.Duration
import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}
import ba.sake.deder.*

object ServerPage {
  def serverInfo(internals: DederProjectInternals, project: DederProject, refreshMs: Int): Html = {
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

    html"""
      <h2>Server Properties</h2>
      <div style="display: flex; flex-wrap: wrap;">
        ${statCard("JDK", s"$jdkVersion")}
        ${statCard("Vendor", jdkVendor)}
        ${statCard("OS / Arch", s"$osName $osArch")}
        ${statCard("Processors", processors.toString)}
        ${statCard("Heap", s"${usedHeapMB}MB / ${maxHeapMB}MB")}
        ${statCard("Deder Version", dederVersion)}
        ${statCard("Uptime", uptimeStr)}
        ${statCard("Modules", moduleCount.toString)}
      </div>

      <hr style="margin:1rem 0;">
      <h3>Live Stats</h3>
      <div hx-get="/stats/overview" hx-trigger="load, every ${refreshMs}ms" hx-swap="innerHTML">
        <p>Loading overview...</p>
      </div>
      <h4 style="margin-top:0.75rem;">Current Requests</h4>
      <div hx-get="/stats/current" hx-trigger="load, every ${refreshMs}ms" hx-swap="innerHTML">
        <p>Loading current requests...</p>
      </div>
      <h4 style="margin-top:0.75rem;">Recent History</h4>
      <div hx-get="/stats/history" hx-trigger="load, every ${refreshMs}ms" hx-swap="innerHTML">
        <p>Loading recent history...</p>
      </div>
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
```

---

### Task 8: Update `DashboardServer.scala` routes

**Files:** Modify `deder-web-dashboard/src/ba/sake/deder/webdashboard/server/DashboardServer.scala`

- [ ] **Step 1: Update imports and routes**

Replace the import block and routes. Keep everything else (`private var jdkServer`, `refreshMs`, `start()`, `stop()`) unchanged.

New imports:
```scala
import ba.sake.deder.webdashboard.pages.{Layout, ModulesPage, ModulesGraphPage, ServerPage, LiveStatsPage}
```

New routes:
```scala
  private val routes = Routes {
    case GET -> Path() =>
      Response.redirect("/modules")

    case GET -> Path("modules") =>
      val content = ModulesPage.modulesTable(project)
      Response.withBody(Layout.htmlPage("Modules - Deder Dashboard", "modules", content))

    case GET -> Path("modules", "graph") =>
      val content = ModulesGraphPage.dependencyGraph(project)
      Response.withBody(Layout.htmlPage("Dependency Graph - Deder Dashboard", "graph", content))

    case GET -> Path("server") =>
      val content = ServerPage.serverInfo(internals, project, refreshMs)
      Response.withBody(Layout.htmlPage("Server - Deder Dashboard", "server", content))

    case GET -> Path("stats", "overview") =>
      val html = LiveStatsPage.overviewCards(internals)
      Response.withBody(html)

    case GET -> Path("stats", "current") =>
      val html = LiveStatsPage.currentRequestsTable(internals)
      Response.withBody(html)

    case GET -> Path("stats", "history") =>
      val html = LiveStatsPage.historyTable(internals)
      Response.withBody(html)
  }
```

---

### Task 9: Update `LiveStatsPage.scala`

**Files:** Modify `deder-web-dashboard/src/ba/sake/deder/webdashboard/pages/LiveStatsPage.scala`

- [ ] **Step 1: Remove `fullStatsPage()` method** (lines 11-25). Keep `overviewCards`, `currentRequestsTable`, `historyTable`, and private helpers.

- [ ] **Step 2: Remove unused imports** (`java.time.Instant` for `fullStatsPage` should stay if used in fragments; check — `Instant` is used in `currentRequestsTable` and `historyTable`. `DateTimeFormatter` used in `formatDateTime`. Only `ZoneId` may become unused — remove it if so.)

- [ ] **Step 3: Update `overviewCards()` uptime formatting**

Replace the goofy `// TODO if days > 0 etc..` uptime display with clean formatting. The return is still an `<div style="display:flex">` with 3 stat cards — same structure, just fix the uptime calc to use modulo arithmetic:

```scala
  def overviewCards(internals: DederProjectInternals): Html = {
    val uptime = internals.serverUptime
    val days = uptime.toDays
    val hours = uptime.toHours % 24
    val minutes = uptime.toMinutes % 60
    val seconds = uptime.toSeconds % 60
    val parts = Seq(
      if days > 0 then Some(s"${days}d") else None,
      if hours > 0 || days > 0 then Some(s"${hours}h") else None,
      if minutes > 0 || days > 0 || hours > 0 then Some(s"${minutes}m") else None,
      Some(s"${seconds}s")
    ).flatten
    val uptimeStr = parts.mkString(" ")
    html"""
      <div style="display: flex; flex-wrap: wrap;">
        <div class="stat-card">
          <div class="label">Total Requests</div>
          <div class="value">${internals.totalRequestsServed}</div>
        </div>
        <div class="stat-card">
          <div class="label">Total Errors</div>
          <div class="value" style="color: ${
        if internals.totalErrors > 0 then "var(--pico-color-red-400)" else "var(--pico-color-green-400)"
      }">${internals.totalErrors}</div>
        </div>
        <div class="stat-card">
          <div class="label">Uptime</div>
          <div class="value" style="font-size:1rem">$uptimeStr</div>
        </div>
      </div>
    """
  }
```

---

### Task 10: Update `DashboardServerSuite.scala` tests

**Files:** Modify `deder-web-dashboard/test/src/ba/sake/deder/webdashboard/server/DashboardServerSuite.scala`

- [ ] **Step 1: Replace the entire test file**

```scala
package ba.sake.deder.webdashboard.server

import java.time.{Duration, Instant}
import java.net.{URL, HttpURLConnection}
import java.util.concurrent.TimeUnit
import ba.sake.deder.*
import ba.sake.deder.config.DederProject
import WebDashboard.WebDashboardPluginConfig
import munit.FunSuite

class DashboardServerSuite extends FunSuite {

  private val testHost = "localhost"
  private val testPort = 19999
  private val testRefreshMs = 5000

  private def stubInternals: DederProjectInternals = new DederProjectInternals {
    def currentRequests: Seq[LiveRequest] =
      Seq(LiveRequest("req-001", CallerType.Cli, "compile", Seq("my-module"), Instant.now()))
    def recentHistory: Seq[CompletedRequest] =
      Seq(
        CompletedRequest("req-000", CallerType.Cli, "compile", Seq("my-module"),
          Instant.now().minusSeconds(10), Duration.ofSeconds(5), true)
      )
    def taskStats(taskName: String): Option[TaskStats] = None
    def allTaskStats: Seq[(String, TaskStats)] = Seq.empty
    def totalRequestsServed: Long = 100L
    def totalErrors: Long = 5L
    def serverUptime: Duration = Duration.ofSeconds(8130L)
    def workerThreadPoolSize: Int = 10
  }

  private def stubProject: DederProject =
    new DederProject(
      java.util.List.of(),
      java.util.List.of(),
      java.util.List.of(),
      true
    )

  private val config = WebDashboardPluginConfig(true, testHost, testPort.toLong, testRefreshMs.toLong)
  private val server = DashboardServer(config, stubProject, stubInternals)
  private val baseUrl = s"http://$testHost:$testPort"

  override def beforeAll(): Unit = {
    server.start()
    Thread.sleep(500)
  }

  override def afterAll(): Unit = {
    server.stop()
  }

  private def httpGet(path: String): (Int, String) = {
    val url = s"$baseUrl$path"
    val conn = URL(url).openConnection().asInstanceOf[HttpURLConnection]
    conn.setInstanceFollowRedirects(false)
    conn.setConnectTimeout(2000)
    conn.setReadTimeout(2000)
    try {
      val code = conn.getResponseCode
      val stream = if code >= 400 then conn.getErrorStream else conn.getInputStream
      val body = if stream != null then scala.io.Source.fromInputStream(stream).mkString else ""
      conn.disconnect()
      (code, body)
    } catch {
      case e: Exception =>
        conn.disconnect()
        throw e
    }
  }

  test("GET / redirects to /modules") {
    val (code, _) = httpGet("/")
    assertEquals(code, 301)
  }

  test("GET /modules returns HTML page with filter") {
    val (code, body) = httpGet("/modules")
    assertEquals(code, 200)
    assert(body.contains("Modules"), s"body should contain 'Modules', got: ${body.take(300)}")
    assert(body.contains("x-model"), s"body should contain Alpine filter, got: ${body.take(300)}")
  }

  test("GET /modules/graph returns HTML page with Cytoscape container") {
    val (code, body) = httpGet("/modules/graph")
    assertEquals(code, 200)
    assert(body.contains("Dependency Graph"), s"body should contain 'Dependency Graph', got: ${body.take(300)}")
    assert(body.contains("id=\"cy\""), s"body should contain Cytoscape div, got: ${body.take(300)}")
  }

  test("GET /server returns HTML page with server properties") {
    val (code, body) = httpGet("/server")
    assertEquals(code, 200)
    assert(body.contains("Server Properties"), s"body should contain 'Server Properties', got: ${body.take(300)}")
    assert(body.contains("JDK"), s"body should contain 'JDK', got: ${body.take(300)}")
  }

  test("GET /stats/overview returns stat cards HTML fragment") {
    val (code, body) = httpGet("/stats/overview")
    assertEquals(code, 200)
    assert(body.contains("100"), s"should contain total requests (100), got: ${body}")
    assert(body.contains("5"), s"should contain total errors (5), got: ${body}")
  }

  test("GET /stats/current returns current requests HTML fragment") {
    val (code, body) = httpGet("/stats/current")
    assertEquals(code, 200)
    assert(body.contains("compile"), s"should contain task name 'compile', got: ${body}")
  }

  test("GET /stats/history returns recent history HTML fragment") {
    val (code, body) = httpGet("/stats/history")
    assertEquals(code, 200)
    assert(body.contains("compile"), s"should contain task name 'compile', got: ${body}")
    assert(body.contains("OK"), s"should contain success marker 'OK', got: ${body}")
  }
}
```

---

### Task 11: Compile

- [ ] **Step 1: Compile web dashboard module**

```bash
cd /home/sake/projects/sake92/deder-plugins/.worktrees/web-dashboard-v2
deder exec -t compile -m deder-web-dashboard
```

Expected: `Compilation succeeded.`

Fix any compilation errors by adjusting imports or type references.

---

### Task 12: Run tests

- [ ] **Step 1: Compile test module**

```bash
deder exec -t compile -m deder-web-dashboard-test
```

- [ ] **Step 2: Run tests**

```bash
deder exec -t test -m deder-web-dashboard-test
```

Expected: All 7 tests pass.

---

### Cleanup (before PR)

Remove this entire plan section from `AGENTS.md` before creating a PR.
