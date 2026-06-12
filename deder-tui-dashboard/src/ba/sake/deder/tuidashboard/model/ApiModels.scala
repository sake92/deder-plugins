package ba.sake.deder.tuidashboard.model

import ba.sake.tupson.*

case class ApiModule(id: String, `type`: String, deps: Int) derives JsonRW
case class StatsOverview(totalRequestsServed: Long, totalErrors: Long, uptimeSecs: Long) derives JsonRW
case class ApiCurrentRequest(requestId: String, taskName: String, moduleIds: Seq[String], startTimeMs: Long) derives JsonRW
case class ApiHistoryEntry(requestId: String, taskName: String, moduleIds: Seq[String], startTimeMs: Long, durationMs: Long, success: Boolean) derives JsonRW

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
