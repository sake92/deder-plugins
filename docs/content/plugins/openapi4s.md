---
title: OpenAPI4s
description: Generate editable Scala sources from OpenAPI and JSON Schema
pagination:
  enabled: false
layout: page.html
---

# OpenAPI4s

Generates editable Scala 3 models, server code, and clients using [OpenAPI4s](https://github.com/sake92/openapi4s).

```pkl
import "https://sake92.github.io/deder-plugins/config/deder-openapi4s/early-access/Openapi4sPlugin.pkl" as OA

plugins {
  new OA.Openapi4sPlugin {
    config { defaults { basePackage = "com.example.api" } }
  }
}
```

Run `deder exec -t openapi4sGenerate -m <module-id>`. Generation is explicit and writes editable files to the module's first source directory by default. Add the libraries required by the selected generated backends to the module's dependencies. Review regenerated files for changes to hand-written code.

## Configuration

Set fields in `defaults` or override them in `modules["module-id"]`. Paths are relative to the module root unless absolute.

| Field | Default | Purpose |
|---|---|---|
| `enabled` | `true` | Enable generation. |
| `basePackage` | `""` | Required Scala package for generated sources. |
| `models` | `"tupson"` | Model backend: `tupson` or `circe`. |
| `framework` | `"sharaf"` | Server framework: `sharaf`, `http4s`, or `none`. |
| `client` | `"none"` | Client backend: `sttp` or `none`. |
| `validation` | `"none"` | Validation backend: `none`, `iron`, or `validson`. |
| `tags` | empty | Client tags; empty includes all tags. |
| `input` | first resource directory + `/openapi.json` | OpenAPI document, JSON Schema file, or JSON Schema directory. |
| `targetDir` | first source directory | Output base directory. |
| `version` | `"0.10.0"` | OpenAPI4s CLI version. |

## Schema

- [early-access](../config/deder-openapi4s/early-access/Openapi4sPlugin.pkl)
