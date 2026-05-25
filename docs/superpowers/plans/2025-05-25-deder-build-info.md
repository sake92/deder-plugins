# Deder BuildInfo Plugin — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a `deder-build-info` plugin that generates a Scala `object BuildInfo` with build metadata constants (module name, version, Scala/Java version, git hash, timestamp, user-defined extras).

**Architecture:** Mirrors `deder-protobuf` exactly: Pkl config schema → generated Java bindings → Scala normalizer → single `CachedTaskBuilder` source generator task. No new Deder framework types needed.

**Tech Stack:** Scala 3.7.4, Pkl (config schema), pkl-codegen-java (bindings), os-lib (git), MUnit (testing).

---

### Task 1: Project Scaffold — Directory Structure and Build Config

**Files:**
- Create: `deder-build-info/resources/` (dir structure)
- Create: `deder-build-info/scripts/` (dir)
- Create: `deder-build-info/src/ba/sake/deder/buildinfo/` (dir)
- Create: `deder-build-info/test/src/ba/sake/deder/buildinfo/` (dir)
- Create: `deder-build-info/examples/consumer/` (dir)
- Modify: `deder.pkl`

- [ ] **Step 1: Create directory structure**

```bash
mkdir -p deder-build-info/resources/META-INF/services
mkdir -p deder-build-info/resources/META-INF/org/pkl/config/java/mapper/classes
mkdir -p deder-build-info/scripts
mkdir -p deder-build-info/src/ba/sake/deder/buildinfo
mkdir -p deder-build-info/test/src/ba/sake/deder/buildinfo
mkdir -p deder-build-info/examples/consumer
```

- [ ] **Step 2: Add BuildInfo plugin modules to root `deder.pkl`**

Modify `/home/sake/projects/sake92/deder-plugins/deder.pkl` — add a second `CreateScalaModules` block after the existing `pluginModules` block, and add it to the `modules` block:

```pkl
local const buildInfoModules =
  new CreateScalaModules {
    root = "deder-build-info"
    id = "deder-build-info"
    layout = "default"
    template = new ScalaModule {
      scalaVersion = scalaVer
      deps {
        "ba.sake::deder-plugin-api:\(pluginApiVersion)"
        "com.lihaoyi::os-lib:0.11.6"
      }
      pomSettings =
        pom(
          "deder-build-info",
          "Deder BuildInfo Plugin",
          "Deder plugin that generates a BuildInfo Scala object with build metadata constants",
        )
      publish = true
    }
    testTemplate = (template.asTest()) {
      deps {
        "org.scalameta::munit:1.2.1"
      }
    }
  }.get

modules {
  ...pluginModules.all
  ...buildInfoModules.all
}
```

The full `deder.pkl` after modification:

```pkl
amends "https://sake92.github.io/deder/config/v0.9.1/DederProject.pkl"

local const scalaVer = "3.7.4"
local const pluginApiVersion = "0.7.8-SNAPSHOT"
local const pluginVersion = "0.1.0-SNAPSHOT"

local const function pom(artId: String, artName: String, artDesc: String): PomSettings = new {
  groupId = "ba.sake"
  artifactId = artId
  version = pluginVersion
  name = artName
  description = artDesc
  url = "https://github.com/sake92/deder-plugins"
  licenses {
    new PomLicense {
      name = "Apache 2.0 License"
      url = "https://opensource.org/license/apache-2-0"
    }
  }
}

local const pluginModules =
  new CreateScalaModules {
    root = "deder-protobuf"
    id = "deder-protobuf"
    layout = "default"
    template = new ScalaModule {
      scalaVersion = scalaVer
      deps {
        "ba.sake::deder-plugin-api:\(pluginApiVersion)"
        "com.lihaoyi::os-lib:0.11.6"
      }
      pomSettings =
        pom(
          "deder-protobuf",
          "Deder Protobuf Plugin",
          "Deder plugin for protobuf and gRPC code generation",
        )
      publish = true
    }
    testTemplate = (template.asTest()) {
      deps {
        "org.scalameta::munit:1.2.1"
      }
    }
  }.get

local const buildInfoModules =
  new CreateScalaModules {
    root = "deder-build-info"
    id = "deder-build-info"
    layout = "default"
    template = new ScalaModule {
      scalaVersion = scalaVer
      deps {
        "ba.sake::deder-plugin-api:\(pluginApiVersion)"
        "com.lihaoyi::os-lib:0.11.6"
      }
      pomSettings =
        pom(
          "deder-build-info",
          "Deder BuildInfo Plugin",
          "Deder plugin that generates a BuildInfo Scala object with build metadata constants",
        )
      publish = true
    }
    testTemplate = (template.asTest()) {
      deps {
        "org.scalameta::munit:1.2.1"
      }
    }
  }.get

modules {
  ...pluginModules.all
  ...buildInfoModules.all
}
```

