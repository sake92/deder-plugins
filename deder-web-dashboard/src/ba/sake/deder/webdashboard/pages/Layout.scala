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
        <script src="/htmx.min.js"></script>
        <script src="/alpine.min.js" defer></script>
        <script src="/cytoscape.min.js"></script>
        <script src="/dagre.min.js"></script>
        <script src="/cytoscape-dagre.min.js"></script>
        <script src="/dashboard.js" defer></script>
        <style>
          :root {
            --pico-font-size: 88%;
            --pico-spacing: 0.6rem;
            --pico-line-height: 1.4;
          }
          body { max-width: 1100px; margin: 0 auto; padding: 0.6rem 0.75rem; }
          .navbar { display: flex; gap: 0.25rem; margin-bottom: 0.75rem; border-bottom: 1px solid var(--pico-muted-border-color); padding-bottom: 0.4rem; }
          .navbar a { text-decoration: none; padding: 0.25rem 0.5rem; border-radius: 4px; font-size: 0.85rem; font-weight: 600; }
          .navbar a:hover { background: var(--pico-muted-border-color); }
          .navbar a.active { background: var(--pico-primary); color: var(--pico-primary-inverse); }
          .stat-card { display: inline-block; border: 1px solid var(--pico-muted-border-color); border-radius: 4px; padding: 0.4rem 0.6rem; margin: 0.2rem; min-width: 140px; text-align: center; }
          .stat-card .label { font-size: 0.7rem; color: var(--pico-muted-color); text-transform: uppercase; letter-spacing: 0.5px; }
          .stat-card .value { font-size: 1.1rem; font-weight: 700; }
          .success { color: var(--pico-color-green-400); }
          .failure { color: var(--pico-color-red-400); }
          #cy { width: 100%; height: 520px; border: 1px solid var(--pico-muted-border-color); border-radius: 4px; }
          .graph-controls { display: flex; gap: 0.4rem; flex-wrap: wrap; align-items: center; margin-bottom: 0.4rem; font-size: 0.8rem; }
          .graph-controls label { display: flex; align-items: center; gap: 0.15rem; cursor: pointer; }
          .graph-controls input[type="checkbox"] { margin: 0; }
          .highlighted { border-width: 3px !important; border-color: var(--pico-color-yellow-300) !important; }
          .dimmed { opacity: 0.15; }
          .subnav { display: flex; align-items: center; margin-bottom: 0.75rem; padding-bottom: 0.4rem; border-bottom: 1px solid var(--pico-muted-border-color); font-size: 0.85rem; }
          .subnav a { text-decoration: none; padding: 0.2rem 0.5rem; border: 1px solid var(--pico-muted-border-color); }
          .subnav a:first-child { border-radius: 4px 0 0 4px; }
          .subnav a:last-child  { border-radius: 0 4px 4px 0; }
          .subnav a + a { border-left: none; }
          .subnav a:hover { background: var(--pico-muted-border-color); }
          .subnav a.active { background: var(--pico-primary); color: var(--pico-primary-inverse); border-color: var(--pico-primary); }
          input[type="text"], input[type="search"], select { padding: 0.2rem 0.4rem; font-size: 0.82rem; height: auto; }
          .request-section { margin-bottom: 1rem; }
          .section-header { display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.5rem; }
          .state-badge { padding: 0.1rem 0.5rem; border-radius: 4px; font-size: 0.75rem; font-weight: bold; }
          .state-badge.queued { background: #e2e3e5; color: #383d41; }
          .state-badge.locks { background: #fff3cd; color: #856404; }
          .state-badge.executing { background: #d4edda; color: #155724; }
          .cancel-btn { color: var(--pico-color-red-400); background: none; border: 1px solid var(--pico-color-red-400); padding: 0.1rem 0.5rem; border-radius: 4px; cursor: pointer; font-size: 0.75rem; }
          .cancel-btn:hover { background: var(--pico-color-red-400); color: white; }
          .progress-bar { height: 6px; background: #e9ecef; border-radius: 3px; margin: 4px 0; min-width: 80px; }
          .progress-bar-fill { height: 100%; border-radius: 3px; transition: width 0.3s; }
          .progress-bar-fill.locks { background: #ffc107; }
          .progress-bar-fill.stages { background: #28a745; }
          .progress-detail { font-size: 0.75rem; color: var(--pico-muted-color); line-height: 1.3; }
        </style>
      </head>
      <body>
        <nav class="navbar" hx-boost="true">
          <a href="/modules" class="${
            if activeTab == "modules" || activeTab == "graph" then "active" else ""
          }">Modules</a>
          <a href="/tasks" class="${
            if activeTab == "tasks" then "active" else ""
          }">Tasks</a>
          <a href="/live" class="${
            if activeTab == "live" || activeTab == "history" || activeTab == "stats" then "active" else ""
          }">Stats</a>
          <a href="/server" class="${
            if activeTab == "server" then "active" else ""
          }">Info</a>
        </nav>
        ${
          if activeTab == "modules" || activeTab == "graph" then
            html"""
            <nav class="subnav" hx-boost="true">
              <a href="/modules"       class="${if activeTab == "modules" then "active" else ""}">List</a>
              <a href="/modules/graph" class="${if activeTab == "graph"   then "active" else ""}">Graph</a>
            </nav>
            """
          else if activeTab == "live" || activeTab == "history" || activeTab == "stats" then
            html"""
            <nav class="subnav" hx-boost="true">
              <a href="/live" class="${if activeTab == "live" then "active" else ""}">Live</a>
              <a href="/history" class="${if activeTab == "history" then "active" else ""}">History</a>
              <a href="/stats" class="${if activeTab == "stats" then "active" else ""}">Aggregates</a>
            </nav>
            """
          else Html("")
        }
        <main>$content</main>
        ${
          if projectRoot.nonEmpty then
            html"""<footer style="margin-top:1.5rem; padding-top:0.5rem; border-top:1px solid var(--pico-muted-border-color); text-align:center; font-size:0.75rem; color:var(--pico-muted-color);">📁 $projectRoot</footer>"""
          else Html("")
        }
      </body>
      </html>
    """
}
