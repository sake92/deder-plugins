---
title: Protobuf
description: Protobuf plugin tasks and schema
pagination:
  enabled: false
layout: page.html
---

# Protobuf

Generates protobuf and gRPC Java/Scala code from `.proto` files.

Add it to your plugins list:
```pkl
import "https://sake92.github.io/deder-plugins/config/v0.4.0/ProtobufPlugin.pkl" as PB

plugins {
  new PB.ProtobufPlugin {}
}
```

Here are the defaults if you need to tweak them:
```pkl
plugins {
  new PB.ProtobufPlugin {
    config = new PB.ProtobufPluginConfig {
      defaults = new PB.ModuleDefaults {
        enabled = true
        protoc = new PB.ResolverConfig {
          kind = "system-path"
        }
        builtins = new {}
        plugins = new {}
        imports = new PB.ImportConfig {
          includeProjectDependencies = true
          dependencyArtifacts = new {}
          paths = new {}
        }
        descriptor = null
        main = new PB.SourceSetConfig {}
        test = new PB.SourceSetConfig {}
      }
      modules = new Mapping {}
    }
  }
}
```

## Schema

- [early-access](../config/deder-protobuf/early-access/ProtobufPlugin.pkl)

## Tasks

- `protobufSourceFiles` (source files discovery)
- `protobufGenerate` (source generator)
- `protobufGenerateResources` (resource generator)
