package ba.sake.deder.tuidashboard.model

import ba.sake.tupson.*

case class ApiModule(id: String, `type`: String, deps: Int) derives JsonRW
case class StatsOverview(totalRequestsServed: Long, totalErrors: Long, uptimeSecs: Long) derives JsonRW
case class ApiCurrentRequest(requestId: String, taskName: String, moduleIds: Seq[String], startTimeMs: Long) derives JsonRW
case class ApiHistoryEntry(requestId: String, taskName: String, moduleIds: Seq[String], startTimeMs: Long, durationMs: Long, success: Boolean) derives JsonRW

/** Combined snapshot of all data fetched from the dashboard API. */
case class DashboardData(
    modules: Seq[ApiModule],
    overview: Option[StatsOverview],
    currentRequests: Seq[ApiCurrentRequest],
    history: Seq[ApiHistoryEntry]
)
