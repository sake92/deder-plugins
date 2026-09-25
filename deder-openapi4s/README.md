# deder-openapi4s

Deder plugin for [OpenAPI4s](https://github.com/sake92/openapi4s). It generates editable Scala 3 models, server code, and clients from an OpenAPI document or local JSON Schema input.

## Setup

Publish this plugin locally with `deder exec -t publishLocal -m deder-openapi4s` for local consumer testing. Configure a consumer's `deder.pkl`:

```pkl
import "path/to/Openapi4sPlugin.pkl" as OA

plugins {
  new OA.Openapi4sPlugin {
    config = new {
      defaults = new {
        basePackage = "com.example.api"
      }
    }
  }
}
```

Run `deder exec -t openapi4sGenerate -m <module-id>`. The task writes editable files into the module's first source directory and is invoked explicitly. It does not add the OpenAPI4s CLI to the consumer's compile dependencies. Add the libraries needed by your chosen generated backends to the consumer module.

## Settings

Set these fields in `config.defaults`; use `config.modules["module-id"]` to override a field for one module. Paths are relative to the module root unless absolute.

| Field | Default | Meaning |
|---|---|---|
| `enabled` | `true` | Enable generation for this module |
| `basePackage` | required | Scala package for generated sources |
| `models` | `"tupson"` | `tupson` or `circe` |
| `framework` | `"sharaf"` | `sharaf`, `http4s`, or `none` |
| `client` | `"none"` | `sttp` or `none` |
| `validation` | `"none"` | `none`, `iron`, or `validson` |
| `tags` | empty listing | Client tags; empty includes all tags |
| `input` | first resource directory + `/openapi.json` | OpenAPI document, JSON Schema file, or JSON Schema directory |
| `targetDir` | first source directory | Output base directory |
| `version` | `"0.9.0"` | OpenAPI4s CLI version |

Set `framework = "none"` or `client = "none"` to omit those CLI options. Generated files can contain hand-written changes, so review regeneration before committing.

See [`examples/consumer`](examples/consumer/deder.pkl) for a minimal working configuration.
