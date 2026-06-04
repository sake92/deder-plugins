# Icon-Based Module Graph Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the color-only module type distinction in the web dashboard's dependency graph with language/platform icons (Scala, Scala.js, Scala Native, Java) plus a dashed-border test badge.

**Architecture:** Download/create 4 SVG icon assets; update the Scala page template (`ModulesGraphPage.scala`) to emit icon URLs instead of colors and handle all 9 module types; update `dashboard.js` toggle logic to use 5 combined checkboxes; no changes to server routing, layout, or other plugins.

**Tech Stack:** Scala 3, play.twirl.api.Html (HTML templates), Cytoscape.js (Canvas graph), Alpine.js (checkbox toggles), inline SVG assets.

---

### Task 1: Create icon SVG assets

**Files:**
- Create: `deder-web-dashboard/resources/public/icons/scala.svg`
- Create: `deder-web-dashboard/resources/public/icons/java.svg`
- Create: `deder-web-dashboard/resources/public/icons/scalajs.svg`
- Create: `deder-web-dashboard/resources/public/icons/scalanative.svg`

- [ ] **Step 1: Create `scala.svg`** — simplified Scala spiral, flat red (`#dc322f`)

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64">
  <path fill="#dc322f" d="M47.2 9.9c0 1.5-7.6 4-15.6 6.5-8 2.5-15.6 5-15.6 6.5v2.6c0 1.5 7.6 4 15.6 6.5 8 2.5 15.6 5 15.6 6.5v-2.9c0-1.5-7.6-4-15.6-6.5-8-2.5-15.6-5-15.6-6.5V22c0-1.5 7.6-4 15.6-6.5C39.6 13 47.2 10.5 47.2 9"/>
  <path fill="#dc322f" d="M47.2 22.8v-2.9c0-1.5-7.6-4-15.6-6.5-8-2.5-15.6-5-15.6-6.5v2.6c0 1.5 7.6 4 15.6 6.5 8 2.5 15.6 5 15.6 6.5"/>
  <path fill="#b81b1e" d="M31.6 16.4c-8-2.5-15.6-5-15.6-6.5v2.6c0 1.5 7.6 4 15.6 6.5 8 2.5 15.6 5 15.6 6.5v-2.9c0-1.5-7.6-4-15.6-6.5"/>
</svg>
```

- [ ] **Step 2: Create `java.svg`** — simplified Java coffee cup, blue (`#007396`) + orange (`#ED8B00`)

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64">
  <path fill="#007396" d="M24 45.2s-2.2 1.3 1.5 1.7c4.6.6 7 .5 12.1-.5.8.5 1.7 1 2.6 1.3-11.4 4.9-25.8-.3-16.9-2.8l.7.3zm-1.5-6.4s-2.4 1.9 1.4 2.3c5 .5 8.9.6 15.7-.8.7.6 1.5 1 2.3 1.5-13.8 4.1-29.2.4-19.3-2.9l-.1-.1zm27 11.2s1.7 1.4-1.9 2.5c-6.5 2-27.5 2.5-33.4 0-2.1-.9 1.9-2.2 3.1-2.4 1.3-.3 2-.3 2-.3-2.3-1.6-15 3.2-6.5 4.6 23.4 3.8 42.7-1.7 36.6-4.4h.1zM22 32.1s-10.7 2.5-3.8 3.4c2.9.4 8.7.3 14.1-.1 4.4-.3 8.8-1.2 8.8-1.2s-1.6.7-2.6 1.4c-10.9 2.8-31.7 1.6-25.7-1.4 5.1-2.4 9.3-2.1 9.3-2.1l-.1.1zm19.1 10.7c11-5.7 5.9-11.2 2.3-10.5-.9.2-1.3.4-1.3.4s.3-.6.9-1c7-2.5 12.4 7.4-2.3 11.1 0 0 .1-.1.4 0z"/>
  <path fill="#ED8B00" d="M34.6 7.5s6.1 6.2-5.8 15.5c-9.5 7.5-2.2 11.8 0 16.7-5.6-5-9.6-9.4-6.9-13.5 3.8-5.8 14.5-8 12.7-18.7z"/>
  <path fill="#ED8B00" d="M31.5 36.1c2.8 3.2-.8 6.2-.8 6.2s7.2-3.7 3.9-8.3c-3-4.4-5.4-6.5 7.3-13.8 0 0-20 5-10.4 15.9z"/>
