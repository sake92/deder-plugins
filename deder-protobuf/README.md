# deder-protobuf

Standalone Deder plugin for protobuf and gRPC code generation.

Status: MVP



## Minimal plugin config

```pkl
import "resources/ProtobufPlugin.pkl" as PB

plugins {
  new PB.ProtobufPlugin {
    config = new PB.ProtobufPluginConfig {
      defaults = new PB.ModuleDefaults {
        builtins {
          new PB.BuiltinLanguageTarget {
            name = "java"
          }
        }
      }
    }
  }
}
```

## gRPC example config

```pkl
import "resources/ProtobufPlugin.pkl" as PB

plugins {
  new PB.ProtobufPlugin {
    config = new PB.ProtobufPluginConfig {
      defaults = new PB.ModuleDefaults {
        builtins {
          new PB.BuiltinLanguageTarget {
            name = "java"
          }
        }
        plugins {
          new PB.PluginTarget {
            name = "grpc-java"
            resolver = new PB.ResolverConfig {
              kind = "binary-maven"
              dependency = "io.grpc:protoc-gen-grpc-java:1.75.0"
            }
          }
        }
      }
    }
  }
}
```

## Local publish + consumer flow

1. Publish `deder-plugin-api` locally from a sibling Deder checkout if needed.
2. Publish this plugin locally from the repo root:

```bash
deder exec -t publishLocal -m deder-protobuf
```

3. Use `examples/consumer` as a minimal project that consumes the published plugin.

## Directory conventions

- Maven-like modules default to `src/main/protobuf`, `src/main/proto`, `src/test/protobuf`, and `src/test/proto`.
- Default-layout modules fall back to `protobuf`, `proto`, `test/protobuf`, and `test/proto`.
- Proto inputs are treated as build inputs, not generic runtime resources.

## Deferred follow-up work

The following are intentionally out of scope for the current MVP and fail fast if configured:

- URL-based tool distributions
- digest verification
- sanctioned executable paths
- optional plugin skipping on unsupported platforms
- descriptor inputs beyond generated descriptor-set output

## Development
Prerequisites:
- `protoc` when using the system-path resolver



