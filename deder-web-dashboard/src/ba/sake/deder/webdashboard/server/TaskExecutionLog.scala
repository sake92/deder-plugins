package ba.sake.deder.webdashboard.server

import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue
import scala.jdk.CollectionConverters.*

enum ExecStatus:
  case PENDING, RUNNING, SUCCESS, FAILURE, CANCELLED

/** Per-module outcome of a web-triggered task execution. */
case class ExecModuleOutcome(
    moduleId: String,
    success: Boolean,
    error: Option[String],
    fromCache: Boolean
)

case class ExecEntry(
    execId: String,
    taskName: String,
    moduleIds: Seq[String],
    startTime: Instant,
    endTime: Option[Instant],
    status: ExecStatus,
    output: String,
    outcomes: Seq[ExecModuleOutcome],
    renderedSummary: Option[String],
    error: Option[String],
    requestId: Option[String]
)

class TaskExecutionLog(maxEntries: Int):
  private val lock = new Object()
  // TODO no need for ConcurrentLinkedQueue if we always synchronize on lock?
  private val entries = new ConcurrentLinkedQueue[ExecEntry]()

  def add(entry: ExecEntry): Unit = lock.synchronized {
    entries.add(entry)
    // remove oldest entries if we exceed maxEntries
    while entries.size() > maxEntries do entries.poll()
  }

  def get(execId: String): Option[ExecEntry] =
    entries.asScala.find(_.execId == execId)

  def recent(limit: Int = 50): Seq[ExecEntry] =
    entries.asScala.toSeq.reverse.take(limit)

  def update(execId: String)(fn: ExecEntry => ExecEntry): Unit = lock.synchronized {
    entries.asScala.find(_.execId == execId).foreach { old =>
      val updated = fn(old)
      entries.remove(old)
      entries.add(updated)
    }
  }
