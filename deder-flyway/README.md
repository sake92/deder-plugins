# deder-flyway

Deder plugin for running [Flyway](https://flywaydb.org) (Community edition) SQL migrations.
Supports Java and Scala modules (not test modules).

```pkl
import "resources/FlywayPlugin.pkl" as FW

plugins {
  new FW.FlywayPlugin {
    config = new {
      defaults = new {
        url = "jdbc:h2:./database"
        user = "sa"
        password = read?("env:DB_PASSWORD") ?? ""
        jdbcDeps { "com.h2database:h2:2.3.232" }
        locations { "classpath:db/migration"; "filesystem:db/extra" }
      }
    }
  }
}
```

Secrets are plain Pkl values, use `read?("env:...")` to take them from environment variables.

## Tasks

Run with `deder exec -t <task> -m <module-id>`:

- `flywayMigrate`, `flywayInfo`, `flywayValidate`, `flywayBaseline`, `flywayRepair`
- `flywayClean` is destructive and fails unless `cleanDisabled = false` is configured

`flywayUndo` is not provided, undo migrations are a paid Flyway edition feature.

## Config

`defaults` (overridable per module in `config.modules["module-id"]`):
`enabled`, `url`, `user`, `password`, `locations` (default `classpath:db/migration`), `schemas`,
`jdbcDeps`, `flywayVersion` (default `11.8.2`), `cleanDisabled` (default `true`).

- `classpath:` locations are read from the module's run classpath, so compile first (`deder exec -t compile`) after changing migrations; `flywayMigrate` already depends on the classpath task.
- Relative `filesystem:` locations are resolved against the project root.
- Only SQL migrations are supported. Flyway 10+ needs a database module besides the driver for some databases, add it to `jdbcDeps` (e.g. `org.flywaydb:flyway-database-postgresql:11.8.2`).