- [ ] **Step 3: Compile to verify build integration**

```bash
deder exec -t compile -m deder-build-info
```

Expected: succeeds (empty source directory, no source files — compiles nothing but verifies build config is valid).

- [ ] **Step 4: Commit**

```bash
git add deder.pkl deder-build-info/
git commit -m "feat: scaffold deder-build-info plugin directory and build config"
```

---

### Task 2: Pkl Config Schema

**Files:**
- Create: `deder-build-info/resources/BuildInfoPlugin.pkl`

- [ ] **Step 1: Write BuildInfoPlugin.pkl**

Write the schema following the exact pattern from `deder-protobuf/resources/ProtobufPlugin.pkl`:

```pkl
module ba.sake.deder.buildinfo

import "https://sake92.github.io/deder/config/v0.9.1/DederProject.pkl" as P

class BuildInfoPlugin extends P.DederPlugin {
  id = "build-info"
  deps {
    "ba.sake::deder-build-info:0.1.0-SNAPSHOT"
  }
  config: BuildInfoPluginConfig = new {}
}

class BuildInfoPluginConfig {
  defaults: ModuleDefaults = new {}
  modules: Mapping<String, ModuleOverride> = new Mapping {}
}

class ModuleDefaults {
  enabled: Boolean = true
  packageName: String? = null
  objectName: String = "BuildInfo"
  includeGitHash: Boolean = false
  includeTimestamp: Boolean = false
  extra: Mapping<String, String> = new Mapping {}
}

class ModuleOverride {
  enabled: Boolean? = null
  packageName: String? = null
  objectName: String? = null
  includeGitHash: Boolean? = null
  includeTimestamp: Boolean? = null
  extra: Mapping<String, String>? = null
}

config: BuildInfoPluginConfig = new {}
```

- [ ] **Step 2: Commit**

```bash
git add deder-build-info/resources/BuildInfoPlugin.pkl
git commit -m "feat: add BuildInfo plugin Pkl config schema"
```

---

### Task 3: Generate Java Bindings

**Files:**
- Create: `deder-build-info/scripts/gen-plugin-bindings.sh`
- Create: `deder-build-info/src/ba/sake/deder/BuildInfo.java` (generated, committed)

- [ ] **Step 1: Write the gen-plugin-bindings.sh script**

Follow the exact pattern from `deder-protobuf/scripts/gen-plugin-bindings.sh`, adjusting only the Pkl file reference and marker strings for the build-info plugin:

```bash
#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PKL_FILE="$ROOT_DIR/resources/BuildInfoPlugin.pkl"
WORK_DIR="$ROOT_DIR/tmp/.gen-plugin-bindings.$BASHPID"
JAVA_GENERATED_MARKER="DO NOT EDIT: your changes will be overwritten."
RESOURCE_GENERATED_MARKER="Generated by ./scripts/gen-plugin-bindings.sh; do not edit by hand."

cleanup() {
  rm -rf "$WORK_DIR"
}
trap cleanup EXIT

remove_generated_files() {
  local target_dir="$1"
  local file_glob="$2"
  local marker="$3"

  if [[ ! -d "$target_dir" ]]; then
    return
  fi

  python - "$target_dir" "$file_glob" "$marker" <<'PY'
from pathlib import Path
import sys

target_dir = Path(sys.argv[1])
file_glob = sys.argv[2]
marker = sys.argv[3]

for path in target_dir.rglob(file_glob):
    if not path.is_file():
        continue

    try:
        contents = path.read_text()
    except UnicodeDecodeError:
        continue

    if marker in contents:
        path.unlink()

for path in sorted(target_dir.rglob("*"), key=lambda item: len(item.parts), reverse=True):
    if path.is_dir():
        try:
            path.rmdir()
        except OSError:
            pass
PY
}

add_generated_warning() {
  local java_dir="$1"

  if [[ ! -d "$java_dir" ]]; then
    return
  fi

  python - "$java_dir" <<'PY'
from pathlib import Path
import sys

java_dir = Path(sys.argv[1])
header = """/*
 * This file is generated by ./scripts/gen-plugin-bindings.sh from resources/BuildInfoPlugin.pkl.
 * DO NOT EDIT: your changes will be overwritten.
 */

"""

for java_file in java_dir.rglob("*.java"):
    contents = java_file.read_text()
    if "DO NOT EDIT" not in contents:
        java_file.write_text(header + contents)
PY
}

normalize_generated_resources() {
  local resources_dir="$1"

  if [[ ! -d "$resources_dir" ]]; then
    return
  fi

  python - "$resources_dir" <<'PY'
from pathlib import Path
import sys

resources_dir = Path(sys.argv[1])
properties_file = resources_dir / "META-INF" / "org" / "pkl" / "config" / "java" / "mapper" / "classes" / "ba.sake.deder.buildinfo.properties"

if properties_file.exists():
    lines = properties_file.read_text().splitlines()
    if len(lines) >= 2 and lines[0].startswith("#Java mappings for Pkl module"):
        lines[1] = "# Generated by ./scripts/gen-plugin-bindings.sh; do not edit by hand."
        properties_file.write_text("\n".join(lines) + "\n")
else:
    print(
        f"warning: expected generated mapper properties at {properties_file} but pkl-codegen-java did not produce it",
        file=sys.stderr,
    )
PY
}

if ! command -v pkl-codegen-java >/dev/null 2>&1; then
  echo "pkl-codegen-java is required on PATH" >&2
  exit 1
fi

echo "Generating Java bindings from $PKL_FILE"
mkdir -p "$WORK_DIR"
pkl-codegen-java "$PKL_FILE" -o "$WORK_DIR"
add_generated_warning "$WORK_DIR/java"
normalize_generated_resources "$WORK_DIR/resources"
remove_generated_files "$ROOT_DIR/src" "*.java" "$JAVA_GENERATED_MARKER"
remove_generated_files "$ROOT_DIR/resources" "*" "$RESOURCE_GENERATED_MARKER"
cp -r "$WORK_DIR/java/." "$ROOT_DIR/src"
cp -r "$WORK_DIR/resources/." "$ROOT_DIR/resources"
echo "Bindings updated under src and resources"
```

