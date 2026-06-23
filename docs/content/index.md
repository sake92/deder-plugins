---
title: Deder Plugins
description: Official Deder Build Tool Plugins
pagination:
  enabled: false
layout: page.html
---

# Deder Plugins

Official plugins for the [Deder build tool](https://sake92.github.io/deder/).

## Using a Plugin

Add a plugin to your `deder.pkl` by importing its config, and adding it in the `plugins` listing:

```pkl
import "https://sake92.github.io/deder-plugins/config/deder-web-dashboard/early-access/WebDashboardPlugin.pkl" as WD

plugins {
  new WD.WebDashboardPlugin {
    // version = "1.2.3" // optionally override version
    config { // most plugins have some config

    }
  }
}
```

Versioned schemas are published automatically for every git tag.

Each plugin also has an `examples/` folder in this GitHub repository.

## Available Plugins

| Plugin | Schema |
|--------|--------|
| **[Build Info](plugins/build-info.html)** | [early-access](config/deder-build-info/early-access/BuildInfoPlugin.pkl) |
| Generates a Scala `BuildInfo` object with metadata. | |
| **[Protobuf](plugins/protobuf.html)** | [early-access](config/deder-protobuf/early-access/ProtobufPlugin.pkl) |
| Generates protobuf and gRPC code from `.proto` files. | |
| **[Web Dashboard](plugins/web-dashboard.html)** | [early-access](config/deder-web-dashboard/early-access/WebDashboardPlugin.pkl) |
| Embedded HTTP dashboard with module info and live stats. | |
| **[TUI Dashboard](plugins/tui-dashboard.html)** | — |
| Terminal-based dashboard that polls the Web Dashboard API. | |
