package ba.sake.deder.tuidashboard.render

import ba.sake.deder.tuidashboard.model.*
import jatatui.core.layout.{Constraint, Layout, Rect}
import jatatui.core.style.{Color, Modifier, Style}
import jatatui.core.text.{Line, Span}
import jatatui.core.buffer.Buffer
import jatatui.widgets.block.Block
import jatatui.widgets.paragraph.Paragraph
import jatatui.widgets.list.{List => JList, ListItem}
import jatatui.widgets.Borders
import scala.jdk.CollectionConverters.*

object DashboardRenderer {

  private val titleStyle      = Style.empty().withFg(Color.CYAN).withAddModifier(Modifier.BOLD)
  private val urlStyle         = Style.empty().withFg(Color.WHITE)
  private val dimStyle         = Style.empty().withFg(Color.DARK_GRAY)
  private val errorStyle       = Style.empty().withFg(Color.RED)
  private val successStyle     = Style.empty().withFg(Color.GREEN)
  private val highlightStyle   = Style.empty().withFg(Color.CYAN).withAddModifier(Modifier.BOLD)
  private val borderStyle      = Style.empty().withFg(Color.DARK_GRAY)

  private val moduleTypeColors: Map[String, Style] = Map(
    "SCALA"      -> Style.empty().withFg(Color.RED),
    "JAVA"       -> Style.empty().withFg(Color.YELLOW),
    "SCALA_TEST" -> Style.empty().withFg(Color.MAGENTA),
    "JAVA_TEST"  -> Style.empty().withFg(Color.BLUE)
  )

  def render(area: Rect, buf: Buffer, serverUrl: String, data: DashboardData, error: Option[String]): Unit = {
    val chunks = Layout.vertical(new Constraint.Length(4), new Constraint.Fill(1)).split(area)
    val statusBarArea = chunks(0)
    val contentArea   = chunks(1)

    renderStatusBar(statusBarArea, buf, serverUrl, data, error)
    renderContent(contentArea, buf, data)
  }

  private def renderStatusBar(area: Rect, buf: Buffer, serverUrl: String, data: DashboardData, error: Option[String]): Unit = {
    val statusBlock = Block.empty()
      .withBorders(Borders.ALL)
      .withBorderStyle(borderStyle)
    statusBlock.render(area, buf)
    val inner = statusBlock.inner(area)
    val rows  = Layout.vertical(new Constraint.Length(1), new Constraint.Length(1)).split(inner)

    val titleSpan = Span.styled("Deder TUI Dashboard (q to quit)", titleStyle)
    val spacer    = Span.styled("    ", Style.empty())
    val urlSpan   = Span.styled(serverUrl, urlStyle)
    val titleLine = Line.fromSpans(Seq(titleSpan, spacer, urlSpan).asJava)
    Paragraph.of(titleLine).render(rows(0), buf)

    val statsLine = formatStatsLine(data)
    Paragraph.of(statsLine).render(rows(1), buf)

    error.foreach { err =>
      val errSpan = Span.styled(s" ERROR: $err", errorStyle)
      val errLine = Line.fromSpans(Seq(errSpan).asJava)
      Paragraph.of(errLine).render(rows(1), buf)
    }
  }

  private def renderContent(area: Rect, buf: Buffer, data: DashboardData): Unit = {
    val chunks       = Layout.horizontal(new Constraint.Percentage(50), new Constraint.Percentage(50)).split(area)
    val modulesArea  = chunks(0)
    val requestsArea = chunks(1)

    renderModulesPane(modulesArea, buf, data.modules)
    renderRequestsPane(requestsArea, buf, data)
  }

  private def renderModulesPane(area: Rect, buf: Buffer, modules: Seq[ApiModule]): Unit = {
    val block = Block.empty()
      .withTitle(" Modules │ Type ")
      .withBorders(Borders.ALL)
      .withBorderStyle(borderStyle)
    block.render(area, buf)
    val inner = block.inner(area)

    if (modules.isEmpty) {
      val line = Line.fromSpans(Seq(Span.styled("(no modules)", dimStyle)).asJava)
      Paragraph.of(line).render(inner, buf)
    } else {
      val items = modules.map { m =>
        val moduleType = m.`type`.toUpperCase()
        val typeStyle  = moduleTypeColors.getOrElse(moduleType, Style.empty())
        val nameSpan   = Span.styled(m.id, Style.empty())
        val spacerSpan = Span.styled("  ", Style.empty())
        val typeSpan   = Span.styled(String.format("%-12s", moduleType), typeStyle)
        val line       = Line.fromSpans(Seq(nameSpan, spacerSpan, typeSpan).asJava)
        ListItem.of(line)
      }
      JList.of(items.asJava).render(inner, buf)
    }
  }