Make it executable:

```bash
chmod +x deder-build-info/scripts/gen-plugin-bindings.sh
```

- [ ] **Step 2: Run the generation script**

```bash
./deder-build-info/scripts/gen-plugin-bindings.sh
```

Expected: generates `deder-build-info/src/ba/sake/deder/BuildInfo.java` and the mapper properties file.

- [ ] **Step 3: Verify generated BuildInfo.java**

Run `git diff --stat` to confirm only new generated files were created. The `BuildInfo.java` should follow the same structure as `Protobuf.java` but with build-info-specific classes: `BuildInfo`, `BuildInfoPluginConfig`, `ModuleDefaults`, `ModuleOverride`.

- [ ] **Step 4: Update root bindings script to cover both plugins**

The root script at `scripts/gen-plugin-bindings.sh` currently `exec`s only the protobuf script. Modify it to run both:

```bash
#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
"$ROOT_DIR/deder-protobuf/scripts/gen-plugin-bindings.sh" "$@"
"$ROOT_DIR/deder-build-info/scripts/gen-plugin-bindings.sh" "$@"
```

- [ ] **Step 5: Commit**

```bash
git add deder-build-info/scripts/gen-plugin-bindings.sh \
        deder-build-info/src/ba/sake/deder/BuildInfo.java \
        deder-build-info/resources/META-INF/org/pkl/config/java/mapper/classes/ba.sake.deder.buildinfo.properties \
        scripts/gen-plugin-bindings.sh
git commit -m "feat: generate Java bindings from BuildInfoPlugin.pkl"
```

---

### Task 4: BuildInfoConfigNormalizer

**Files:**
- Create: `deder-build-info/src/ba/sake/deder/buildinfo/BuildInfoConfigNormalizer.scala`

- [ ] **Step 1: Write the test first (TDD)**

Create `deder-build-info/test/src/ba/sake/deder/buildinfo/BuildInfoConfigNormalizerSuite.scala`:

