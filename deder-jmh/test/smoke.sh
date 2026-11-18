#!/usr/bin/env bash
set -euo pipefail

plugin_dir="$(cd "$(dirname "$0")/.." && pwd)"
repo_dir="$(cd "$plugin_dir/.." && pwd)"
fixture="$(mktemp -d "$plugin_dir/examples/.jmh-smoke.XXXXXX")"
cleanup() {
  (cd "$fixture" && deder shutdown >/dev/null 2>&1) || true
  rm -rf "$fixture"
}
trap cleanup EXIT
cp -R "$plugin_dir/examples/consumer/deder.pkl" "$plugin_dir/examples/consumer/src" "$fixture/"
(cd "$repo_dir" && deder exec -t publishLocal -m deder-jmh)
cd "$fixture"
listed="$(deder exec -t jmhList -m benchmarks)"
[[ "$listed" == *"example.ExampleBenchmark.addition"* ]]
[[ "$listed" == *"example.JavaBenchmark.addition"* ]]
run="$(deder exec -t jmhRun -m benchmarks -- -wi 0 -i 1 -r 100ms -f 1 '.*ExampleBenchmark.*')"
[[ "$run" == *"Result \"example.ExampleBenchmark.addition\""* ]]
if deder exec -t jmhRun -m benchmarks -- -invalid-jmh-option; then
  echo "Invalid JMH option unexpectedly succeeded" >&2
  exit 1
fi
rm src/example/JavaBenchmark.java
listed="$(deder exec -t jmhList -m benchmarks)"
[[ "$listed" == *"example.ExampleBenchmark.addition"* ]]
[[ "$listed" != *"example.JavaBenchmark.addition"* ]]
echo "JMH smoke checks passed"
