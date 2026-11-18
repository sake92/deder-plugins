package example;

import org.openjdk.jmh.annotations.Benchmark;

public class JavaBenchmark {
  @Benchmark public int addition() { return 1 + 1; }
}