```scala
package ba.sake.deder.buildinfo

import ba.sake.deder.BuildInfo
import munit.FunSuite

import java.util.{List as JList, Map as JMap}

class BuildInfoConfigNormalizerSuite extends FunSuite {

  test("defaults resolve to expected values") {
    val defaults = new BuildInfo.ModuleDefaults(
      true,
      null,
      "BuildInfo",
      false,
      false,
      JMap.of()
    )
    val config = new BuildInfo.BuildInfoPluginConfig(defaults, JMap.of())

    val resolved = BuildInfoConfigNormalizer.normalize(
      moduleId = "my-module",
      moduleType = "JAVA",
      scalaVersion = None,
      config = config
    )

    assertEquals(resolved.enabled, true)
    assertEquals(resolved.packageName, None)
    assertEquals(resolved.objectName, "BuildInfo")
    assertEquals(resolved.includeGitHash, false)
    assertEquals(resolved.includeTimestamp, false)
    assertEquals(resolved.extra, Map.empty)
    assertEquals(resolved.scalaVersion, None)
  }

  test("packageName resolves to Some when set") {
    val defaults = new BuildInfo.ModuleDefaults(
      true,
      "com.example",
      "BuildInfo",
      false,
      false,
      JMap.of()
    )
    val config = new BuildInfo.BuildInfoPluginConfig(defaults, JMap.of())

    val resolved = BuildInfoConfigNormalizer.normalize(
      moduleId = "my-module",
      moduleType = "SCALA",
      scalaVersion = Some("3.7.4"),
      config = config
    )

    assertEquals(resolved.packageName, Some("com.example"))
    assertEquals(resolved.scalaVersion, Some("3.7.4"))
  }

  test("module override wins over defaults") {
    val defaults = new BuildInfo.ModuleDefaults(
      true,
      null,
      "BuildInfo",
      false,
      false,
      JMap.of()
    )
    val moduleOverride = new BuildInfo.ModuleOverride(
      null,
      "com.override",
      "MyBuildInfo",
      true,
      null,
      JMap.of("myKey", "myValue")
    )
    val config = new BuildInfo.BuildInfoPluginConfig(defaults, JMap.of("my-module", moduleOverride))

    val resolved = BuildInfoConfigNormalizer.normalize(
      moduleId = "my-module",
      moduleType = "SCALA",
      scalaVersion = Some("3.7.4"),
      config = config
    )

    assertEquals(resolved.packageName, Some("com.override"))
    assertEquals(resolved.objectName, "MyBuildInfo")
    assertEquals(resolved.includeGitHash, true)
    assertEquals(resolved.includeTimestamp, false) // not overridden, uses default
    assertEquals(resolved.extra, Map("myKey" -> "myValue"))
  }

  test("disabled module resolves enabled=false") {
    val defaults = new BuildInfo.ModuleDefaults(
      true,
      null,
      "BuildInfo",
      false,
      false,
      JMap.of()
    )
    val moduleOverride = new BuildInfo.ModuleOverride(
      false,
      null,
      null,
      null,
      null,
      null
    )
    val config = new BuildInfo.BuildInfoPluginConfig(defaults, JMap.of("my-module", moduleOverride))

    val resolved = BuildInfoConfigNormalizer.normalize(
      moduleId = "my-module",
      moduleType = "SCALA",
      scalaVersion = None,
      config = config
    )

    assertEquals(resolved.enabled, false)
  }

  test("extra values merge from override and defaults") {
    val defaults = new BuildInfo.ModuleDefaults(
      true,
      null,
      "BuildInfo",
      false,
      false,
      JMap.of("defaultKey", "defaultVal")
    )
    val moduleOverride = new BuildInfo.ModuleOverride(
      null,
      null,
      null,
      null,
      null,
      JMap.of("overrideKey", "overrideVal")
    )
    val config = new BuildInfo.BuildInfoPluginConfig(defaults, JMap.of("my-module", moduleOverride))

    val resolved = BuildInfoConfigNormalizer.normalize(
      moduleId = "my-module",
      moduleType = "SCALA",
      scalaVersion = None,
      config = config
    )

    assertEquals(resolved.extra, Map("overrideKey" -> "overrideVal"))
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
deder exec -t test -m deder-build-info-test ba.sake.deder.buildinfo.BuildInfoConfigNormalizerSuite
```

Expected: compilation or test failure (class not found).

- [ ] **Step 3: Write the normalizer implementation**

Create `deder-build-info/src/ba/sake/deder/buildinfo/BuildInfoConfigNormalizer.scala`:

```scala
package ba.sake.deder.buildinfo

import ba.sake.deder.BuildInfo
import scala.jdk.CollectionConverters.*

final case class ResolvedBuildInfoConfig(
    enabled: Boolean,
    packageName: Option[String],
    objectName: String,
    includeGitHash: Boolean,
    includeTimestamp: Boolean,
    extra: Map[String, String],
    scalaVersion: Option[String]
)

object BuildInfoConfigNormalizer {

  def normalize(
      moduleId: String,
      moduleType: String,
      scalaVersion: Option[String],
      config: BuildInfo.BuildInfoPluginConfig
  ): ResolvedBuildInfoConfig = {
    val defaults = config.defaults
    val overrideConfig = Option(config.modules.get(moduleId))

    val enabled = overrideConfig
      .flatMap(value => Option(value.enabled).map(_.booleanValue()))
      .getOrElse(defaults.enabled)

    val packageName = overrideConfig
      .flatMap(value => Option(value.packageName))
      .orElse(trimToOption(defaults.packageName))

    val objectName = overrideConfig
      .flatMap(value => Option(value.objectName))
      .getOrElse(defaults.objectName)

    val includeGitHash = overrideConfig
      .flatMap(value => Option(value.includeGitHash).map(_.booleanValue()))
      .getOrElse(defaults.includeGitHash)

    val includeTimestamp = overrideConfig
      .flatMap(value => Option(value.includeTimestamp).map(_.booleanValue()))
      .getOrElse(defaults.includeTimestamp)

    val extra = overrideConfig
      .flatMap(value => Option(value.extra).map(_.asScala.toMap))
      .getOrElse(defaults.extra.asScala.toMap)
      .view
      .mapValues(_.trim)
      .toMap
      .filter { case (key, value) => key.trim.nonEmpty && value.nonEmpty }

    ResolvedBuildInfoConfig(
      enabled = enabled,
      packageName = packageName,
      objectName = objectName,
      includeGitHash = includeGitHash,
      includeTimestamp = includeTimestamp,
      extra = extra,
      scalaVersion = scalaVersion
    )
  }

  private def trimToOption(value: String): Option[String] =
    Option(value).map(_.trim).filter(_.nonEmpty)
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
deder exec -t test -m deder-build-info-test ba.sake.deder.buildinfo.BuildInfoConfigNormalizerSuite
```

