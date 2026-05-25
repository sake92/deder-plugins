package consumer

@main def main(): Unit =
  println(s"Module:      ${BuildInfo.moduleName}")
  println(s"Version:     ${BuildInfo.moduleVersion}")
  println(s"Scala:       ${BuildInfo.scalaVersion}")
  println(s"Java:        ${BuildInfo.javaVersion}")
  println(s"Git HEAD:    ${BuildInfo.gitHead}")
  println(s"Built at:    ${BuildInfo.builtAtMillis}")
  println()
  println("All values:")
  BuildInfo.toMap.foreach { case (k, v) =>
    println(s"  $k = $v")
  }
