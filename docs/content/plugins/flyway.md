---
title: Flyway
description: Flyway plugin tasks and schema
pagination:
  enabled: false
layout: page.html
---

# Flyway

Runs [Flyway](https://flywaydb.org) Community SQL migrations for Java and Scala modules (not test modules).

```pkl
import "https://sake92.github.io/deder-plugins/config/v0.4.0/FlywayPlugin.pkl" as FW

plugins {
  new FW.FlywayPlugin {
    config = new {
      defaults = new {
        url = "jdbc:postgresql://localhost/app"
        user = "app"
        password = read?("env:DB_PASSWORD") ?? ""
        jdbcDeps {
          "org.postgresql:postgresql:42.7.4"
          "org.flywaydb:flyway-database-postgresql:11.8.2"
        }
      }
    }
  }
}
```

Secrets are plain Pkl values; use `read?("env:...")` to read them from environment variables.

Defaults:
```pkl
enabled = true
url = ""
user = ""
password = ""
locations { "classpath:db/migration" }
schemas {}
jdbcDeps {}
flywayVersion = "11.8.2"
cleanDisabled = true // flywayClean fails unless false
```

Relative `filesystem:` locations resolve against the project root. Only SQL migrations are supported.

## Schema

- [early-access](../config/deder-flyway/early-access/FlywayPlugin.pkl)

## Tasks

- `flywayMigrate`, `flywayInfo`, `flywayValidate`, `flywayBaseline`, `flywayRepair`
- `flywayClean` drops all objects in the configured schemas, requires `cleanDisabled = false`