Expected: all 5 tests pass.

- [ ] **Step 5: Commit**

```bash
git add deder-build-info/src/ba/sake/deder/buildinfo/BuildInfoConfigNormalizer.scala \
        deder-build-info/test/src/ba/sake/deder/buildinfo/BuildInfoConfigNormalizerSuite.scala
git commit -m "feat: implement BuildInfo config normalizer with tests"
```

---

### Task 5: GitSupport

**Files:**
- Create: `deder-build-info/src/ba/sake/deder/buildinfo/GitSupport.scala`
- Create: `deder-build-info/test/src/ba/sake/deder/buildinfo/GitSupportSuite.scala`

- [ ] **Step 1: Write the test first**

```scala
package ba.sake.deder.buildinfo

import munit.FunSuite

class GitSupportSuite extends FunSuite {

  test("gitHead returns a non-empty string when in a git repo") {
    val result = GitSupport.gitHead(os.pwd)
    // The project itself is a git repo, so this should return a hash
    assert(result.nonEmpty, s"Expected non-empty git hash, got '$result'")
    assert(!result.startsWith("fatal:"), s"Expected git hash, got error: $result")
  }

  test("gitHead returns 'unknown' for non-existent directory") {
    val nonGitDir = os.temp.dir()
    val result = GitSupport.gitHead(nonGitDir)
    assertEquals(result, "unknown")
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
deder exec -t test -m deder-build-info-test ba.sake.deder.buildinfo.GitSupportSuite
```

- [ ] **Step 3: Write GitSupport implementation**

```scala
package ba.sake.deder.buildinfo

object GitSupport {

  def gitHead(repoRoot: os.Path): String =
    try {
      val result = os.proc("git", "rev-parse", "HEAD")
        .call(cwd = repoRoot, stderr = os.Pipe)
      result.out.trim()
    } catch {
      case _: Exception => "unknown"
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
deder exec -t test -m deder-build-info-test ba.sake.deder.buildinfo.GitSupportSuite
```

Expected: both tests pass.

- [ ] **Step 5: Commit**

```bash
git add deder-build-info/src/ba/sake/deder/buildinfo/GitSupport.scala \
        deder-build-info/test/src/ba/sake/deder/buildinfo/GitSupportSuite.scala
git commit -m "feat: implement GitSupport for git HEAD hash retrieval"
```

---

### Task 6: BuildInfoGenerationTasks (Source Generator)

**Files:**
- Create: `deder-build-info/src/ba/sake/deder/buildinfo/BuildInfoGenerationTasks.scala`
- Create: `deder-build-info/test/src/ba/sake/deder/buildinfo/BuildInfoGenerationTasksSuite.scala`

- [ ] **Step 1: Write the test first**

