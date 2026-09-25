---
title: Squery
description: Generate Squery models and DAOs from a JDBC database
pagination:
  enabled: false
layout: page.html
---

# Squery

Generate Scala row models and DAOs from a JDBC database before compilation.

Add the plugin to `deder.pkl`:

```pkl
import "https://sake92.github.io/deder-plugins/config/deder-squery/early-access/SqueryPlugin.pkl" as SQ

plugins {
  new SQ.SqueryPlugin {
    config = new {
      defaults = new {
        jdbcUrl = "jdbc:h2:./database"
        jdbcDeps { "com.h2database:h2:2.3.232" }
        schemaMappings { ["PUBLIC"] = "example.db" }
      }
    }
  }
}
```

Add `ba.sake::squery:0.12.0` and the JDBC driver to the consuming Scala
module's dependencies. Run `deder exec -t squeryGenerate -m <module-id>`, or
compile the module to generate sources automatically.

## Configuration

`defaults` applies to each Scala module. Set fields in `modules["module-id"]`
to override them for one module. Set `enabled = false` to skip generation.

| Field | Default | Purpose |
|---|---|---|
| `jdbcUrl` | `""` | JDBC connection URL; required when enabled. |
| `jdbcDeps` | empty | Maven dependencies for the JDBC driver. |
| `schemaMappings` | empty | Database schema to Scala package mappings; required when enabled. |
| `colNameIdentifierMapper` | `"camelcase"` | Column identifier mapper (`camelcase` or `noop`). |
| `typeNameMapper` | `"camelcase"` | Type name mapper (`camelcase` or `noop`). |
| `rowTypeSuffix` | `"Row"` | Generated row type suffix. |
| `daoTypeSuffix` | `"Dao"` | Generated DAO type suffix. |
| `typeMappingRules` | empty | Ordered `column-regex\|declared-type-regex\|Scala-type` rules. |
| `includeTables` | all tables | Regexes matching `schema.table`; ordered and repeatable. |
| `excludeTables` | empty | Regexes to exclude; exclusions take precedence. |
| `squeryVersion` | `"0.12.0"` | Squery CLI version. |

Generated files are written to Deder's `squeryGenerate/sources` output.

## Schema

- [early-access](../config/deder-squery/early-access/SqueryPlugin.pkl)
