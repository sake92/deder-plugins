package ba.sake.deder.webdashboard.server

import java.time.{Duration, Instant}
import ba.sake.deder.*
import ba.sake.deder.config.DederProject
import ba.sake.tupson.*
import scala.jdk.CollectionConverters.*

object ApiRoutes {

  case class ApiModule(id: String, `type`: String, deps: Int) derives JsonRW
  case class StatsOverview(totalRequestsServed: Long, totalErrors: Long, uptimeSecs: Long) derives JsonRW
  case class ApiCurrentRequest(requestId: String, taskName: String, moduleIds: Seq[String], startTimeMs: Long) derives JsonRW
  case class ApiHistoryEntry(requestId: String, taskName: String, moduleIds: Seq[String], startTimeMs: Long, durationMs: Long, success: Boolean) derives JsonRW

  def modulesJson(project: DederProject): String = {
    val modules = project.modules.asScala.toSeq.map { m =>
      val moduleType = Option(m.`type`).map(_.name()).getOrElse("UNKNOWN")
      ApiModule(m.id, moduleType, m.moduleDeps.size())
    }
    modules.toJson
  }

  def overviewJson(internals: DederProjectInternals): String = {
    val uptimeSecs = internals.serverUptime.getSeconds
    StatsOverview(
      totalRequestsServed = internals.totalRequestsServed,
      totalErrors = internals.totalErrors,
      uptimeSecs = uptimeSecs
    ).toJson
  }

  def currentRequestsJson(internals: DederProjectInternals): String = {
    val requests = internals.currentRequests.map { r =>
      ApiCurrentRequest(
        requestId = r.requestId,
        taskName = r.taskName,
        moduleIds = r.moduleIds,
        startTimeMs = r.startTime.toEpochMilli
      )
    }
    requests.toJson
  }

  def historyJson(internals: DederProjectInternals): String = {
    val entries = internals.recentHistory.map { r =>
      ApiHistoryEntry(
        requestId = r.requestId,
        taskName = r.taskName,
        moduleIds = r.moduleIds,
        startTimeMs = r.startTime.toEpochMilli,
        durationMs = r.duration.toMillis,
        success = r.success
      )
    }
    entries.toJson
  }
}
