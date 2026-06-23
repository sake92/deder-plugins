package ba.sake.deder.webdashboard.pages

import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}

object Layout {

  def htmlPage(title: String, activeTab: String, content: Html, projectRoot: String = ""): Html =
    html"""
      <!DOCTYPE html>
      <html lang="en" data-theme="light">
      <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>$title</title>
        <link rel="stylesheet" href="/pico.zinc.min.css">
        <link rel="stylesheet" href="/dashboard.css">
        
        <script src="/htmx.min.js"></script>
        <script src="/alpine.min.js" defer></script>
        <script src="/cytoscape.min.js"></script>
        <script src="/dagre.min.js"></script>
        <script src="/cytoscape-dagre.min.js"></script>
      </head>
      <body>
      <header class="container">
        <nav>
          <ul>
            <li><a href="/server" ${activePageAttrs(activeTab, Set("server"))}><strong>Home</strong></a></li>
          </ul>
          <ul>
            <li><a href="/modules" ${activePageAttrs(activeTab, Set("modules", "graph"))}>📦 Modules</a></li>
            <li><a href="/tasks" ${activePageAttrs(activeTab, Set("tasks"))}>🔧 Tasks</a></li>
            <li><a href="/live" ${activePageAttrs(activeTab, Set("live", "history", "stats"))}>ℹ️ Stats</a></li>
          </ul>
        </nav>
        ${
          if activeTab == "modules" || activeTab == "graph" then
            html"""
            <nav class="sub-nav">
              <ul>
                <li><a href="/modules"       ${activePageAttrs(activeTab, Set("modules"))}>📋 List</a></li>
                <li><a href="/modules/graph" ${activePageAttrs(activeTab, Set("graph"))}>🕸️ Graph</a></li>
              </ul>
            </nav>
            """
          else if activeTab == "live" || activeTab == "history" || activeTab == "stats" then
            html"""
            <nav class="sub-nav">
              <ul>
                <li><a href="/live" ${activePageAttrs(activeTab, Set("live"))}>👁️ Live</a></li>
                <li><a href="/history" ${activePageAttrs(activeTab, Set("history"))}>📜 History</a></li>
                <li><a href="/stats" ${activePageAttrs(activeTab, Set("stats"))}>📊 Aggregates</a></li>
              </ul>
            </nav>
            """
          else Html("")
        }
        </header>
        <main class="container">$content</main>
        ${
          if projectRoot.nonEmpty then
            html"""<footer class="container"><small>📁 $projectRoot</small></footer>"""
          else Html("")
        }
      </body>
      </html>
    """
  
  private def activePageAttrs(activeTab: String, pages: Set[String]) =
    if pages.contains(activeTab) then html"""aria-current="page"""" else html""
}
