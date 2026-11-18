package ba.sake.deder.owasp

import ba.sake.deder.plugins.Owasp
import java.util.{List as JList, Map as JMap}

class OwaspTaskSuite extends munit.FunSuite {
  private def defaults = new Owasp.ModuleDefaults(true, "12.2.2", JList.of("HTML", "JSON"), null, JList.of(), ".deder/data", JList.of())
  private def empty = new Owasp.ModuleOverride(null, null, null, null, null, null, null)

  test("module overrides inherit defaults and can configure a threshold") {
    val overrideConfig = empty.withFailOnCvss(7.0).withFormats(JList.of("SARIF"))
    val config = new Owasp.OwaspPluginConfig(defaults, JMap.of("app", overrideConfig))
    val result = OwaspTask.normalize("app", config)
    assertEquals(result.threshold, Some(7.0))
    assertEquals(result.formats, Seq("SARIF"))
    assertEquals(result.version, "12.2.2")
    assertEquals(OwaspTask.normalize("other", config).threshold, None)
  }

  test("rejects invalid thresholds and options owned by the plugin") {
    val badThreshold = new Owasp.OwaspPluginConfig(defaults.withFailOnCvss(11.0), JMap.of())
    intercept[IllegalArgumentException](OwaspTask.normalize("app", badThreshold))
    val badArgs = new Owasp.OwaspPluginConfig(defaults.withExtraArgs(JList.of("--out=/tmp")), JMap.of())
    intercept[IllegalArgumentException](OwaspTask.normalize("app", badArgs))
  }

  test("scan arguments include each unique jar, formats and threshold") {
    val settings = ScanConfig(true, "12.2.2", Seq("HTML", "JSON"), Some(7.0), Seq.empty, "data", Seq("--noupdate"))
    val jar = os.Path("/tmp/example.jar")
    val args = OwaspTask.arguments(settings, "app", Seq(jar, jar), os.Path("/tmp/reports"), os.Path("/tmp/data"))
    assertEquals(args.count(_ == "--scan"), 1)
    assertEquals(args.count(_ == "--format"), 2)
    assert(args.contains("7.0"))
    assert(args.contains("--noupdate"))
  }
}
