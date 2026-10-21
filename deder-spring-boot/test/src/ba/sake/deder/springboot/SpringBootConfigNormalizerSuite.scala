package ba.sake.deder.springboot

import ba.sake.deder.plugins.Springboot
import munit.FunSuite

import java.util.{List as JList, Map as JMap}

class SpringBootConfigNormalizerSuite extends FunSuite {

  private val defaults = new Springboot.ModuleDefaults(true, "3.5.6", null, JList.of())

  test("defaults resolve to expected values") {
    val config = new Springboot.SpringBootPluginConfig(defaults, JMap.of())
    val resolved = SpringBootConfigNormalizer.normalize("app", config)
    assertEquals(resolved, ResolvedSpringBootConfig(true, "3.5.6", None, Seq.empty))
  }

  test("module override shadows defaults") {
    val over = new Springboot.ModuleOverride(false, "3.4.0", " com.example.App ", JList.of("-Xmx1g", " "))
    val config = new Springboot.SpringBootPluginConfig(defaults, JMap.of("app", over))
    val resolved = SpringBootConfigNormalizer.normalize("app", config)
    assertEquals(resolved, ResolvedSpringBootConfig(false, "3.4.0", Some("com.example.App"), Seq("-Xmx1g")))
    // other modules keep defaults
    assertEquals(SpringBootConfigNormalizer.normalize("other", config).enabled, true)
  }

  test("null override fields fall back to defaults") {
    val d = new Springboot.ModuleDefaults(true, "3.5.6", "a.Main", JList.of("-Dx=1"))
    val over = new Springboot.ModuleOverride(null, null, null, null)
    val config = new Springboot.SpringBootPluginConfig(d, JMap.of("app", over))
    val resolved = SpringBootConfigNormalizer.normalize("app", config)
    assertEquals(resolved, ResolvedSpringBootConfig(true, "3.5.6", Some("a.Main"), Seq("-Dx=1")))
  }
}
