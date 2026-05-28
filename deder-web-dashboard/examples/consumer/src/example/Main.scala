package example

@main def main(): Unit =
  println("Deder Web Dashboard example module")
  println("Run `deder exec -t webDashboardStart` to start the dashboard.")
  println(s"Then open http://localhost:9090 in your browser.")
  println("Press Ctrl-C to stop (or run `deder exec -t webDashboardStop`).")

  // keep alive so the dashboard server stays up
  while true do
    Thread.sleep(10000)
