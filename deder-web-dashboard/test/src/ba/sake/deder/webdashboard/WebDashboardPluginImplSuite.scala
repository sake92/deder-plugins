package ba.sake.deder.webdashboard

import ba.sake.deder.*
import munit.FunSuite

class WebDashboardPluginImplSuite extends FunSuite {
  test("plugin id is web-dashboard") {
    val plugin = new WebDashboardPluginImpl
    assertEquals(plugin.id, "web-dashboard")
  }

  test("onClose with no server does not throw") {
    val plugin = new WebDashboardPluginImpl
    plugin.onClose()
  }
}
