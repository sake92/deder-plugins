package ba.sake.deder.webdashboard.server.stubs

import java.time.Duration
import ba.sake.deder.*

class StubInternals(
    val currentRequests: Seq[LiveRequest] = Seq.empty,
    val recentHistory: Seq[CompletedRequest] = Seq.empty,
    val totalRequestsServed: Long = 0L,
    val totalErrors: Long = 0L,
    val serverUptime: Duration = Duration.ZERO,
    val loadedPlugins: Seq[LoadedPluginInfo] = Seq.empty,
    val inMemoryCachesStats: Map[String, InMemCacheStats] = Map.empty,
    val allRequestStatuses: Seq[RequestStatus] = Seq.empty,
    val cancelFn: String => Boolean = _ => false,
    val purgeFn: () => PurgeCachesResult = () => PurgeCachesResult(0, 0, 0, false),
) extends DederProjectInternals:

  def taskStats(taskName: String): Option[TaskStats] = None
  def allTaskStats: Seq[(String, TaskStats)] = Seq.empty
  def cancelRequest(requestId: String): Boolean = cancelFn(requestId)
  def requestStatus(requestId: String): Option[RequestStatus] = None
  def purgeInMemoryCaches(): PurgeCachesResult = purgeFn()
