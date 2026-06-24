package ba.sake.deder.tuidashboard.app

import layoutz.*

case class ScrollView[T](
    items: Seq[T],
    selectedIndex: Int,
    scrollOffset: Int,
    visibleHeight: Int,
    renderItem: (T, Int, Boolean) => Element,
    style: Element => Element = identity,
    scrollUpHint: String = "↑ ... more above",
    scrollDownHint: String = "↓ ... more below"
) extends Element {

  def render: String = {
    if items.isEmpty then return ""

    val safeVisibleHeight = math.max(1, visibleHeight)
    val safeOffset = math.max(0, math.min(scrollOffset, math.max(0, items.size - safeVisibleHeight)))
    val visible = items.slice(safeOffset, safeOffset + safeVisibleHeight)
    val hasAbove = safeOffset > 0
    val hasBelow = safeOffset + safeVisibleHeight < items.size

    val topHint = if hasAbove then {
      val countAbove = safeOffset
      Text(s"$scrollUpHint (${countAbove})").color(Color.BrightBlack)
    } else empty

    val bottomHint = if hasBelow then {
      val countBelow = items.size - safeOffset - safeVisibleHeight
      Text(s"$scrollDownHint (${countBelow})").color(Color.BrightBlack)
    } else empty

    val renderedItems = visible.zipWithIndex.map { case (item, localIdx) =>
      val globalIdx = safeOffset + localIdx
      renderItem(item, globalIdx, globalIdx == selectedIndex)
    }

    val elements = topHint +: renderedItems :+ bottomHint
    val content = layout(elements*)
    style(content).render
  }
}

def clampOffset(selected: Int, currentOff: Int, totalItems: Int, visibleHeight: Int): Int = {
  if totalItems <= visibleHeight then 0
  else if selected < currentOff then selected
  else if selected >= currentOff + visibleHeight then selected - visibleHeight + 1
  else currentOff
}
