# Design: GitHub Pages + flatmark for deder-plugins

## Overview

Publish versioned plugin `.pkl` config schemas and a documentation site to GitHub Pages using flatmark. Follows the same pattern as the `sake92/deder` project's publishing pipeline, adapted for a multi-plugin monorepo.

## Published URL Structure

```
https://sake92.github.io/deder-plugins/
├── index.html                                    # Docs home
├── config/
│   ├── deder-build-info/
│   │   ├── early-access/
│   │   │   └── BuildInfoPlugin.pkl               # Latest from main
│   │   ├── v0.1.0/
│   │   │   └── BuildInfoPlugin.pkl               # Snapshot at tag
│   │   └── v0.2.0/
│   │       └── BuildInfoPlugin.pkl
│   ├── deder-protobuf/
│   │   ├── early-access/
│   │   │   └── ProtobufPlugin.pkl
│   │   └── ...
│   └── deder-web-dashboard/
│       ├── early-access/
│       │   └── WebDashboardPlugin.pkl
│       └── ...
└── (flatmark docs pages)
```

Consumer usage pattern:
```
amends "https://sake92.github.io/deder-plugins/config/deder-protobuf/early-access/ProtobufPlugin.pkl"
amends "https://sake92.github.io/deder-plugins/config/deder-protobuf/v0.1.0/ProtobufPlugin.pkl"
```

## Components

### 1. Build Script: `scripts/build-docs.sh`

Auto-discovers plugins by scanning for `*/resources/*Plugin.pkl` files (no hardcoded plugin list). For each plugin:

- **Early-access**: Copies `resources/<PluginName>Plugin.pkl` from HEAD of main into `docs/static/config/<plugin>/early-access/`
- **Versioned**: For each git tag (excluding `early-access`), uses `git ls-tree` and `git show` to extract that tag's `.pkl` file into `docs/static/config/<plugin>/$tag/`

Then runs `flatmark build -i docs` to generate the static site.

**Key behaviors:**
- Auto-discovers plugins — no maintenance when adding new plugins
- All plugins get versioned directories for every tag (simple, predictable)
- If a plugin didn't exist at a given tag, it's silently skipped (no error)
- Generated files in `docs/static/config/` are never committed

### 2. flatmark Configuration

- `docs/_config.yaml`: Minimal site config (name, description)
- `docs/_data/project.yaml`: Plugin listing for templates
- `docs/content/index.md`: Home page listing available plugins
- Theme: Reuses `sake92/flatmark-themes` default theme (auto-cached by flatmark)

Documentation pages can be added incrementally — the publishing pipeline is content-agnostic.

### 3. GitHub Actions Workflow: `.github/workflows/ghpages.yml`

Adapted from deder's `ghpages.yml`:

- **Triggers**: Push to main (non-release commits) + after `Release` workflow completes successfully
- **Runner**: macOS (flatmark is macOS-only, distributed as `.pkg`)
- **Steps**:
  1. Checkout (correct SHA for push vs workflow_run triggers)
  2. Download flatmark `.pkg` from GitHub Releases, install via `sudo installer`
  3. Run `./scripts/build-docs.sh` with `FLATMARK_BASE_URL` env var set
  4. Deploy `docs/_site/` to `gh-pages` branch using `JamesIves/github-pages-deploy-action@v4`

### 4. .gitignore Updates

```
docs/_site/
docs/.flatmark-cache
docs/static/config/
```

## Files to Create

| File | Purpose |
|------|---------|
| `scripts/build-docs.sh` | Core build script — collects .pkl files and runs flatmark |
| `docs/_config.yaml` | flatmark site configuration |
| `docs/_data/project.yaml` | Plugin listing data for docs templates |
| `docs/content/index.md` | Documentation home page |
| `.github/workflows/ghpages.yml` | CI workflow to build and deploy to GitHub Pages |

## Design Decisions

1. **Auto-discovery over hardcoded list**: Script finds plugins by scanning for `*/resources/*Plugin.pkl`. Adding a plugin requires zero script changes.
2. **All plugins per tag**: A git tag creates versioned directories for every plugin, even if unchanged. Simpler than tracking per-plugin changes.
3. **Same flatmark stack as deder**: Reuses the identical theme, layout patterns, and static asset approach.
4. **No early-access tag**: Unlike deder which has explicit `early-access` git tags, we use HEAD of main directly — simpler, fewer moving parts.
5. **Skip non-existent plugins at old tags**: If a plugin directory doesn't exist at a historical tag, the script silently skips it (no error, no empty directory).

## Differences from deder Reference

| Aspect | deder | deder-plugins |
|--------|-------|---------------|
| Config location | `config/*.pkl` (single source dir) | `*/resources/*Plugin.pkl` (per-plugin) |
| Directory structure | `config/early-access/`, `config/v0.15.0/` | `config/<plugin>/early-access/`, `config/<plugin>/v0.1.0/` |
| Plugin discovery | N/A (single product) | Auto-discovered from directory structure |
| Release workflow | Full JReleaser pipeline | Not yet exists (future work) |
| Docs content | Extensive (tutorials, how-tos, reference) | Starts minimal, grows incrementally |

## Error Handling

- **Missing plugin at historical tag**: Silently skipped (plugin didn't exist then)
- **No tags yet**: Versioned step produces no output (only early-access populated) — site still builds correctly
- **flatmark missing/not installed**: CI fails with clear error; local dev can install manually
- **Empty config directory**: flatmark copies `static/` verbatim — empty dirs are harmless
