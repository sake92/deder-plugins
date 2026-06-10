package ba.sake.deder.tuidashboard.client

import ba.sake.deder.tuidashboard.model.*
import ba.sake.tupson.*

import munit.FunSuite

class DashboardClientSuite extends FunSuite {

  test("decode modules JSON") {
    val json = """[{"id":"foo","type":"SCALA","deps":3},{"id":"bar","type":"JAVA","deps":1}]"""
    val result = json.parseJson[Seq[ApiModule]]
    assertEquals(result.length, 2)
    assertEquals(result(0).id, "foo")
    assertEquals(result(0).`type`, "SCALA")
    assertEquals(result(0).deps, 3)
  }

  test("decode stats overview JSON") {
    val json = """{"totalRequestsServed":150,"totalErrors":2,"uptimeSecs":3600}"""
    val result = json.parseJson[StatsOverview]
    assertEquals(result.totalRequestsServed, 150L)
    assertEquals(result.totalErrors, 2L)
    assertEquals(result.uptimeSecs, 3600L)
  }

  test("decode current requests JSON") {
    val json = """[{"requestId":"req-001","taskName":"compile","moduleIds":["foo","bar"],"startTimeMs":1700000000000}]"""
    val result = json.parseJson[Seq[ApiCurrentRequest]]
    assertEquals(result.length, 1)
    assertEquals(result(0).requestId, "req-001")
    assertEquals(result(0).taskName, "compile")
    assertEquals(result(0).moduleIds, Seq("foo", "bar"))
    assertEquals(result(0).startTimeMs, 1700000000000L)
  }

  test("decode history JSON") {
    val json = """[{"requestId":"req-000","taskName":"compile","moduleIds":["foo"],"startTimeMs":1700000000000,"durationMs":4500,"success":true}]"""
    val result = json.parseJson[Seq[ApiHistoryEntry]]
    assertEquals(result.length, 1)
    assertEquals(result(0).requestId, "req-000")
    assertEquals(result(0).taskName, "compile")
    assertEquals(result(0).moduleIds, Seq("foo"))
    assertEquals(result(0).durationMs, 4500L)
    assertEquals(result(0).success, true)
  }

  test("decode history entry with success=false") {
    val json = """[{"requestId":"req-fail","taskName":"test","moduleIds":[],"startTimeMs":0,"durationMs":100,"success":false}]"""
    val result = json.parseJson[Seq[ApiHistoryEntry]]
    assertEquals(result.length, 1)
    assertEquals(result(0).success, false)
  }

  test("parse error for invalid JSON") {
    val json = """not json at all"""
    try {
      json.parseJson[Seq[ApiModule]]
      assert(false, "expected exception")
    } catch {
      case _: Exception => () // expected
    }
  }

  test("parse error for wrong shape JSON") {
    val json = """{"wrong":"shape"}"""
    try {
      json.parseJson[Seq[ApiModule]]
      assert(false, "expected exception")
    } catch {
      case _: Exception => () // expected
    }
  }

  test("empty modules array") {
    val json = """[]"""
    val result = json.parseJson[Seq[ApiModule]]
    assertEquals(result.length, 0)
  }
}
