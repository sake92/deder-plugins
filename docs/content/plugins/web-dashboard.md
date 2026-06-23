---
title: Web Dashboard
description: Web Dashboard plugin tasks, schema, and screenshots
pagination:
  enabled: false
layout: page.html
---

# Web Dashboard

Starts an HTTP server app that shows modules info, modules graph, live stats etc.  
By default it is available at http://localhost:9292

Add it to your plugins list:
```pkl
import "https://sake92.github.io/deder-plugins/config/v0.4.0/WebDashboardPlugin.pkl" as WD

plugins {
  new WD.WebDashboardPlugin {}
}
```

Here are the defaults if you need to tweak them:
```pkl
plugins {
  new WD.WebDashboardPlugin {
    config = new {
      enabled = true
      host = "localhost"
      port = 9292
      statsRefreshIntervalMs = 5000
    }
  }
}
```


## Schema

- [early-access](../config/deder-web-dashboard/early-access/WebDashboardPlugin.pkl)

## Tasks

This plugin starts the dashboard server during plugin initialization and does not currently expose dedicated build tasks.

## Screenshots

<article>
  <img src="/images/deder-web-dashboard/home.png" alt="Server info">
  <footer>Server info — Deder version, uptime, JVM stats, and plugins</footer>
</article>

<article>
  <img src="/images/deder-web-dashboard/modules_list.png" alt="Modules list">
  <footer>Modules list — filterable table with type badges</footer>
</article>

<article>
  <img src="/images/deder-web-dashboard/modules_graph.png" alt="Modules graph">
  <footer>Module dependency graph with type coloring</footer>
</article>

<article>
  <img src="/images/deder-web-dashboard/tasks_run.png" alt="Tasks run">
  <footer>Tasks — trigger builds and view execution logs</footer>
</article>

<article>
  <img src="/images/deder-web-dashboard/stats_live.png" alt="Live stats">
  <footer>Live stats — queued, executing, and acquiring requests</footer>
</article>

<article>
  <img src="/images/deder-web-dashboard/stats_history.png" alt="Stats history">
  <footer>History — filterable, sortable build run history</footer>
</article>

<article>
  <img src="/images/deder-web-dashboard/stats_aggregate.png" alt="Stats aggregate">
  <footer>Aggregates — per-task stats, heaviest modules, error summary</footer>
</article>
