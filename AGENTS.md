# Copilot instructions for `deder-plugins`

## Build and test commands

Run commands from the repository root. The root `deder.pkl` defines the real build; it generates the `deder-protobuf` and `deder-protobuf-test` modules even though the code lives under `deder-protobuf/`.

- Compile the main plugin module:
  ```bash
  deder exec -t compile -m deder-protobuf
  ```
- Compile the test module:
  ```bash
  deder exec -t compile -m deder-protobuf-test
  ```
- List discovered test suites:
  ```bash
  deder exec -t testClasses -m deder-protobuf-test --json
  ```
- Run the documented full test task, and use `testClasses` above as the check for what Deder actually discovered in the current workspace:
  ```bash
  deder exec -t test -m deder-protobuf-test
  ```
- Run a single MUnit suite with the generated Deder classpath:
  ```bash
  deder exec -t test -m deder-protobuf-test ba.sake.deder.protobuf.protoc.BinaryMavenDependencySuite
  ```
- After editing `deder-protobuf/resources/ProtobufPlugin.pkl`, regenerate the committed bindings:
  ```bash
  ./scripts/gen-plugin-bindings.sh
  ```

Prerequisites called out by the repo docs: JDK 21+, `deder`, `pkl-codegen-java`, and `protoc` when using the `system-path` resolver.

## High-level architecture

- `deder.pkl` at the repo root is the build entrypoint. It uses `CreateScalaModules` to create a publishable `deder-protobuf` module and its paired `deder-protobuf-test` module, both rooted in `deder-protobuf/`.
- `deder-protobuf/src/ba/sake/deder/protobuf/ProtobufPluginImpl.scala` is the plugin entrypoint. It evaluates the typed Pkl config from `ProtobufPlugin.pkl` and wires exactly three Deder tasks: `protobufSourceFiles`, `protobufGenerate`, and `protobufGenerateResources`.
- `config/ProtobufConfigNormalizer.scala` is the normalization boundary between the generated Pkl model and the runtime tasks. It merges repo-wide defaults with per-module overrides, infers proto source directories from the module layout, and rejects deferred parity options such as URL resolvers and digest verification instead of silently ignoring them.
- `sources/ProtoInputResolver.scala` is responsible for the full proto input graph. It collects local `.proto` files, extra import paths, configured dependency artifacts, and project dependency artifacts; `ArchiveProtoExtractor.scala` unpacks `.jar` and `.zip` imports into task-local workspaces so `protoc` can consume them as import roots.
- `tasks/ProtobufGenerationTasks.scala` orchestrates generation. It normalizes config, resolves imports, resolves `protoc` plus plugin executables, builds the command line, runs `protoc`, and returns task-local output directories. Optional descriptor output is the only generated artifact that is copied through the resource-generator task.
- The executable-resolution layer lives under `protoc/`. `ExecutableResolverSupport.scala` handles `system-path`, `path`, `binary-maven`, and `jvm-maven` resolvers; `ProtocCommandBuilder.scala` turns the resolved model into CLI flags; `ProtocRunner.scala` runs the command and surfaces stdout/stderr through Deder notifications.
- `deder-protobuf/examples/consumer/` is the integration example for the local publish flow after `deder exec -t publishLocal -m deder-protobuf`.

## Key conventions

- Treat `deder-protobuf/resources/ProtobufPlugin.pkl` as the source of truth for plugin configuration. The generated Java bindings under `deder-protobuf/src/ba/sake/deder` and extra generated resources are committed and must be regenerated when the Pkl schema changes.
- Keep build assumptions anchored to the root `deder.pkl`, not to the `deder-protobuf/` directory layout. The module source roots are nested, but task execution is driven from the repository root.
- New tests should follow the existing subsystem split under `deder-protobuf/test/src/ba/sake/deder/protobuf/`: `config`, `sources`, `protoc`, and `tasks`. The suites are MUnit `FunSuite`s named after the runtime component they cover.
- Configuration fields that are intentionally out of MVP scope are still present in the Pkl schema for forward compatibility, but the Scala layer is expected to fail fast when they are set. Preserve that explicit rejection behavior instead of adding silent fallbacks.
- Proto files are treated as build inputs, not generic resources. Keep source discovery, import resolution, and generated output handling inside the Deder task graph rather than writing generated files back into tracked source directories.
