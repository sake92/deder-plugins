---
title: OWASP Dependency-Check
description: Scan runtime dependencies for known vulnerabilities
pagination:
  enabled: false
layout: page.html
---

# OWASP Dependency-Check

Scans runtime dependency JARs of Java and Scala modules using [OWASP Dependency-Check](https://dependency-check.github.io/DependencyCheck/). Test modules and aggregate reports are not supported.

```pkl
import "https://sake92.github.io/deder-plugins/config/deder-owasp-dependency-check/early-access/OwaspPlugin.pkl" as OWASP

plugins {
  new OWASP.OwaspPlugin {
    config {
      defaults {
        failOnCvss = 7.0
        suppressionFiles { "dependency-check-suppressions.xml" }
      }
    }
  }
}
```

Run `deder exec -t owaspDependencyCheck -m <module-id>`. The task runs on every invocation and prints the report directory on completion or failure. Scanner failures always fail the task.

Set `NVD_API_KEY` to speed up vulnerability data updates. The key is passed to the CLI as a command-line argument and may be visible in local process listings. Initial updates can take a long time; CI jobs sharing a key may encounter NVD rate limits.

## Configuration

Set fields in `defaults` or override them in `modules["module-id"]`. Relative paths resolve against the project root.

| Field | Default | Purpose |
|---|---|---|
| `enabled` | `true` | Enable scanning. |
| `dependencyCheckVersion` | `"12.2.2"` | Dependency-Check CLI version. |
| `formats` | `HTML`, `JSON` | Report formats. |
| `failOnCvss` | `null` | Fail on findings at or above this score (0–10); null disables the threshold. |
| `suppressionFiles` | empty | Suppression file paths. |
| `dataDirectory` | `".deder/owasp-dependency-check-data"` | Parent directory for version-specific vulnerability databases. |
| `extraArgs` | empty | Additional CLI arguments; plugin-managed options cannot be overridden. |

Scans sharing a data directory are serialized with a file lock.

## Schema

- [early-access](../config/deder-owasp-dependency-check/early-access/OwaspPlugin.pkl)
