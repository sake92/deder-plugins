package ba.sake.deder.tuidashboard.client

import ba.sake.deder.tuidashboard.model.*
import ba.sake.tupson.*
import sttp.client3.*

class DashboardClient(serverUrl: String) {

  private val backend = HttpClientSyncBackend()

  def fetchModules(): Either[String, Seq[ApiModule]] =
    fetch[Seq[ApiModule]](s"$serverUrl/api/modules")

  def fetchOverview(): Either[String, StatsOverview] =
    fetch[StatsOverview](s"$serverUrl/api/stats/overview")

  def fetchCurrentRequests(): Either[String, Seq[ApiCurrentRequest]] =
    fetch[Seq[ApiCurrentRequest]](s"$serverUrl/api/stats/current")

  def fetchHistory(): Either[String, Seq[ApiHistoryEntry]] =
    fetch[Seq[ApiHistoryEntry]](s"$serverUrl/api/stats/history")

  /** Fetch all endpoints and combine into DashboardData. Errors are collected per-endpoint. */
  def fetchAll(): DashboardData = {
    val modules = fetchModules().fold(_ => Seq.empty, identity)
    val overview = fetchOverview().fold(_ => None, Some(_))
    val current = fetchCurrentRequests().fold(_ => Seq.empty, identity)
    val history = fetchHistory().fold(_ => Seq.empty, identity)
    DashboardData(modules, overview, current, history)
  }

  private def fetch[T: JsonRW](url: String): Either[String, T] = {
    try {
      val response = basicRequest
        .get(uri"$url")
        .response(asStringAlways)
        .send(backend)
      Right(response.body.parseJson[T])
    } catch {
      case e: Exception => Left(s"Request failed to $url: ${e.getMessage}")
    }
  }
}