```scala
package ba.sake.deder.buildinfo

import ba.sake.deder.{*, given}
import ba.sake.deder.BuildInfo
import ba.sake.tupson.JsonRW
import munit.FunSuite

import java.util.{List as JList, Map as JMap}

class BuildInfoGenerationTasksSuite extends FunSuite {

  test("generateScalaSource produces all always-on fields") {
    val config = ResolvedBuildInfoConfig(
      enabled = true,
      packageName = Some("com.example"),
      objectName = "BuildInfo",
      includeGitHash = false,
      includeTimestamp = false,
      extra = Map.empty,
      scalaVersion = Some("3.7.4")
    )

    val source = BuildInfoGenerationTasks.generateScalaSource(
      moduleName = "my-app",
      config = config
    )

    assert(source.contains("package com.example"))
    assert(source.contains("object BuildInfo {"))
    assert(source.contains("val moduleName: String = \"my-app\""))
    assert(source.contains("val moduleVersion: String = \"0.1.0-SNAPSHOT\""))
    assert(source.contains("val scalaVersion: String = \"3.7.4\""))
    assert(source.contains("val javaVersion: String ="))
    assert(!source.contains("val gitHead"))
    assert(!source.contains("val builtAtMillis"))
    assert(source.contains("def toMap: Map[String, Any] ="))
  }

  test("generateScalaSource omits scalaVersion for Java modules") {
    val config = ResolvedBuildInfoConfig(
      enabled = true,
      packageName = None,
      objectName = "BuildInfo",
      includeGitHash = false,
      includeTimestamp = false,
      extra = Map.empty,
      scalaVersion = None
    )

    val source = BuildInfoGenerationTasks.generateScalaSource(
      moduleName = "my-app",
      config = config
    )

    assert(!source.contains("package "))
    assert(!source.contains("scalaVersion"))
    assert(source.contains("object BuildInfo {"))
  }

  test("generateScalaSource includes opt-in fields when enabled") {
    val config = ResolvedBuildInfoConfig(
      enabled = true,
      packageName = None,
      objectName = "BuildInfo",
      includeGitHash = true,
      includeTimestamp = true,
      extra = Map.empty,
      scalaVersion = None
    )

    val gitHash = "abc123def"
    val timestamp = 1711123200000L

    val source = BuildInfoGenerationTasks.generateScalaSource(
      moduleName = "my-app",
      config = config,
      gitHash = Some(gitHash),
      timestamp = Some(timestamp)
    )

    assert(source.contains(s"val gitHead: String = \"$gitHash\""))
    assert(source.contains(s"val builtAtMillis: Long = ${timestamp}L"))
    assert(source.contains(s""""gitHead" -> gitHead"""))
    assert(source.contains(s""""builtAtMillis" -> builtAtMillis"""))
  }

  test("generateScalaSource includes extra values alphabetically") {
    val config = ResolvedBuildInfoConfig(
      enabled = true,
      packageName = None,
      objectName = "BuildInfo",
      includeGitHash = false,
      includeTimestamp = false,
      extra = Map("zebra" -> "z", "alpha" -> "a"),
      scalaVersion = None
    )

    val source = BuildInfoGenerationTasks.generateScalaSource(
      moduleName = "my-app",
      config = config
    )

    assert(source.contains("val alpha: String = \"a\""))
    assert(source.contains("val zebra: String = \"z\""))

    // alpha should appear before zebra in the file
    val alphaIndex = source.indexOf("val alpha")
    val zebraIndex = source.indexOf("val zebra")
    assert(alphaIndex < zebraIndex, "extra values should be sorted alphabetically")
  }

  test("generateScalaSource toMap includes all active keys") {
    val config = ResolvedBuildInfoConfig(
      enabled = true,
      packageName = None,
      objectName = "BuildInfo",
      includeGitHash = true,
      includeTimestamp = true,
      extra = Map("custom" -> "val"),
      scalaVersion = Some("3.7.4")
    )

    val source = BuildInfoGenerationTasks.generateScalaSource(
      moduleName = "my-app",
      config = config,
      gitHash = Some("abc"),
      timestamp = Some(123L)
    )

    assert(source.contains("\"moduleName\" -> moduleName"))
    assert(source.contains("\"moduleVersion\" -> moduleVersion"))
    assert(source.contains("\"scalaVersion\" -> scalaVersion"))
    assert(source.contains("\"javaVersion\" -> javaVersion"))
    assert(source.contains("\"gitHead\" -> gitHead"))
    assert(source.contains("\"builtAtMillis\" -> builtAtMillis"))
    assert(source.contains("\"custom\" -> custom"))
  }

  test("task name is buildInfoGenerate") {
    val config = new BuildInfo.BuildInfoPluginConfig(
      new BuildInfo.ModuleDefaults(true, null, "BuildInfo", false, false, JMap.of()),
      JMap.of()
    )
    val task = BuildInfoGenerationTasks.sourceGeneratorTask(config)

    assertEquals(task.name, "buildInfoGenerate")
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
deder exec -t test -m deder-build-info-test ba.sake.deder.buildinfo.BuildInfoGenerationTasksSuite
```

- [ ] **Step 3: Write the task implementation**

