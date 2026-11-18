---
title: JMH
description: Run Java and Scala microbenchmarks
pagination:
  enabled: false
layout: page.html
---

# JMH

Runs Java and Scala microbenchmarks with [JMH](https://openjdk.org/projects/code-tools/jmh/). Enable the plugin for a dedicated benchmark module and add `org.openjdk.jmh:jmh-core` at the same version as `jmhVersion` to that module's dependencies.

```pkl
import "https://sake92.github.io/deder-plugins/config/deder-jmh/early-access/JmhPlugin.pkl" as Jmh

plugins {
  new Jmh.JmhPlugin {
    config { modules { ["benchmarks"] = new Jmh.ModuleOverride { enabled = true } } }
  }
}
```

```sh
deder exec -t jmhList -m benchmarks
deder exec -t jmhRun -m benchmarks -- -wi 3 -i 3 -f 1 '.*ExampleBenchmark.*'
```

Arguments after `--` are passed to JMH. Generation follows module compilation and rebuilds the benchmark harness for each invocation. The runner uses the project's working directory and selected JDK. Avoid other workloads during measurement; Deder does not guarantee exclusive benchmark execution.

## Configuration

Fields in `defaults` apply to all supported modules; fields in `modules["module-id"]` override them for one module.

| Field | Default | Purpose |
|---|---|---|
| `enabled` | `false` | Enable benchmark tasks. |
| `jmhVersion` | `"1.37"` | JMH version. |
| `jvmOptions` | empty | JVM options for the generator and JMH launcher. JMH fork options can be passed as JMH arguments or annotations. |

## Schema

- [early-access](../config/deder-jmh/early-access/JmhPlugin.pkl)
