package ba.sake.deder.tuidashboard.app

import layoutz.Key

@main def TuiDashboardMain(args: String*): Unit = {
  val serverUrl = parseFlagValue(args.toSeq, "--server-url", "http://localhost:9292")
  val pollMs = parseFlagValue(args.toSeq, "--poll-ms", "1000").toInt

  // Safety net: if the JVM is killed by an external signal (e.g. Ctrl+C / SIGINT)
  // before the app can run its normal cleanup, restore the terminal to a sane state
  // so the user's shell does not hang or appear frozen.
  Runtime.getRuntime.addShutdownHook(Thread(() => {
    System.out.print("\u001b[?25h") // show cursor (if hidden)
    System.out.print("\u001b[0m")  // reset all SGR attributes
    System.out.flush()
  }))

  DashboardApp(serverUrl, pollMs).run(
    tickIntervalMs = 100,
    renderIntervalMs = 50,
    clearOnStart = true,
    clearOnExit = true,
    quitKey = Key.Ctrl('c')
  )
}

def parseFlagValue(args: Seq[String], flag: String, default: String): String = {
  val idx = args.indexOf(flag)
  if idx >= 0 && idx + 1 < args.length then args(idx + 1) else default
}
