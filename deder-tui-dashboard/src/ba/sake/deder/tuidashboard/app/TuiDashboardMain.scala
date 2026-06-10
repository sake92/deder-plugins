package ba.sake.deder.tuidashboard.app

import ba.sake.deder.tuidashboard.client.DashboardClient
import ba.sake.deder.tuidashboard.render.DashboardRenderer
import jatatui.crossterm.{CrosstermBackend, Jatatui}
import tui.crossterm.{Duration, Event, KeyCode}

import scala.util.boundary, boundary.break

@main def TuiDashboardMain(args: String*): Unit = {
  val serverUrl = parseFlagValue(args.toSeq, "--server-url", "http://localhost:9292")
  val pollMs = parseFlagValue(args.toSeq, "--poll-ms", "1000").toInt

  val client = DashboardClient(serverUrl)

  Jatatui.runIo { terminal =>
    val ct = terminal.backend().asInstanceOf[CrosstermBackend].writer()
    var lastError: Option[String] = None

    boundary:
      while !Thread.currentThread().isInterrupted do {
        val data = client.fetchAll()

        terminal.draw { frame =>
          DashboardRenderer.render(frame.area(), frame.bufferMut(), serverUrl, data, lastError)
        }

        val secs = pollMs / 1000
        val nanos = (pollMs % 1000) * 1_000_000
        if ct.poll(new Duration(secs, nanos)) then {
          ct.read() match {
            case event if event.isInstanceOf[Event.Key] =>
              val key = event.asInstanceOf[Event.Key]
              key.keyEvent().code() match {
                case c: KeyCode.Char if c.c() == 'q' => break()
                case _                                =>
              }
            case _ =>
          }
        }
      }
  }
}

private def parseFlagValue(args: Seq[String], flag: String, default: String): String = {
  val idx = args.indexOf(flag)
  if idx >= 0 && idx + 1 < args.length then args(idx + 1) else default
}
