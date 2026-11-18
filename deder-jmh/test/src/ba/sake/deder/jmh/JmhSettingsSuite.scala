package ba.sake.deder.jmh

import ba.sake.deder.PluginConfigEvaluators
import ba.sake.deder.plugins.Jmh
import java.util.{List as JList, Map as JMap}

class JmhSettingsSuite extends munit.FunSuite {
  test("module settings override defaults independently") {
    val defaults = new Jmh.ModuleDefaults(false, "1.37", JList.of("-Xmx1g"))
    val overrides = new Jmh.ModuleOverride(true, null, JList.of())
    val config = new Jmh.JmhPluginConfig(defaults, JMap.of("bench", overrides))
    assertEquals(JmhSettings.forModule("bench", config), JmhSettings(true, "1.37", Seq.empty))
    assertEquals(JmhSettings.forModule("other", config), JmhSettings(false, "1.37", Seq("-Xmx1g")))
  }

  test("serialized module overrides are evaluated as typed overrides") {
    val text = """config {
      |  modules {
      |    ["bench"] { enabled = true; jmhVersion = null; jvmOptions = null }
      |  }
      |}""".stripMargin
    val config = PluginConfigEvaluators.evaluate(getClass.getClassLoader, "JmhPlugin.pkl", text, classOf[Jmh]).config
    assertEquals(JmhSettings.forModule("bench", config), JmhSettings(true, "1.37", Seq.empty))
  }
}
