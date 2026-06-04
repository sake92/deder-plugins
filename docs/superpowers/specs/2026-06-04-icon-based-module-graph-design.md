# Icon-Based Module Graph Design

**Date:** 2026-06-04
**Status:** Draft

## Overview

Replace the current color-only module type distinction in the web dashboard's dependency graph with language/platform icons, eliminating the red=error ambiguity and adding proper visual identity for Scala.js and Scala Native modules.

## Module Types

The DederProject schema defines 9 module types in a class hierarchy:

```
DederModule
└── JavaModule  (type: "java")
    ├── JavaTestModule      (type: "java-test")
    └── ScalaModule         (type: "scala")
        ├── ScalaTestModule       (type: "scala-test")
        ├── ScalaJsModule         (type: "scala-js")
        │   └── ScalaJsTestModule     (type: "scala-js-test")
        └── ScalaNativeModule     (type: "scala-native")
            └── ScalaNativeTestModule (type: "scala-native-test")
```

The web dashboard currently only handles the JVM 4 (`ScalaModule`, `ScalaTestModule`, `JavaModule`, `JavaTestModule`). The other 4 types fall through to an "UNKNOWN" catch-all.

## Goal

Make each module type visually distinguishable and intuitive, without relying on color as the primary differentiator.

## Icon Assets

Download official SVG logos and serve as static assets:

| Icon | Source | File Path |
|------|--------|-----------|
| Scala logo | scala-lang.org | `resources/public/icons/scala.svg` |
| Java logo | oracle.com/java | `resources/public/icons/java.svg` |
| Scala.js logo | scala-js.org | `resources/public/icons/scalajs.svg` |
| Scala Native logo | scala-native.org | `resources/public/icons/scalanative.svg` |

All SVGs should be simplified where needed to render well at small sizes (18-20px effective icon size within a 32px node).

## Visual Rules

### Shape (existing, unchanged)
- **ellipse** — regular (non-test) modules
- **round-rectangle** — test modules

### Border
- **solid `#ccc`, 1px** — regular modules
- **dashed `#999`, 2px** — test modules (the "test badge")

### Background
- **Neutral white** (`#f8f9fa`) for all module types — no language colors

### Icon
- Centered within the node, fitted to ~65% of node dimensions

| Type | Icon | Shape | Border |
|------|------|-------|--------|
| `ScalaModule` | scala.svg | ellipse | solid |
| `ScalaTestModule` | scala.svg | round-rectangle | dashed |
| `ScalaJsModule` | scalajs.svg | ellipse | solid |
| `ScalaJsTestModule` | scalajs.svg | round-rectangle | dashed |
| `ScalaNativeModule` | scalanative.svg | ellipse | solid |
| `ScalaNativeTestModule` | scalanative.svg | round-rectangle | dashed |
| `JavaModule` | java.svg | ellipse | solid |
| `JavaTestModule` | java.svg | round-rectangle | dashed |
| Fallback | none (grey node) | ellipse | solid |

### Node Size
- Increase from 28×28 to **32×32** pixels to improve icon legibility.

### Label
- Dark text (`#222`), font-size 10px, centered below/within node.
- No need for text outline (dark on neutral is readable).

## Cytoscape.js Implementation

### Data Format

Each node JSON object carries:
```json
{
  "data": {
    "id": "module-name",
    "type": "SCALA",           // "SCALA" | "JAVA" | "SCALA_JS" | "SCALA_NATIVE" | "SCALA_TEST" | "JAVA_TEST" | "SCALA_JS_TEST" | "SCALA_NATIVE_TEST" | "UNKNOWN"
    "label": "module-name",
    "icon": "/icons/scala.svg" // URL path to the SVG
  }
}
```

### Styles

