# deder-squery

Deder plugin for generating Squery row models and DAOs from a JDBC database.

Add the plugin to `deder.pkl` and configure a JDBC URL, driver dependency, and
schema to Scala package mapping:

```pkl
import "resources/SqueryPlugin.pkl" as SQ

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

Run `deder exec -t squeryGenerate -m <module-id>`. Generation also runs before
compilation because `squeryGenerate` is a source generator. Add
`ba.sake::squery:0.12.0` and your JDBC driver to the consuming module's `deps`
when the generated code uses them.

The `defaults` object also supports `colNameIdentifierMapper`, `typeNameMapper`,
`rowTypeSuffix`, `daoTypeSuffix`, `typeMappingRules`, `includeTables`,
`excludeTables`, and `squeryVersion`. CLI options with multiple values use Pkl
listings. `includeTables` defaults to all tables; exclusions take precedence.
Override any field for one module in `config.modules["module-id"]` or set
`enabled = false` there. Generated files live in Deder's source generator output.
