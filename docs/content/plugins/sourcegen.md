---
title: Sourcegen
description: Sourcegen plugin tasks and schema
pagination:
  enabled: false
layout: page.html
---

# Sourcegen

Generates source code before module compilation using user-written Scala scripts.

Place scripts in `<module-root>/deder/sourcegen/` with a `@main` entry point. Scripts are compiled and executed independently — they don't require the module to compile first.

Add it to your plugins list:
```pkl
import "https://sake92.github.io/deder-plugins/config/deder-sourcegen/early-access/SourcegenPlugin.pkl" as SG

plugins {
  new SG.SourcegenPlugin {
    config = new {
      defaults = new {
        deps {
          "com.lihaoyi::upickle:4.1.0"
          "com.lihaoyi::os-lib:0.11.3"
        }
        extra {
          ["package"] = "com.example.generated"
        }
      }
    }
  }
}
```

Here are the defaults if you need to tweak them:
```pkl
plugins {
  new SG.SourcegenPlugin {
    config = new {
      defaults = new {
        enabled = true
        deps = new {}
        scriptsDir = "deder/sourcegen"
        scalaVersion = null
        outputSourceSet = "main"
        extra = new Mapping {}
      }
      modules = new Mapping {}
    }
  }
}
```

## Script API

Each script is a `.scala` file with a `@main` entry point. The plugin writes a JSON config file and passes its path as the only argument:

```scala
// core/deder/sourcegen/GenerateCodecs.scala
@main def generate(configPath: String): Unit =
  val config = ujson.read(os.read(os.Path(configPath)))
  val outDir = os.Path(config("outDir").str)
  val pkg = config("config")("package").str

  val source =
    s"""package $pkg
       |
       |case class GeneratedMessage(text: String)
       |""".stripMargin

  os.write(outDir / "GeneratedMessage.scala", source)
```

### Config JSON shape

```json
{
  "outDir": "/path/to/target/deder/sourcegen/main",
  "module": {
    "id": "core",
    "root": "/home/user/project/core",
    "scalaVersion": "3.7.4",
    "moduleType": "scala"
  },
  "config": {
    "package": "com.example.generated"
  }
}
```

The `config` object mirrors the user's `extra` mapping from the Pkl config.

## Schema

- [early-access](../config/deder-sourcegen/early-access/SourcegenPlugin.pkl)

## Tasks

- `sourcegenDiscoverScripts` — discovers `.scala` files in the scripts directory
- `sourcegenGenerate` (source generator) — compiles and runs scripts, writes generated sources
