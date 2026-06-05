---
title: Build Info
description: Build Info plugin tasks and schema
pagination:
  enabled: false
layout: page.html
---

# Build Info

Generates a Scala `BuildInfo` object with build metadata constants.

Add it to your plugins list:
```pkl
import "https://sake92.github.io/deder-plugins/config/v0.16.0/BuildInfoPlugin.pkl" as BI

plugins {
  new BI.BuildInfoPlugin {
    config = new BI.BuildInfoPluginConfig {
      modules = new Mapping {
        ["my-module"] = new BI.ModuleOverride {
          extra = new Mapping {
            ["appName"] = "my-service"
            ["buildProfile"] = "dev"
          }
        }
      }
    }
  }
}
```

Here are the defaults if you need to tweak them:
```pkl
plugins {
  new BI.BuildInfoPlugin {
    config = new BI.BuildInfoPluginConfig {
      defaults = new BI.ModuleDefaults {
        enabled = true
        packageName = null
        objectName = "BuildInfo"
        includeGitHash = false
        includeTimestamp = false
        extra = new Mapping {}
      }
      modules = new Mapping {}
    }
  }
}
```

## Schema

- [early-access](../config/deder-build-info/early-access/BuildInfoPlugin.pkl)

## Tasks

- `buildInfoGenerate` (source generator)
