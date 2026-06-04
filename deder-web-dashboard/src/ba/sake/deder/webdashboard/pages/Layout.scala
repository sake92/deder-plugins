package ba.sake.deder.webdashboard.pages

import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}

object Layout {
  
  def htmlPage(title: String, activeTab: String, content: Html): Html =
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
        </style>
      </head>
      <body>
        <nav class="navbar" hx-boost="true">
          <a href="/modules" class="${
            if activeTab == "modules" then "active" else ""
          }">Modules list</a>
          <a href="/modules/graph" class="${
            if activeTab == "graph" then "active" else ""
          }">Modules graph</a>
          <a href="/live" class="${
            if activeTab == "live" then "active" else ""
          }">Live Stats</a>
          <a href="/server" class="${
            if activeTab == "server" then "active" else ""
          }">Server</a>
        </nav>
        <main>$content</main>
      </body>
      </html>
    """
}