  private def renderRequestsPane(area: Rect, buf: Buffer, data: DashboardData): Unit = {
    val chunks       = Layout.vertical(new Constraint.Percentage(50), new Constraint.Percentage(50)).split(area)
    val currentArea  = chunks(0)
    val historyArea  = chunks(1)

    renderCurrentRequestsPane(currentArea, buf, data.currentRequests)
    renderHistoryPane(historyArea, buf, data.history)
  }

  private def renderCurrentRequestsPane(area: Rect, buf: Buffer, requests: Seq[ApiCurrentRequest]): Unit = {
    val count = requests.size
    val block = Block.empty()
      .withTitle(s" Current Requests ($count) ")
      .withBorders(Borders.ALL)
      .withBorderStyle(borderStyle)
    block.render(area, buf)
    val inner = block.inner(area)

    if (requests.isEmpty) {
      val line = Line.fromSpans(Seq(Span.styled("(no active requests)", dimStyle)).asJava)
      Paragraph.of(line).render(inner, buf)
    } else {
      val items = requests.map { r =>
        val idSpan     = Span.styled(r.requestId + "  ", dimStyle)
        val taskSpan   = Span.styled(r.taskName, Style.empty())
        val moduleSpan = Span.styled("  [" + r.moduleIds.mkString(",") + "]", Style.empty().withFg(Color.CYAN))
        val line       = Line.fromSpans(Seq(idSpan, taskSpan, moduleSpan).asJava)
        ListItem.of(line)
      }
      JList.of(items.asJava).render(inner, buf)
    }
  }

  private def renderHistoryPane(area: Rect, buf: Buffer, history: Seq[ApiHistoryEntry]): Unit = {
    val count = history.size
    val block = Block.empty()
      .withTitle(s" Recent History ($count) ")
      .withBorders(Borders.ALL)
      .withBorderStyle(borderStyle)
    block.render(area, buf)
    val inner = block.inner(area)

    if (history.isEmpty) {
      val line = Line.fromSpans(Seq(Span.styled("(no history)", dimStyle)).asJava)
      Paragraph.of(line).render(inner, buf)
    } else {
      val items = history.map { h =>
        val statusSymbol = if (h.success) "✓" else "✗"
        val statusColor  = if (h.success) successStyle else errorStyle
        val statusSpan   = Span.styled(statusSymbol + " ", statusColor)
        val taskSpan     = Span.styled(h.taskName + "  ", Style.empty())
        val durSpan      = Span.styled(formatDuration(h.durationMs) + "  ", dimStyle)
        val moduleSpan   = Span.styled("[" + h.moduleIds.mkString(",") + "]", Style.empty().withFg(Color.CYAN))
        val line         = Line.fromSpans(Seq(statusSpan, taskSpan, durSpan, moduleSpan).asJava)
        ListItem.of(line)
      }
      JList.of(items.asJava).render(inner, buf)
    }
  }

  private def formatStatsLine(data: DashboardData): Line = {
    data.overview match {
      case Some(ov) =>
        val served = s"Served: ${ov.totalRequestsServed}"
        val errors = s"Errors: ${ov.totalErrors}"
        val uptime = s"Uptime: ${formatUptime(ov.uptimeSecs)}"
        Line.from(s"$served  $errors  $uptime")
      case None =>
        Line.fromSpans(Seq(Span.styled("No connection to server", dimStyle)).asJava)
    }
  }

  private def formatUptime(seconds: Long): String = {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    if (h > 0) s"${h}h ${m}m"
    else if (m > 0) s"${m}m ${s}s"
    else s"${s}s"
  }

  private def formatDuration(ms: Long): String = {
    if (ms >= 1000) f"${ms / 1000.0}%.1fs"
    else s"${ms}ms"
  }
}
