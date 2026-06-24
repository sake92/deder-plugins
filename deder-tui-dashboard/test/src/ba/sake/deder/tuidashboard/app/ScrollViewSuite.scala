package ba.sake.deder.tuidashboard.app

import layoutz.*
import munit.FunSuite

class ScrollViewSuite extends FunSuite {

  test("render shows all items when items fit within visibleHeight") {
    val sv = ScrollView[String](
      items = Seq("a", "b", "c"),
      selectedIndex = 1,
      scrollOffset = 0,
      visibleHeight = 5,
      renderItem = (s, _, isSel) => if isSel then Text(s">$s") else Text(s" $s")
    )
    val rendered = sv.render
    assert(clue(rendered).contains(">b"))
    assert(clue(rendered).contains(" a"))
    assert(clue(rendered).contains(" c"))
    assert(!clue(rendered).contains("more above"))
    assert(!clue(rendered).contains("more below"))
  }

  test("render shows only visible slice when items exceed visibleHeight") {
    val sv = ScrollView[String](
      items = Seq("a", "b", "c", "d", "e", "f", "g"),
      selectedIndex = 2,
      scrollOffset = 0,
      visibleHeight = 3,
      renderItem = (s, _, isSel) => Text(s)
    )
    val rendered = sv.render
    assert(clue(rendered).contains("a"))
    assert(clue(rendered).contains("b"))
    assert(clue(rendered).contains("c"))
    assert(!clue(rendered).contains("d"))
    assert(clue(rendered).contains("more below"))
    assert(!clue(rendered).contains("more above"))
  }

  test("render shows scroll-up hint when offset > 0") {
    val sv = ScrollView[String](
      items = (0 to 9).map(_.toString),
      selectedIndex = 5,
      scrollOffset = 3,
      visibleHeight = 3,
      renderItem = (s, _, _) => Text(s)
    )
    val rendered = sv.render
    assert(clue(rendered).contains("more above"))
    assert(clue(rendered).contains("more below"))
  }

  test("render shows both hints when offset is in the middle") {
    val sv = ScrollView[String](
      items = (0 to 9).map(_.toString),
      selectedIndex = 5,
      scrollOffset = 3,
      visibleHeight = 3,
      renderItem = (s, _, _) => Text(s)
    )
    val rendered = sv.render
    assert(clue(rendered).contains("3"))
    assert(clue(rendered).contains("4"))
    assert(clue(rendered).contains("5"))
  }

  test("render handles empty items") {
    val sv = ScrollView[String](
      items = Seq.empty,
      selectedIndex = 0,
      scrollOffset = 0,
      visibleHeight = 5,
      renderItem = (s, _, _) => Text(s)
    )
    val rendered = sv.render
    assert(!clue(rendered).contains("more"))
  }

  test("render applies style wrapper") {
    val sv = ScrollView[String](
      items = Seq("x"),
      selectedIndex = 0,
      scrollOffset = 0,
      visibleHeight = 5,
      renderItem = (s, _, _) => Text(s),
      style = elem => layout(Text("PREFIX"), elem, Text("SUFFIX"))
    )
    val rendered = sv.render
    assert(clue(rendered).contains("PREFIX"))
    assert(clue(rendered).contains("SUFFIX"))
  }

  test("clampOffset returns selected when within visible range") {
    assertEquals(clampOffset(5, 3, 10, 5), 3)
  }

  test("clampOffset scrolls up when selected above visible window") {
    assertEquals(clampOffset(1, 5, 10, 3), 1)
  }

  test("clampOffset scrolls down when selected below visible window") {
    assertEquals(clampOffset(7, 3, 10, 3), 5)
  }

  test("clampOffset clamps to 0 as minimum") {
    assertEquals(clampOffset(0, 5, 0, 3), 0)
  }

  test("clampOffset clamps to max when selected at end") {
    assertEquals(clampOffset(9, 5, 10, 3), 7)
  }
}
