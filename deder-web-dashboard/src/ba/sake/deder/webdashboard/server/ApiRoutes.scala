package ba.sake.deder.webdashboard.server

import java.time.{Duration, Instant}
import ba.sake.deder.*
import ba.sake.deder.config.DederProject
import ba.sake.tupson.*
import scala.jdk.CollectionConverters.*

object ApiRoutes {

  // --- existing data types ---
  case class ApiModule(id: String, `type`: String, deps: Int) derives JsonRW
  case class StatsOverview(totalRequestsServed: Long, totalErrors: Long, uptimeSecs: Long) derives JsonRW
  case class ApiCurrentRequest(requestId: String, taskName: String, moduleIds: Seq[String], startTimeMs: Long) derives JsonRW
  case class ApiHistoryEntry(requestId: String, taskName: String, moduleIds: Seq[String], startTimeMs: Long, durationMs: Long, success: Boolean) derives JsonRW

  // --- new data types ---
  case class ApiTaskAggregate(
    taskName: String,
    invocations: Long,
    errors: Long,
    totalTimeMs: Long,
    avgTimeMs: Long,
    minTimeMs: Long,
    maxTimeMs: Long,
    longestModuleId: String
  ) derives JsonRW

  case class ApiModuleAggregate(
    moduleId: String,
    invocations: Long,
    errors: Long,
    totalTimeMs: Long,
    avgTimeMs: Long,
    minTimeMs: Long,
    maxTimeMs: Long
  ) derives JsonRW

  case class ApiErrorSummaryEntry(
    taskName: String,
    moduleIds: Seq[String],
    errorCount: Long
  ) derives JsonRW

  case class ApiPluginInfo(id: String, taskCount: Int, taskNames: Seq[String]) derives JsonRW

  case class ApiServerInfo(
    dederVersion: String,
    jdkVersion: String,
    jdkVendor: String,
    osName: String,
    osArch: String,
    processors: Int,
    maxHeapMB: Long,
    usedHeapMB: Long,
    uptimeSecs: Long,
    moduleCount: Int,
    pluginCount: Int,
    projectRoot: String,
    plugins: Seq[ApiPluginInfo]
  ) derives JsonRW

  // --- existing JSON endpoints ---
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

  // --- new aggregation logic ---

  /** Per-task aggregate stats computed from recentHistory. */
  def taskAggregates(internals: DederProjectInternals): Seq[ApiTaskAggregate] = {
    val grouped = internals.recentHistory.groupBy(_.taskName)
    grouped.toSeq.map { case (taskName, requests) =>
      val invocations = requests.size.toLong
      val errors = requests.count(!_.success).toLong
      val durations = requests.map(_.duration.toMillis)
      val totalTimeMs = durations.sum
      val avgTimeMs = if invocations > 0 then totalTimeMs / invocations else 0L
      val minTimeMs = if durations.nonEmpty then durations.min else 0L
      val maxTimeMs = if durations.nonEmpty then durations.max else 0L
      // find module that accumulated the most total time for this task
      val longestModuleId = moduleTotalTimes(requests).maxByOption(_._2).map(_._1).getOrElse("-")
      ApiTaskAggregate(taskName, invocations, errors, totalTimeMs, avgTimeMs, minTimeMs, maxTimeMs, longestModuleId)
    }.sortBy(_.taskName)
  }

  /** Per-module breakdown for a specific task. */
  def moduleBreakdown(internals: DederProjectInternals, taskName: String): Seq[ApiModuleAggregate] = {
    val requests = internals.recentHistory.filter(_.taskName == taskName)
    val moduleTimes = moduleTotalTimes(requests)
    moduleTimes.toSeq.map { case (moduleId, totalTimeMs) =>
      val moduleReqs = requests.filter(_.moduleIds.contains(moduleId))
      val invocations = moduleReqs.size.toLong
      val errors = moduleReqs.count(!_.success).toLong
      val durations = moduleReqs.map(_.duration.toMillis)
      val avgTimeMs = if invocations > 0 then totalTimeMs / invocations else 0L
      val minTimeMs = if durations.nonEmpty then durations.min else 0L
      val maxTimeMs = if durations.nonEmpty then durations.max else 0L
      ApiModuleAggregate(moduleId, invocations, errors, totalTimeMs, avgTimeMs, minTimeMs, maxTimeMs)
    }.sortBy(_.moduleId)
  }