</svg>
```

- [ ] **Step 3: Create `scalajs.svg`** — simplified Scala.js mark, yellow (`#FFDA3E`) + red accents on dark grey base

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64">
  <polygon fill="#D6BA32" points="16.4,58 12.4,6 51.6,6 47.6,58 32,62"/>
  <polygon fill="#FFDA3E" points="32,56.5 46.2,50.4 49.5,10 32,10"/>
  <path fill="#b81b1e" d="M32 21.2v11.6c4.8.3 9.3 1.6 9.2 4.1l.4-9.4c0-2.2-.1-5.1-9.6-6.3"/>
  <path fill="#8a0000" d="M32 34.4v11.5c4.3-.2 8.3-1 8.3-1.4l.3-9.3c0-2-8-1.9-8.6-.8"/>
  <path fill="#e04040" d="M44.2 15.2c-.1 2.6-5.4 4.9-11 6.6l.1 12.3c6.5-3.7 6.7-5.6 6.7-5.6l.4-12.2"/>
  <path fill="#e04040" d="M43.4 28.3c-.1 2.3-5.1 4.9-10.3 7l.1 10.8c9.7-4.6 9.8-7.2 9.8-7.2l.4-10.6"/>
  <path fill="#a00000" d="M32.1 20.8c-5.5.8-10.6 1-10.6 1l.5 11.2c4 .1 7.3 0 10.1-.3V20.8z"/>
  <path fill="#a00000" d="M32.1 34.4c-5.2.3-10 .1-10 .1l.4 9.9c3.8.5 7 .7 9.6.7V34.4z"/>
</svg>
```

- [ ] **Step 4: Create `scalanative.svg`** — simplified Scala Native feather, teal (`#00A3A3`)

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64">
  <path fill="#00A3A3" d="M50 8c-8 14-18 24-32 34l-4 8c6-4 18-12 28-16 6-2 12-4 16-8 2-2 4-6 2-8-2-4-6-8-10-10z"/>
  <path fill="#007171" d="M50 8c-4 8-10 14-18 18-6 4-14 8-22 12l-2 4c8-6 18-12 26-16 6-2 12-6 16-12v-2"/>
  <path fill="#00C4C4" d="M48 10c-6 10-14 18-24 24l-2 6c4-4 14-10 22-14 4-2 8-4 10-8 0-2-2-6-6-8z"/>
