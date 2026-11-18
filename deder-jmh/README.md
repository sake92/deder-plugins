# Deder JMH plugin

Run Java and Scala microbenchmarks with [JMH](https://openjdk.org/projects/code-tools/jmh/). Put benchmarks in a dedicated JVM module and add `org.openjdk.jmh:jmh-core` to that module's dependencies, using the same version as the plugin's `jmhVersion` (default `1.37`). Enable the plugin for the benchmark module; see [the consumer example](examples/consumer/deder.pkl).

```sh
deder exec -t jmhList -m benchmarks
deder exec -t jmhRun -m benchmarks -- -wi 3 -i 3 -f 1 '.*ExampleBenchmark.*'
deder exec -t jmhRun -m benchmarks -- -h
```

Arguments after `--` are passed to JMH. Use JMH's own options for filtering, profiling, result files, and fork JVM options. Plugin `jvmOptions` apply to the generator and JMH launcher; JMH fork options can be specified with JMH arguments or annotations. Generation runs after module compilation, scans that module's classes, and rebuilds the generated harness for each invocation. The benchmark runner uses the project's working directory and selected JDK. Deder does not currently guarantee exclusive benchmark execution, so avoid running other workloads during measurement.

For local development, publish this plugin with `deder exec -t publishLocal -m deder-jmh` before running the example.
