package ba.sake.deder.webdashboard

import ba.sake.tupson.*
import ba.sake.deder.RequestState


case class ApiModule(id: String, `type`: String, deps: Int) derives JsonRW
case class StatsOverview(totalRequestsServed: Long, totalErrors: Long, uptimeSecs: Long) derives JsonRW
case class ApiCurrentRequest(
    requestId: String,
    caller: String,
    taskName: String,
    moduleIds: Seq[String],
    startTimeMs: Long
) derives JsonRW
case class ApiHistoryEntry(
    requestId: String,
    caller: String,
    taskName: String,
    moduleIds: Seq[String],
    startTimeMs: Long,
    durationMs: Long,
    success: Boolean
) derives JsonRW

enum ApiRequestState(val label: String):
  case Queued extends ApiRequestState("QUEUED")
  case AcquiringLocks extends ApiRequestState("ACQUIRING_LOCKS")
  case Executing extends ApiRequestState("EXECUTING")
  case Unknown extends ApiRequestState("UNKNOWN")

object ApiRequestState:
  def fromDeder(state: RequestState): ApiRequestState = state match
    case RequestState.QUEUED          => Queued
    case RequestState.ACQUIRING_LOCKS => AcquiringLocks
    case RequestState.EXECUTING       => Executing
    case _                            => Unknown

case class ApiLockProgress(
    acquired: Int,
    total: Int,
    blockingOn: Option[String],
    heldBy: Option[String]
) derives JsonRW

case class ApiTaskStageProgress(
    currentStage: Int,
    totalStages: Int,
    completed: Int,
    failed: Int,
    skipped: Int,
    running: Int,
    pending: Int
) derives JsonRW

case class ApiRequestStatus(
    requestId: String,
    caller: String,
    taskName: String,
    moduleIds: Seq[String],
    startTimeMs: Long,
    state: ApiRequestState,
    lockProgress: Option[ApiLockProgress],
    taskProgress: Option[ApiTaskStageProgress]
) derives JsonRW

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

// --- Task runner API types ---
case class ApiExecEntry(
    execId: String,
    taskName: String,
    moduleIds: Seq[String],
    startTimeMs: Long,
    endTimeMs: Option[Long],
    status: String,
    output: String,
    error: Option[String]
) derives JsonRW
