# Deder OWASP Dependency-Check plugin

Scans the runtime dependency JARs of Java and Scala modules for known vulnerabilities using [OWASP Dependency-Check](https://dependency-check.github.io/DependencyCheck/). The scan runs every time; vulnerability data can change even when dependencies do not.

```pkl
import "resources/OwaspPlugin.pkl" as OWASP

plugins {
  new OWASP.OwaspPlugin {
    config = new {
      defaults = new {
        failOnCvss = 7.0
        suppressionFiles { "dependency-check-suppressions.xml" }
      }
    }
  }
}
```

Run `deder exec -t owaspDependencyCheck -m <module-id>`. Reports are written to the task output's `reports` directory; the path is displayed on completion or failure. By default it creates HTML and JSON reports and does not fail on vulnerabilities. Set `failOnCvss` to a score from 0 to 10 to fail when a finding meets or exceeds it. Scanner failures always fail the task.

Set `NVD_API_KEY` in the environment to speed up updates. The CLI receives this key as a command-line argument, which may be visible to other local users in process listings. The first update can take a long time, and CI jobs sharing a key may hit NVD rate limits. Dependency-Check may contact other services as well; see its documentation for offline and mirror settings.

`defaults` and `modules["module-id"]` support `enabled`, `dependencyCheckVersion`, `formats`, `failOnCvss`, `suppressionFiles`, `dataDirectory`, and `extraArgs`. Relative paths are resolved against the project root. The database is stored under `dataDirectory/<tool-version>` and shared scans are serialized with a file lock. Extra arguments are passed to the CLI; scan inputs, report location, data directory, formats, threshold, project name and NVD key are managed by the plugin. Test modules and aggregate reports are not currently supported.