```scala
package ba.sake.deder.buildinfo

import ba.sake.deder.{*, given}
import ba.sake.deder.config.DederProject.{DederModule, JavaModule, JavaTestModule, ScalaModule, ScalaTestModule}
import ba.sake.deder.config.DederProject.ModuleType
import ba.sake.deder.BuildInfo

object BuildInfoGenerationTasks {

  private val supportedModuleTypes: Set[ModuleType] =
    Set(ModuleType.JAVA, ModuleType.JAVA_TEST, ModuleType.SCALA, ModuleType.SCALA_TEST)

  def sourceGeneratorTask(
      config: BuildInfo.BuildInfoPluginConfig
  ): AbstractTask[os.Path] =
    CachedTaskBuilder
      .make[os.Path](
        name = "buildInfoGenerate",
        supportedModuleTypes = supportedModuleTypes,
        category = "BuildInfo",
        kind = TaskKind.SourceGenerator
      )
      .build { ctx =>
        val moduleName = ctx.module.id
        val (moduleType, scalaVersion) = extractModuleMeta(ctx.module)
        val resolved = BuildInfoConfigNormalizer.normalize(
          moduleId = moduleName,
          moduleType = moduleType,
          scalaVersion = scalaVersion,
          config = config
        )
        val sourceOut = ctx.out / "sources"
        prepareDirectory(sourceOut)

        if !resolved.enabled then sourceOut
        else {
          val gitHash = if resolved.includeGitHash then {
            val moduleRoot = os.Path(ctx.module.root)
            Some(GitSupport.gitHead(moduleRoot))
          } else None

          val timestamp = if resolved.includeTimestamp then Some(System.currentTimeMillis()) else None

          val sourceCode = generateScalaSource(
            moduleName = moduleName,
            config = resolved,
            gitHash = gitHash,
            timestamp = timestamp
          )

          val pkgPath = resolved.packageName match {
            case Some(pkg) => os.RelPath(pkg.replace('.', '/'))
            case None => os.RelPath(".")
          }
          val pkgDir = sourceOut / pkgPath
          os.makeDir.all(pkgDir)
          val sourceFile = pkgDir / s"${resolved.objectName}.scala"
          os.write(sourceFile, sourceCode)
          sourceOut
        }
      }

  private[buildinfo] def generateScalaSource(
      moduleName: String,
      config: ResolvedBuildInfoConfig,
      gitHash: Option[String] = None,
      timestamp: Option[Long] = None
  ): String = {
    val pkgDecl = config.packageName.map(pkg => s"package $pkg\n\n").getOrElse("")
    val header = "// Generated by deder-build-info. DO NOT EDIT.\n\n"

    val vals = collection.mutable.ListBuffer.empty[String]
    val toMapEntries = collection.mutable.ListBuffer.empty[String]

    def addVal(name: String, tpe: String, value: String): Unit = {
      vals += s"  val $name: $tpe = $value"
      toMapEntries += s""""$name" -> $name"""
    }

    addVal("moduleName", "String", s"\"$moduleName\"")
    addVal("moduleVersion", "String", "\"0.1.0-SNAPSHOT\"")
    config.scalaVersion.foreach(v => addVal("scalaVersion", "String", s"\"$v\""))
    addVal("javaVersion", "String", s"\"${System.getProperty("java.version")}\"")
    gitHash.foreach(h => addVal("gitHead", "String", s"\"$h\""))
    timestamp.foreach(t => addVal("builtAtMillis", "Long", s"${t}L"))

    config.extra.toSeq.sortBy(_._1).foreach { case (key, value) =>
      val escapedValue = value.replace("\\", "\\\\").replace("\"", "\\\"")
      addVal(key, "String", s"\"$escapedValue\"")
    }

    val toMapBody = toMapEntries.mkString(",\n      ")

    s"""$header${pkgDecl}object ${config.objectName} {
       |${vals.mkString("\n\n")}
       |
       |  def toMap: Map[String, Any] = Map(
       |      $toMapBody
       |    )
       |}
       |""".stripMargin
  }

  private def extractModuleMeta(module: DederModule): (String, Option[String]) =
    module match {
      case m: ScalaModule => ("SCALA", trimToOption(m.scalaVersion))
      case m: ScalaTestModule => ("SCALA", trimToOption(m.scalaVersion))
      case _: JavaModule => ("JAVA", None)
      case _: JavaTestModule => ("JAVA", None)
      case other => throw IllegalArgumentException(s"Unsupported module type: ${other.getClass.getName}")
    }

  private def trimToOption(value: String): Option[String] =
    Option(value).map(_.trim).filter(_.nonEmpty)

  private def prepareDirectory(path: os.Path): Unit = {
    if os.exists(path) then os.remove.all(path)
    os.makeDir.all(path)
  }
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
deder exec -t test -m deder-build-info-test ba.sake.deder.buildinfo.BuildInfoGenerationTasksSuite
```

Expected: all 6 tests pass.

- [ ] **Step 5: Commit**

```bash
git add deder-build-info/src/ba/sake/deder/buildinfo/BuildInfoGenerationTasks.scala \
        deder-build-info/test/src/ba/sake/deder/buildinfo/BuildInfoGenerationTasksSuite.scala
git commit -m "feat: implement BuildInfo source generator task with tests"
```

---

### Task 7: Plugin Entrypoint and ServiceLoader

**Files:**
- Create: `deder-build-info/src/ba/sake/deder/buildinfo/BuildInfoPluginImpl.scala`
- Create: `deder-build-info/resources/META-INF/services/ba.sake.deder.DederPluginApi`

- [ ] **Step 1: Write the plugin entrypoint**