  private def moduleTotalTimes(requests: Seq[CompletedRequest]): Map[String, Long] = {
    val pairs = for {
      req <- requests
      mod <- req.moduleIds
    } yield (mod, req.duration.toMillis)
    pairs.groupBy(_._1).view.mapValues(_.map(_._2).sum).toMap
  }

  /** Top N tasks by total accumulated time, descending. */
  def topOffenders(internals: DederProjectInternals, n: Int): Seq[ApiTaskAggregate] =
    taskAggregates(internals).sortBy(-_.totalTimeMs).take(n)

  /** Error summary grouped by (taskName, moduleIds). */
  def errorSummary(internals: DederProjectInternals): Seq[ApiErrorSummaryEntry] = {
    val failures = internals.recentHistory.filter(!_.success)
    failures.groupBy(r => (r.taskName, r.moduleIds.sorted)).toSeq.map { case ((taskName, moduleIds), reqs) =>
      ApiErrorSummaryEntry(taskName, moduleIds, reqs.size.toLong)
    }.sortBy(e => (-e.errorCount, e.taskName))
  }

  // --- new JSON endpoints ---

  def taskAggregatesJson(internals: DederProjectInternals): String =
    taskAggregates(internals).toJson

  def moduleBreakdownJson(internals: DederProjectInternals, taskName: String): String =
    moduleBreakdown(internals, taskName).toJson

  def topOffendersJson(internals: DederProjectInternals, n: Int): String =
    topOffenders(internals, n).toJson

  def errorSummaryJson(internals: DederProjectInternals): String =
    errorSummary(internals).toJson

  def serverInfoJson(internals: DederProjectInternals, project: DederProject): String = {
    val jdkVersion = System.getProperty("java.version")
    val jdkVendor = System.getProperty("java.vendor")
    val osName = System.getProperty("os.name")
    val osArch = System.getProperty("os.arch")
    val processors = Runtime.getRuntime().availableProcessors()
    val maxHeapMB = Runtime.getRuntime().maxMemory() / (1024 * 1024)
    val usedHeapMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
    val dederVersion = DederGlobals.version
    val uptimeSecs = internals.serverUptime.getSeconds
    val moduleCount = project.modules.size()
    val projectRoot = DederGlobals.projectRootDir.toString
    val plugins = internals.loadedPlugins.map { p =>
      ApiPluginInfo(p.id, p.taskNames.size, p.taskNames)
    }
    ApiServerInfo(
      dederVersion = dederVersion,
      jdkVersion = jdkVersion,
      jdkVendor = jdkVendor,
      osName = osName,
      osArch = osArch,
      processors = processors,
      maxHeapMB = maxHeapMB,
      usedHeapMB = usedHeapMB,
      uptimeSecs = uptimeSecs,
      moduleCount = moduleCount,
      pluginCount = plugins.size,
      projectRoot = projectRoot,
      plugins = plugins
    ).toJson
  }

  // --- history filtering ---

  /** Filter and paginate recent history, returning a page of ApiHistoryEntry. */
  def filteredHistory(
    internals: DederProjectInternals,
    search: String,
    caller: String,
    status: String,
    sort: String,
    limit: Int,
    offset: Int
  ): Seq[ApiHistoryEntry] = {
    var filtered = internals.recentHistory

    if search.nonEmpty then
      val lower = search.toLowerCase
      filtered = filtered.filter { r =>
        r.requestId.toLowerCase.contains(lower) ||
        r.taskName.toLowerCase.contains(lower) ||
        r.moduleIds.exists(_.toLowerCase.contains(lower))
      }

    if caller.nonEmpty then
      val cLower = caller.toLowerCase
      filtered = filtered.filter(_.caller.toString.toLowerCase.contains(cLower))

    status match
      case "success" => filtered = filtered.filter(_.success)
      case "failure" => filtered = filtered.filter(!_.success)
      case _         => // "all" — no filter

    filtered = sort match
      case "oldest"   => filtered.sortBy(_.startTime.toEpochMilli)
      case "longest"  => filtered.sortBy(-_.duration.toMillis)
      case "shortest" => filtered.sortBy(_.duration.toMillis)
      case _          => filtered.sortBy(-_.startTime.toEpochMilli) // newest first (default)

    val page = filtered.slice(offset, offset + limit)
    page.map { r =>
      ApiHistoryEntry(
        requestId = r.requestId,
        taskName = r.taskName,
        moduleIds = r.moduleIds,
        startTimeMs = r.startTime.toEpochMilli,
        durationMs = r.duration.toMillis,
        success = r.success
      )
    }
  }
}
