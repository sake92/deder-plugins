# AGENTS.md

## Build System

This project uses [Deder](https://sake92.github.io/deder/) as its build tool, configured via Pkl files (`deder.pkl`). JDK 21+ and `pkl-codegen-java` are required.

### Common commands (run from repo root)

```bash
# Compile / test all modules
deder exec -t compile
deder exec -t test

# Compile / test a specific plugin module
deder exec -t compile -m deder-protobuf
deder exec -t test -m deder-protobuf-test

# Same for other plugins
deder exec -t test -m deder-build-info-test
deder exec -t test -m deder-web-dashboard-test

# Publish a plugin locally (for local consumer testing)
deder exec -t publishLocal -m deder-protobuf
```

### Running a single test

munit supports filtering by name. Pass extra args to the test task:

```bash
deder exec -t test -m deder-protobuf-test -- -F "test name substring"
```

### Regenerating Pkl bindings

After editing any `resources/*Plugin.pkl` file, regenerate and commit the Java bindings:

```bash
./scripts/gen-plugin-bindings.sh
```

This runs `pkl-codegen-java` for each plugin and copies output into each module's `src/` and `resources/` directories. **Do not manually edit the generated `.java` files** (they contain the marker `DO NOT EDIT: your changes will be overwritten.`).

## Architecture

The repo contains three Deder plugins, each in its own subdirectory:

| Directory | Plugin ID | Description |
|---|---|---|
| `deder-protobuf/` | `protobuf` | Protobuf / gRPC code generation via `protoc` |
| `deder-build-info/` | `build-info` | Generates a `BuildInfo` Scala object with build metadata |
| `deder-web-dashboard/` | `web-dashboard` | Starts a web dashboard showing module info and live stats |

Each plugin follows the same structure:
- `resources/*Plugin.pkl` — the Pkl schema users reference in their `deder.pkl`
- `src/ba/sake/deder/<plugin>/` — Scala 3 implementation; the entry point is `*PluginImpl extends DederPluginApi`
- `src/ba/sake/deder/*.java` — **generated** Java bindings from `pkl-codegen-java`; do not edit
- `test/` — munit `FunSuite` tests
- `examples/consumer/` — minimal project consuming the published plugin
- `scripts/gen-plugin-bindings.sh` — per-plugin binding generation script (called by root `scripts/gen-plugin-bindings.sh`)

### Plugin lifecycle

Each plugin's `*PluginImpl.init(params)`:
1. Evaluates the Pkl config via `PluginConfigEvaluators.evaluate(...)` using the generated Java bindings
2. Creates and returns a `Seq[AbstractTask[?]]` (tasks wired together with `.dependsOn(...)`)
3. Returns `Left(errorMessage)` on config errors

Tasks are built with `CachedTaskBuilder.make[T](name, supportedModuleTypes, category, kind)`.

### Config normalisation pattern

Each plugin has a `*ConfigNormalizer` that merges `ModuleDefaults` with per-module `ModuleOverride` values before the tasks run. Tests for these live in `test/src/.../config/`.

## What Not to Commit

Do not commit AI agent session artifacts. These are working files for AI tools and must stay out of version control:

- `docs/superpowers/` — Superpowers skill/plan files
- Any session-scoped plan or spec files dropped by AI agents (e.g. `plan.md`, `spec.md` in repo subdirectories)

These paths are already covered by `.gitignore`; do not add exceptions.

## Key Conventions

- **Scala 3**, scalafmt with `runner.dialect = scala3`, `maxColumn = 120`
- **munit** (`org.scalameta::munit`) for all tests; test classes extend `munit.FunSuite`
- The root `deder.pkl` uses `CreateScalaModules` helpers to define both the main module and the test module (named `<id>-test`) for each plugin
- `ModuleDefaults` + `ModuleOverride` pattern: defaults apply to all modules; per-module overrides are `Option`-typed fields that shadow defaults when set
- `supportedModuleTypes` in each task restricts which Deder module types (Java, Scala, their test variants) the task activates for
- if `examples/consumer/` projects reference the plugin via a local `SNAPSHOT` version; use `publishLocal` before running them