```js
// Base node style (neutral background, icon, label)
{ selector: 'node', style: {
    'background-color': '#f8f9fa',
    'background-image': 'data(icon)',
    'background-fit': 'contain',
    'background-width': '65%',
    'background-height': '65%',
    'label': 'data(label)',
    'font-size': '10px',
    'text-valign': 'center',
    'text-halign': 'center',
    'color': '#222',
    'width': 32,
    'height': 32,
    'border-width': 1,
    'border-color': '#ccc'
}},

// Language/platform icons — set via data(icon) above, no per-language style needed

// Shapes
{ selector: 'node[type="SCALA"]',             style: { 'shape': 'ellipse' } },
{ selector: 'node[type="JAVA"]',              style: { 'shape': 'ellipse' } },
{ selector: 'node[type="SCALA_JS"]',          style: { 'shape': 'ellipse' } },
{ selector: 'node[type="SCALA_NATIVE"]',      style: { 'shape': 'ellipse' } },

// Test modules: round-rectangle shape
{ selector: 'node[type="SCALA_TEST"]',        style: { 'shape': 'round-rectangle', 'border-style': 'dashed', 'border-width': 2, 'border-color': '#999' } },
{ selector: 'node[type="JAVA_TEST"]',         style: { 'shape': 'round-rectangle', 'border-style': 'dashed', 'border-width': 2, 'border-color': '#999' } },
{ selector: 'node[type="SCALA_JS_TEST"]',     style: { 'shape': 'round-rectangle', 'border-style': 'dashed', 'border-width': 2, 'border-color': '#999' } },
{ selector: 'node[type="SCALA_NATIVE_TEST"]', style: { 'shape': 'round-rectangle', 'border-style': 'dashed', 'border-width': 2, 'border-color': '#999' } }
```

Alternative: because all test modules share the same shape+border, a single wildcard selector would be cleaner:
```js
{ selector: 'node[type$="_TEST"]', style: { 'shape': 'round-rectangle', 'border-style': 'dashed', 'border-width': 2, 'border-color': '#999' } },
{ selector: 'node', style: { 'shape': 'ellipse' } }  // base default
```

The `$=` suffix-selector matches any type ending in `_TEST`, covering all 4 test variants.

### Highlight (tap behavior)
- The `.highlighted` CSS class changes the border color. Since we now use a custom border for test modules, ensure the highlight overrides border-style as well. A simple yellow solid border for highlighted nodes will work.

## Filter Toggle Controls (5 Checkboxes)

```
☑ Scala     ☑ Java     ☑ Scala.js     ☑ Scala Native     ☑ Test
```

### Toggle logic (in dashboard.js)

- **Platform toggles**: show/hide all modules of that platform (regular + test variants together)
- **Test toggle**: when OFF, hide all test modules regardless of platform; when ON, test modules are shown if their platform toggle is also ON
- **Combined**: module is visible if: platform toggle is ON AND (type is not test OR test toggle is ON)

### Implementation

Replace the current 4 `toggleGraphType()` calls with a single `applyFilters()` function that reads all 5 checkbox states and iterates over all nodes.

## Legend

Replace the colored-dot legend with icon mini-previews:

```
[scala.svg icon] Scala    [java.svg icon] Java    [scalajs.svg icon] Scala.js    [scalanative.svg icon] Scala Native
[dashed-rectangle ▭] Test
— Arrow = depends on. Tap a node to highlight its neighborhood.
```

## Files Changed

| File | Change |
|------|--------|
| `deder-web-dashboard/src/.../ModulesGraphPage.scala` | Add 4 new type imports; update `buildGraphJson()` with 9-type match; update Cytoscape styles; update legend; update Alpine toggle bindings |
| `deder-web-dashboard/resources/public/dashboard.js` | Replace `toggleGraphType()` with `applyFilters()` using 5-checkbox logic |
| `deder-web-dashboard/resources/public/icons/scala.svg` | New — Scala logo |
| `deder-web-dashboard/resources/public/icons/java.svg` | New — Java logo |
| `deder-web-dashboard/resources/public/icons/scalajs.svg` | New — Scala.js logo |
| `deder-web-dashboard/resources/public/icons/scalanative.svg` | New — Scala Native logo |

## Files Not Changed

- `Layout.scala` — still loads same Cytoscape/dagre JS libs
- `ModulesPage.scala` — table view keeps its existing color-based badges (not affected)
- `dashboard.js` — only the filter function is updated, not the search/reset
- All other plugins (protobuf, build-info) — unrelated

## Edge Cases

- **Unknown module type**: grey node, no icon, ellipse shape, solid border. Visible when user searches or a dependency links to it.
- **No modules**: unchanged behavior — shows "No modules found" message.
- **Highlight tap**: ensure `.highlighted` class overrides border-style to solid yellow for visibility on all node types.
- **Print / B&W**: icons are recognizable shapes, dashed border is visible in monochrome.
- **Icon not found / broken URL**: Cytoscape silently shows the colored background without an image. Neutral background still looks fine, and the label text is always present.
- **Scala.js or Scala Native module with empty deps**: handled the same as any module, no special case.
