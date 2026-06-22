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
        <link rel="stylesheet" href="/pico.min.css">
        <link rel="stylesheet" href="/dashboard.css">
        <script src="/htmx.min.js"></script>
        <script src="/alpine.min.js" defer></script>
        <script src="/cytoscape.min.js"></script>
        <script src="/dagre.min.js"></script>
        <script src="/cytoscape-dagre.min.js"></script>
        <script src="/dashboard.js" defer></script>
      </head>
      <body>
        <nav hx-boost="true">
          <ul>
            <li><a href="/server" class="${if activeTab == "server" then "active" else ""}"><strong>Home</strong></a></li>
          </ul>
          <ul>
            <li><a href="/modules" class="${if activeTab == "modules" || activeTab == "graph" then "active" else ""}">Modules</a></li>
            <li><a href="/tasks" class="${if activeTab == "tasks" then "active" else ""}">Tasks</a></li>
            <li><a href="/live" class="${if activeTab == "live" || activeTab == "history" || activeTab == "stats" then "active" else ""}">Stats</a></li>
          </ul>
        </nav>
        ${
          if activeTab == "modules" || activeTab == "graph" then
            html"""
            <nav class="sub-nav" hx-boost="true">
              <ul>
                <li><a href="/modules"       class="${if activeTab == "modules" then "active" else ""}">List</a></li>
                <li><a href="/modules/graph" class="${if activeTab == "graph"   then "active" else ""}">Graph</a></li>
              </ul>
            </nav>
            """
          else if activeTab == "live" || activeTab == "history" || activeTab == "stats" then
            html"""
            <nav class="sub-nav" hx-boost="true">
              <ul>
                <li><a href="/live" class="${if activeTab == "live" then "active" else ""}">Live</a></li>
                <li><a href="/history" class="${if activeTab == "history" then "active" else ""}">History</a></li>
                <li><a href="/stats" class="${if activeTab == "stats" then "active" else ""}">Aggregates</a></li>
              </ul>
            </nav>
            """
          else Html("")
        }
        <main>$content</main>
        ${
          if projectRoot.nonEmpty then
            html"""<footer class="dashboard-footer">📁 $projectRoot</footer>"""
          else Html("")
        }
      </body>
      </html>
    """
}
