package example

import org.openjdk.jmh.annotations.*

@State(Scope.Thread)
class ExampleBenchmark {
  @Benchmark def addition(): Int = 1 + 1
}
