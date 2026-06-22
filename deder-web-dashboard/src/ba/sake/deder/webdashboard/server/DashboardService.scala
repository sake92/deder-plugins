package ba.sake.deder.webdashboard.server

import java.time.{Duration, Instant}
import scala.jdk.CollectionConverters.*
import ba.sake.tupson.*
import ba.sake.deder.config.DederProject
import ba.sake.deder.*
import ba.sake.deder.webdashboard.*

class DashboardService(internals: DederProjectInternals, tasksRegistry: TasksRegistryApi) {

  def allTasks: Seq[TaskInfo] = tasksRegistry.allTasks

  /** Per-task aggregate stats computed from recentHistory. */
  def taskAggregates: Seq[ApiTaskAggregate] = {
    val grouped = internals.recentHistory.groupBy(_.taskName)
    grouped.toSeq
      .map { case (taskName, requests) =>
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
      }
      .sortBy(_.taskName)
  }

  /** Per-module breakdown for a specific task. */
  def moduleBreakdown(taskName: String): Seq[ApiModuleAggregate] = {
    val requests = internals.recentHistory.filter(_.taskName == taskName)
    val moduleTimes = moduleTotalTimes(requests)
    moduleTimes.toSeq
      .map { case (moduleId, totalTimeMs) =>
        val moduleReqs = requests.filter(_.moduleIds.contains(moduleId))
        val invocations = moduleReqs.size.toLong
        val errors = moduleReqs.count(!_.success).toLong
        val durations = moduleReqs.map(_.duration.toMillis)
        val avgTimeMs = if invocations > 0 then totalTimeMs / invocations else 0L
        val minTimeMs = if durations.nonEmpty then durations.min else 0L
        val maxTimeMs = if durations.nonEmpty then durations.max else 0L
        ApiModuleAggregate(moduleId, invocations, errors, totalTimeMs, avgTimeMs, minTimeMs, maxTimeMs)
      }
      .sortBy(_.moduleId)
  }

  private def moduleTotalTimes(requests: Seq[CompletedRequest]): Map[String, Long] = {
    val pairs = for {
      req <- requests
      mod <- req.moduleIds
    } yield (mod, req.duration.toMillis)
    pairs.groupBy(_._1).view.mapValues(_.map(_._2).sum).toMap
  }

  /** Top N modules by total accumulated time across all tasks, descending. */
  def moduleAggregates(n: Int): Seq[ApiModuleAggregate] = {
    val grouped = internals.recentHistory
      .flatMap { r =>
        r.moduleIds.map(m => (m, r))
      }
      .groupBy(_._1)

    grouped.toSeq
      .map { case (moduleId, pairs) =>
        val requests = pairs.map(_._2)
        val totalTimeMs = requests.map(_.duration.toMillis).sum
        val invocations = requests.size.toLong
        val errors = requests.count(!_.success).toLong
        val durations = requests.map(_.duration.toMillis)
        val avgTimeMs = if invocations > 0 then totalTimeMs / invocations else 0L
        val minTimeMs = if durations.nonEmpty then durations.min else 0L
        val maxTimeMs = if durations.nonEmpty then durations.max else 0L
        ApiModuleAggregate(moduleId, invocations, errors, totalTimeMs, avgTimeMs, minTimeMs, maxTimeMs)
      }
      .sortBy(-_.totalTimeMs)
      .take(n)
  }

  /** Top N tasks by total accumulated time, descending. */
  def topOffenders(n: Int): Seq[ApiTaskAggregate] =
    taskAggregates.sortBy(-_.totalTimeMs).take(n)

  /** Error summary grouped by (taskName, moduleIds). */
  def errorSummary: Seq[ApiErrorSummaryEntry] = {
    val failures = internals.recentHistory.filter(!_.success)
    failures
      .groupBy(r => (r.taskName, r.moduleIds.sorted))
      .toSeq
      .map { case ((taskName, moduleIds), reqs) =>
        ApiErrorSummaryEntry(taskName, moduleIds, reqs.size.toLong)
      }
      .sortBy(e => (-e.errorCount, e.taskName))
  }

  def requestStatuses: Seq[ApiRequestStatus] = {
    internals.allRequestStatuses
      .filter(_.state != RequestState.COMPLETED)
      .map { r =>
        ApiRequestStatus(
          requestId = r.requestId,
          caller = formatCallerType(r.caller),
          taskName = r.taskName,
          moduleIds = r.moduleIds,
          startTimeMs = r.startTime.toEpochMilli,
          state = ApiRequestState.fromDeder(r.state),
          lockProgress = r.lockProgress.map(l => ApiLockProgress(l.acquired, l.total, l.blockingOn, l.heldBy)),
          taskProgress = r.taskProgress.map(t =>
            ApiTaskStageProgress(
              t.currentStage,
              t.totalStages,
              t.completed.size,
              t.failed.size,
              t.skipped.size,
              t.running.size,
              t.pending.size
            )
          )
        )
      }
  }

  def requestStatusesJson: String =
    requestStatuses.toJson

  // --- history filtering ---

  /** Filter and paginate recent history, returning a page of ApiHistoryEntry. */
  def filteredHistory(
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
        caller = formatCallerType(r.caller),
        taskName = r.taskName,
        moduleIds = r.moduleIds,
        startTimeMs = r.startTime.toEpochMilli,
        durationMs = r.duration.toMillis,
        success = r.success
      )
    }
  }

  private def formatCallerType(ct: CallerType): String = ct match
    case CallerType.Cli => "CLI"
    case CallerType.Bsp => "BSP"
    case null           => "unknown"
    case _              => ct.toString
}
