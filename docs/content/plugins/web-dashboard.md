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
import "https://sake92.github.io/deder-plugins/config/v0.2.0/WebDashboardPlugin.pkl" as WD

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

![Modules list](/images/deder-web-dashboard/modules_list.png)
![Modules graph](/images/deder-web-dashboard/modules_graph.png)
![Server info](/images/deder-web-dashboard/server_info.png)
![Live stats](/images/deder-web-dashboard/live_stats.png)