```scala
package ba.sake.deder.buildinfo

import ba.sake.deder.*
import ba.sake.deder.BuildInfo

class BuildInfoPluginImpl extends DederPluginApi {
  override def id: String = "build-info"

  override def tasks(
      params: PluginTasksParams
  ): Either[String, Seq[AbstractTask[?]]] =
    try {
      val pluginModule = PluginConfigEvaluators.evaluate(
        pluginClassLoader = getClass.getClassLoader,
        modulePath = "BuildInfoPlugin.pkl",
        configText = params.configText,
        clazz = classOf[BuildInfo]
      )
      val config = pluginModule.config
      val generator = BuildInfoGenerationTasks.sourceGeneratorTask(config)
      Right(Seq(generator))
    } catch {
      case error: Exception =>
        Left(s"Failed to initialize build-info plugin config: ${error.getMessage}")
    }
}
```

- [ ] **Step 2: Write the ServiceLoader registration**

```bash
echo "ba.sake.deder.buildinfo.BuildInfoPluginImpl" > deder-build-info/resources/META-INF/services/ba.sake.deder.DederPluginApi
```

- [ ] **Step 3: Compile the plugin module**

```bash
deder exec -t compile -m deder-build-info
```

Expected: compiles successfully.

- [ ] **Step 4: Run the full test suite**

```bash
deder exec -t test -m deder-build-info-test
```

Expected: all tests pass.

- [ ] **Step 5: Commit**

```bash
git add deder-build-info/src/ba/sake/deder/buildinfo/BuildInfoPluginImpl.scala \
        deder-build-info/resources/META-INF/services/ba.sake.deder.DederPluginApi
git commit -m "feat: implement BuildInfo plugin entrypoint and ServiceLoader registration"
```

---

### Task 8: Consumer Example

**Files:**
- Create: `deder-build-info/examples/consumer/deder.pkl`
- Create: `deder-build-info/examples/consumer/src/consumer/Main.scala`

- [ ] **Step 1: Publish the plugin locally**

```bash
deder exec -t publishLocal -m deder-build-info
```

- [ ] **Step 2: Write the consumer example build file**

Following the exact pattern from `deder-protobuf/examples/consumer/deder.pkl`, using a top-level `import` (not `local import`) and `plugins` at project level:

Create `deder-build-info/examples/consumer/deder.pkl`:

```pkl
amends "https://sake92.github.io/deder/config/v0.9.1/DederProject.pkl"

import "../../resources/BuildInfoPlugin.pkl" as BI

plugins {
  new BI.BuildInfoPlugin {
    config = new BI.BuildInfoPluginConfig {
      defaults = new BI.ModuleDefaults {
        packageName = "consumer"
        includeGitHash = true
        includeTimestamp = true
      }
    }
  }
}

modules {
  new ScalaModule {
    id = "example"
    root = "."
    scalaVersion = "3.7.4"
    mainClass = "consumer.Main"
  }
}
```

- [ ] **Step 3: Write the consumer Scala source**

Create `deder-build-info/examples/consumer/src/consumer/Main.scala`:

```scala
package consumer

@main def main(): Unit =
  println(s"Module: ${BuildInfo.moduleName}")
  println(s"Version: ${BuildInfo.moduleVersion}")
  println(s"Scala: ${BuildInfo.scalaVersion}")
  println(s"Java: ${BuildInfo.javaVersion}")
  println(s"Git: ${BuildInfo.gitHead}")
  println(s"Built at: ${BuildInfo.builtAtMillis}")
  BuildInfo.toMap.foreach { case (k, v) =>
    println(s"  $k = $v")
  }
```

- [ ] **Step 4: Verify the consumer example compiles**

```bash
cd deder-build-info/examples/consumer && deder exec -t compile -m example
```

Expected: compiles successfully. The `BuildInfo` object is generated as part of source generation.

- [ ] **Step 5: Commit**

```bash
git add deder-build-info/examples/consumer/
git commit -m "feat: add consumer example for BuildInfo plugin"
```

---

### Task 9: Final Integration Verification

**Files:**
- None new — verification only.

- [ ] **Step 1: Compile both plugin modules from repo root**

```bash
deder exec -t compile -m deder-protobuf
deder exec -t compile -m deder-build-info
```

Expected: both compile successfully without affecting each other.

- [ ] **Step 2: Run both test suites**

```bash
deder exec -t test -m deder-protobuf-test
deder exec -t test -m deder-build-info-test
```

Expected: all tests pass for both plugins.

- [ ] **Step 3: Verify build info test coverage**

```bash
deder exec -t testClasses -m deder-build-info-test --json
```

Expected: lists `BuildInfoConfigNormalizerSuite`, `BuildInfoGenerationTasksSuite`, `GitSupportSuite`.

- [ ] **Step 4: Publish both plugins locally**

```bash
deder exec -t publishLocal -m deder-protobuf
deder exec -t publishLocal -m deder-build-info
```

Expected: both publish successfully.

---
