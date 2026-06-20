package ba.sake.deder.webdashboard.server

import java.time.Instant
import munit.FunSuite

class TaskExecutionLogSuite extends FunSuite {

  private def now: Instant = Instant.now()

  private def makeEntry(execId: String, taskName: String, status: ExecStatus): ExecEntry =
    ExecEntry(
      execId = execId,
      taskName = taskName,
      moduleIds = Seq.empty,
      startTime = now,
      endTime = None,
      status = status,
      output = "",
      outcomes = Seq.empty,
      error = None,
      requestId = None
    )

  test("add and get entry by execId") {
    val log = TaskExecutionLog(10)
    val entry = makeEntry("abc", "compile", ExecStatus.RUNNING)
    log.add(entry)
    val found = log.get("abc")
    assert(found.isDefined, s"should find entry by execId")
    assertEquals(found.get.execId, "abc")
    assertEquals(found.get.taskName, "compile")
    assertEquals(found.get.status, ExecStatus.RUNNING)
  }

  test("get returns None for unknown execId") {
    val log = TaskExecutionLog(10)
    assert(log.get("nonexistent").isEmpty)
  }

  test("recent returns most recent first") {
    val log = TaskExecutionLog(10)
    log.add(makeEntry("a", "compile", ExecStatus.SUCCESS))
    log.add(makeEntry("b", "test", ExecStatus.SUCCESS))
    log.add(makeEntry("c", "jar", ExecStatus.SUCCESS))
    val recents = log.recent()
    assertEquals(recents.size, 3)
    assertEquals(recents.head.execId, "c")
    assertEquals(recents.last.execId, "a")
  }

  test("recent with limit returns limited entries") {
    val log = TaskExecutionLog(10)
    log.add(makeEntry("a", "compile", ExecStatus.SUCCESS))
    log.add(makeEntry("b", "test", ExecStatus.SUCCESS))
    log.add(makeEntry("c", "jar", ExecStatus.SUCCESS))
    assertEquals(log.recent(2).size, 2)
  }

  test("evicts oldest entries when exceeding maxEntries") {
    val log = TaskExecutionLog(2)
    log.add(makeEntry("a", "compile", ExecStatus.SUCCESS))
    log.add(makeEntry("b", "test", ExecStatus.SUCCESS))
    log.add(makeEntry("c", "jar", ExecStatus.SUCCESS))
    assertEquals(log.recent().size, 2)
    assert(log.get("a").isEmpty, s"oldest entry 'a' should be evicted")
    assert(log.get("b").isDefined, s"entry 'b' should remain")
    assert(log.get("c").isDefined, s"entry 'c' should remain")
  }

  test("update modifies existing entry") {
    val log = TaskExecutionLog(10)
    val entry = makeEntry("abc", "compile", ExecStatus.RUNNING)
    log.add(entry)
    log.update("abc")(_.copy(status = ExecStatus.SUCCESS, endTime = Some(now.plusSeconds(5))))
    val updated = log.get("abc")
    assertEquals(updated.get.status, ExecStatus.SUCCESS)
    assert(updated.get.endTime.isDefined)
  }

  test("update non-existent entry is safe no-op") {
    val log = TaskExecutionLog(10)
    log.update("nonexistent")(_.copy(status = ExecStatus.SUCCESS))
    assert(log.get("nonexistent").isEmpty)
  }

  test("concurrent adds do not lose data") {
    val log = TaskExecutionLog(500)
    val threads = (1 to 5).map { g =>
      new Thread(() => {
        for i <- 1 to 100 do
          val id = s"${g}-${i}"
          log.add(makeEntry(id, "compile", ExecStatus.SUCCESS))
      })
    }
    threads.foreach(_.start())
    threads.foreach(_.join())
    assertEquals(log.recent(500).size, 500)
  }
}
