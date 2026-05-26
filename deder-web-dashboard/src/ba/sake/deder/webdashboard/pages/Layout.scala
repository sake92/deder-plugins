package ba.sake.deder.webdashboard.pages

import ba.sake.sharaf.*, ba.sake.sharaf.{given, *}

object Layout {
  def htmlPage(title: String, content: Html): Html =
    html"""
      <!DOCTYPE html>
      <html lang="en" data-theme="light">
      <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${title}</title>
        <link rel="stylesheet" href="/public/simple.min.css">
        <script src="/public/htmx.min.js"></script>
        <style>
          body { max-width: 960px; margin: 0 auto; padding: 1rem; }
          .navbar { display: flex; gap: 1rem; margin-bottom: 1.5rem; border-bottom: 1px solid var(--border); padding-bottom: 0.75rem; }
          .navbar a { text-decoration: none; font-weight: 600; }
          .stat-card { display: inline-block; border: 1px solid var(--border); border-radius: 6px; padding: 1rem; margin: 0.5rem; min-width: 180px; text-align: center; }
          .stat-card .label { font-size: 0.85rem; color: var(--text-light); }
          .stat-card .value { font-size: 1.5rem; font-weight: 700; }
          .success { color: green; }
          .failure { color: red; }
        </style>
      </head>
      <body>
        <nav class="navbar">
          <a href="/modules">Modules</a>
          <a href="/stats">Live Stats</a>
        </nav>
        <main>${content}</main>
      </body>
      </html>
    """
}