</svg>
```

- [ ] **Step 5: Verify icons directory exists**

Run:
```bash
mkdir -p deder-web-dashboard/resources/public/icons
ls deder-web-dashboard/resources/public/icons/
```

Expected: `scala.svg  java.svg  scalajs.svg  scalanative.svg`

- [ ] **Step 6: Commit**

```bash
git add deder-web-dashboard/resources/public/icons/
git commit -m "feat: add language/platform SVG icons for module graph"
```

---

### Task 2: Update `ModulesGraphPage.scala` — imports and `buildGraphJson()`

**Files:**
- Modify: `deder-web-dashboard/src/ba/sake/deder/webdashboard/pages/ModulesGraphPage.scala`

- [ ] **Step 1: Add imports for the 4 new module types**

Current import (line 7):
```scala
import ba.sake.deder.config.DederProject.{DederModule, JavaModule, JavaTestModule, ScalaModule, ScalaTestModule}
```

Replace with:
```scala
import ba.sake.deder.config.DederProject.{DederModule, JavaModule, JavaTestModule, ScalaModule, ScalaTestModule, ScalaJsModule, ScalaJsTestModule, ScalaNativeModule, ScalaNativeTestModule}
```

- [ ] **Step 2: Update `buildGraphJson()` to handle all 9 types with icon URLs**

Current code (lines 107-116):
```scala
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
```

Replace with:
```scala
  private def buildGraphJson(modules: Seq[DederModule]): String = {
    val nodes = modules.map { m =>
      val (typ, iconPath) = m match
        case _: ScalaTestModule         => ("SCALA_TEST", "/icons/scala.svg")
        case _: ScalaJsTestModule       => ("SCALA_JS_TEST", "/icons/scalajs.svg")
        case _: ScalaNativeTestModule   => ("SCALA_NATIVE_TEST", "/icons/scalanative.svg")
        case _: JavaTestModule          => ("JAVA_TEST", "/icons/java.svg")
        case _: ScalaModule             => ("SCALA", "/icons/scala.svg")
        case _: ScalaJsModule           => ("SCALA_JS", "/icons/scalajs.svg")
        case _: ScalaNativeModule       => ("SCALA_NATIVE", "/icons/scalanative.svg")
        case _: JavaModule              => ("JAVA", "/icons/java.svg")
        case _                          => ("UNKNOWN", "")
      s"""{ "data": { "id": "${esc(m.id)}", "type": "$typ", "label": "${esc(m.id)}", "icon": "$iconPath" } }"""
    }
```

- [ ] **Step 3: Commit**

```bash
git add deder-web-dashboard/src/ba/sake/deder/webdashboard/pages/ModulesGraphPage.scala
git commit -m "feat: handle all 9 module types with icon URLs in graph data"
```

---

### Task 3: Update `ModulesGraphPage.scala` — Cytoscape styles

**Files:**
- Modify: `deder-web-dashboard/src/ba/sake/deder/webdashboard/pages/ModulesGraphPage.scala`

- [ ] **Step 1: Update node style section** (lines 54-75)

Current styles (lines 54-75):
```scala
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
```

Replace with:
```scala
          style: [
            {
              selector: 'node',
              style: {
                'background-color': '#f8f9fa',
                'background-image': 'data(icon)',
                'background-fit': 'contain',
                'background-width': '60%',
                'background-height': '60%',
                'label': 'data(label)',
                'font-size': '10px',
                'text-valign': 'center',
                'text-halign': 'center',
                'color': '#222',
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
            { selector: 'node[type$="_TEST"]', style: {
              'shape': 'round-rectangle',
              'border-style': 'dashed',
              'border-width': 2,
              'border-color': '#999'
            }},
```

- [ ] **Step 2: Remove the now-unnecessary `data(color)` references** — no other code references `data(color)` after the style change, so no further changes needed in the Cytoscape config.

- [ ] **Step 3: Commit**

```bash
git add deder-web-dashboard/src/ba/sake/deder/webdashboard/pages/ModulesGraphPage.scala
git commit -m "feat: update Cytoscape styles with icons, neutral bg, dashed test border"
```

---

### Task 4: Update `ModulesGraphPage.scala` — Legend and checkbox controls

**Files:**
- Modify: `deder-web-dashboard/src/ba/sake/deder/webdashboard/pages/ModulesGraphPage.scala`

- [ ] **Step 1: Update Alpine x-data toggles** (lines 20-31)

Current code:
```html
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
```

Replace with:
```html
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
```

- [ ] **Step 2: Update checkbox controls** (lines 32-39)

Current code:
```html
          <div class="graph-controls">
            <label><input type="checkbox" x-model="showScala" @change="toggleScala(showScala)"> Scala</label>
            <label><input type="checkbox" x-model="showJava" @change="toggleJava(showJava)"> Java</label>
            <label><input type="checkbox" x-model="showScalaTest" @change="toggleScalaTest(showScalaTest)"> Scala Test</label>
            <label><input type="checkbox" x-model="showJavaTest" @change="toggleJavaTest(showJavaTest)"> Java Test</label>
            <input type="search" @input="search($$el.value)" placeholder="Search module..." style="max-width:200px; margin-left:auto;" aria-label="Search graph">
            <button type="button" class="outline secondary" @click="reset()" style="font-size:0.8rem; padding:0.2rem 0.5rem;">Reset</button>
          </div>
```

Replace with:
```html
          <div class="graph-controls">
            <label><input type="checkbox" x-model="showScala" @change="applyFilters()"> Scala</label>
            <label><input type="checkbox" x-model="showJava" @change="applyFilters()"> Java</label>
            <label><input type="checkbox" x-model="showScalaJs" @change="applyFilters()"> Scala.js</label>
            <label><input type="checkbox" x-model="showScalaNative" @change="applyFilters()"> Scala Native</label>
            <label><input type="checkbox" x-model="showTest" @change="applyFilters()"> Test</label>
            <input type="search" @input="search($$el.value)" placeholder="Search module..." style="max-width:200px; margin-left:auto;" aria-label="Search graph">
            <button type="button" class="outline secondary" @click="reset()" style="font-size:0.8rem; padding:0.2rem 0.5rem;">Reset</button>
          </div>
```

- [ ] **Step 3: Update legend** (lines 41-47)

Current code:
```html
          <div style="font-size: 0.72rem; margin-top: 0.25rem; color: var(--pico-muted-color);">
            <span style="color:#e74c3c;">● Scala</span>
            <span style="color:#3498db; margin-left:0.5rem;">● Java</span>
            <span style="color:#f8a5c2; margin-left:0.5rem;">● Scala Test</span>
            <span style="color:#85d0e7; margin-left:0.5rem;">● Java Test</span>
            &mdash; Arrow = depends on. Tap a node to highlight its neighborhood.
          </div>
```

Replace with:
```html
          <div style="font-size: 0.72rem; margin-top: 0.25rem; color: var(--pico-muted-color); display: flex; align-items: center; gap: 0.6rem; flex-wrap: wrap;">
            <span><img src="/icons/scala.svg" style="width:14px;height:14px;vertical-align:middle;"> Scala</span>
            <span><img src="/icons/java.svg" style="width:14px;height:14px;vertical-align:middle;"> Java</span>
            <span><img src="/icons/scalajs.svg" style="width:14px;height:14px;vertical-align:middle;"> Scala.js</span>
            <span><img src="/icons/scalanative.svg" style="width:14px;height:14px;vertical-align:middle;"> Scala Native</span>
            <span style="display:inline-block;width:14px;height:14px;border:2px dashed #999;border-radius:3px;vertical-align:middle;"></span> Test
            &mdash; Arrow = depends on. Tap a node to highlight its neighborhood.
          </div>
```

- [ ] **Step 4: Commit**

```bash
git add deder-web-dashboard/src/ba/sake/deder/webdashboard/pages/ModulesGraphPage.scala
git commit -m "feat: update controls, checkboxes, and legend for icon-based graph"
```

---

### Task 5: Update `dashboard.js` — new filter logic

**Files:**
- Modify: `deder-web-dashboard/resources/public/dashboard.js`

- [ ] **Step 1: Replace `toggleGraphType` with `applyFilters`**

Current content of `dashboard.js`:
```javascript
// Deder Dashboard — Cytoscape graph helpers

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

Replace with:
```javascript
// Deder Dashboard — Cytoscape graph helpers

// Called from Alpine.js @change on checkboxes — reads all 5 filter states from Alpine scope
window.applyFilters = function(cy) {
  if (!cy) return;
  var rootEl = document.querySelector('[x-data]');
  if (!rootEl) return;
  var state = Alpine.$data(rootEl);

  cy.nodes().forEach(function(node) {
    var type = node.data('type');
    var show = false;

    // Platform check: which toggle controls this node type?
    if (type === 'SCALA' || type === 'SCALA_TEST') {
      show = state.showScala;
    } else if (type === 'JAVA' || type === 'JAVA_TEST') {
      show = state.showJava;
    } else if (type === 'SCALA_JS' || type === 'SCALA_JS_TEST') {
      show = state.showScalaJs;
    } else if (type === 'SCALA_NATIVE' || type === 'SCALA_NATIVE_TEST') {
      show = state.showScalaNative;
    } else {
      show = true; // UNKNOWN type always visible
    }

    // Test filter: if it's a test module and Test toggle is OFF, hide it
    if (show && !state.showTest && type.indexOf('_TEST') !== -1) {
      show = false;
    }

    node.style('display', show ? 'element' : 'none');
  });
};

// Called from Alpine.js @input on search field
window.graphSearch = function(cy, term) {
  cy.elements().removeClass('dimmed');
  if (!term || term.trim() === '') {
    cy.nodes().style('display', 'element');
    window.applyFilters(cy); // re-apply filters after showing all
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
  window.applyFilters(cy);
  cy.fit();
  cy.center();
};
```

- [ ] **Step 2: Commit**

```bash
git add deder-web-dashboard/resources/public/dashboard.js
git commit -m "feat: replace toggleGraphType with applyFilters for 5-checkbox filtering"
```

---

### Task 6: Verify tests pass

- [ ] **Step 1: Run existing test suite**

Run:
```bash
cd deder-web-dashboard && ../mill test
```

Or if using `mill` from repo root:
```bash
mill deder-web-dashboard.test
```

Expected: all tests pass (the existing tests check that HTTP responses contain certain strings — the page structure hasn't changed, just the visual presentation).

- [ ] **Step 2: Check for any compilation errors**

Run:
```bash
mill deder-web-dashboard.compile
```

Expected: SUCCESS (no errors from new imports, string interpolations, etc.)

- [ ] **Step 3: Commit any test fixes if needed**

```bash
git add -A
git commit -m "fix: adjust tests for icon-based module graph"
```
