# GitHub Pages + flatmark for deder-plugins Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Publish versioned plugin `.pkl` config schemas and a documentation site to GitHub Pages using flatmark, following the `sake92/deder` pattern.

**Architecture:** A `build-docs.sh` script auto-discovers plugins, copies their `.pkl` schemas into `docs/static/config/<plugin>/early-access/` and `docs/static/config/<plugin>/<tag>/`, then runs `flatmark build -i docs`. A `ghpages.yml` GitHub Actions workflow triggers on push to main and release, downloading flatmark, running the build script, and deploying `docs/_site/` to the `gh-pages` branch.

**Tech Stack:** Bash, flatmark v0.0.28, GitHub Actions, Pico CSS (via flatmark theme)

---

### Task 1: Update .gitignore

**Files:**
- Modify: `.gitignore`

- [ ] **Step 1: Add flatmark/generated entries to .gitignore**

Append these three lines to `.gitignore`:

```
# flatmark generated output
docs/_site/
docs/.flatmark-cache
docs/static/config/
```

- [ ] **Step 2: Verify .gitignore is correct**

Run: `cat .gitignore`
Expected: The file ends with the three new lines above.

- [ ] **Step 3: Commit**

```bash
git add .gitignore
git commit -m "chore: add flatmark generated dirs to .gitignore"
```

---

### Task 2: Create flatmark site configuration

**Files:**
- Create: `docs/_config.yaml`
- Create: `docs/_data/project.yaml`

- [ ] **Step 1: Create `docs/_config.yaml`**

Run: `mkdir -p docs/_data docs/content`

Then write this content:

```yaml
name: Deder Plugins
description: Official Deder Build Tool Plugins
```

- [ ] **Step 2: Create `docs/_data/project.yaml`**

```yaml
plugins:
  - name: Build Info
    dir: deder-build-info
    schema: BuildInfoPlugin.pkl
    description: Generates a Scala BuildInfo object with metadata (version, git hash, timestamp).
  - name: Protobuf
    dir: deder-protobuf
    schema: ProtobufPlugin.pkl
    description: Generates protobuf and gRPC Java/Scala code from .proto files.
  - name: Web Dashboard
    dir: deder-web-dashboard
    schema: WebDashboardPlugin.pkl
    description: Embedded HTTP dashboard showing module info, dependency graph, and live stats.
```

- [ ] **Step 3: Commit**

```bash
git add docs/_config.yaml docs/_data/project.yaml
git commit -m "docs: add flatmark site config"
```

---

### Task 3: Create documentation home page

**Files:**
- Create: `docs/content/index.md`

- [ ] **Step 1: Create `docs/content/index.md`**

```markdown
# Deder Plugins

Official plugins for the [Deder build tool](https://sake92.github.io/deder/).

## Available Plugins

| Plugin | Schema |
|--------|--------|
| **Build Info** | [early-access](config/deder-build-info/early-access/BuildInfoPlugin.pkl) |
| Generates a Scala `BuildInfo` object with metadata. | |
| **Protobuf** | [early-access](config/deder-protobuf/early-access/ProtobufPlugin.pkl) |
| Generates protobuf and gRPC code from `.proto` files. | |
| **Web Dashboard** | [early-access](config/deder-web-dashboard/early-access/WebDashboardPlugin.pkl) |
| Embedded HTTP dashboard with module info and live stats. | |

## Using a Plugin

Add a plugin to your `deder.pkl` by amending its config schema:

```pkl
amends "https://sake92.github.io/deder-plugins/config/deder-protobuf/early-access/ProtobufPlugin.pkl"
```

Or pin to a specific version:

```pkl
amends "https://sake92.github.io/deder-plugins/config/deder-protobuf/v0.1.0/ProtobufPlugin.pkl"
```

Versioned schemas are published automatically for every git tag.
```

- [ ] **Step 2: Commit**

```bash
git add docs/content/index.md
git commit -m "docs: add plugin listing home page"
```

---

### Task 4: Create build script (`scripts/build-docs.sh`)

**Files:**
- Create: `scripts/build-docs.sh`

- [ ] **Step 1: Create the script**

```bash
#!/usr/bin/env bash
set -euo pipefail

echo "=== Cleaning previous output ==="
rm -rf docs/static/config

echo "=== Auto-discovering plugins ==="
shopt -s nullglob
PLUGINS=()
for pkl_file in */resources/*Plugin.pkl; do
  plugin_name=$(echo "$pkl_file" | cut -d/ -f1)
  PLUGINS+=("$plugin_name")
done

if [ ${#PLUGINS[@]} -eq 0 ]; then
  echo "Error: No plugins found (expected /*/resources/*Plugin.pkl files)"
  exit 1
fi

echo "Found plugins: ${PLUGINS[*]}"

echo "=== Publishing early-access (latest from main) ==="
for plugin in "${PLUGINS[@]}"; do
  mkdir -p "docs/static/config/$plugin/early-access"
  for pkl_file in "$plugin"/resources/*Plugin.pkl; do
    cp "$pkl_file" "docs/static/config/$plugin/early-access/"
    echo "  $plugin/early-access/$(basename "$pkl_file")"
  done
done

echo "=== Publishing versioned configs from git tags ==="
for tag in $(git tag --sort=-creatordate | grep -v 'early-access'); do
  echo "  Processing tag: $tag"
  for plugin in "${PLUGINS[@]}"; do
    mkdir -p "docs/static/config/$plugin/$tag"
    pkl_files=$(git ls-tree -r --name-only "$tag" -- "$plugin/resources" 2>/dev/null | grep 'Plugin\.pkl$' || true)
    if [ -n "$pkl_files" ]; then
      for pkl_file in $pkl_files; do
        git show "$tag:$pkl_file" > "docs/static/config/$plugin/$tag/$(basename "$pkl_file")"
        echo "    $plugin/$tag/$(basename "$pkl_file")"
      done
    fi
  done
done

echo "=== Building flatmark site ==="
flatmark build -i docs

echo "=== Done ==="
echo "Site built to docs/_site/"
```

- [ ] **Step 2: Make the script executable**

Run: `chmod +x scripts/build-docs.sh`

- [ ] **Step 3: Verify the script discovers plugins correctly**

Run: `bash scripts/build-docs.sh`
Expected: The script runs and outputs "Found plugins: deder-build-info deder-protobuf deder-web-dashboard". If flatmark is not installed, it will fail at the `flatmark build` step — that's expected for now.

If flatmark IS installed locally, expected: prints "Site built to docs/_site/".

- [ ] **Step 4: Verify generated directory structure**

Run: `find docs/static/config -type f | sort`
Expected output:
```
docs/static/config/deder-build-info/early-access/BuildInfoPlugin.pkl
docs/static/config/deder-protobuf/early-access/ProtobufPlugin.pkl
docs/static/config/deder-web-dashboard/early-access/WebDashboardPlugin.pkl
```

(Versioned directories only appear if git tags exist.)

- [ ] **Step 5: Commit**

```bash
git add scripts/build-docs.sh
git commit -m "feat: add build-docs.sh for gh-pages publishing"
```

---

### Task 5: Create GitHub Actions workflow

**Files:**
- Create: `.github/workflows/ghpages.yml`

- [ ] **Step 1: Create `.github/workflows/ghpages.yml`**

Run: `mkdir -p .github/workflows`

```yaml
name: Deploy GhPages

on:
  push:
    branches: [main]
  workflow_run:
    workflows: ["Release"]
    types: [completed]
    branches: [main]

jobs:
  build-and-deploy:
    if: |
      github.repository == 'sake92/deder-plugins' &&
      (
        (github.event_name == 'push' && !startsWith(github.event.head_commit.message, 'Releasing version')) ||
        (github.event_name == 'workflow_run' && github.event.workflow_run.conclusion == 'success')
      )
    runs-on: macos-latest
    permissions:
      contents: write
    steps:
      - name: Checkout
        uses: actions/checkout@v4
        with:
          ref: ${{ github.event_name == 'workflow_run' && github.event.workflow_run.head_sha || github.sha }}
          fetch-depth: 0

      - name: Build docs
        env:
          FLATMARK_BASE_URL: https://sake92.github.io/deder-plugins
        run: |
          wget https://github.com/sake92/flatmark/releases/download/v0.0.28/flatmark-1.0.0.pkg
          sudo installer -pkg flatmark-1.0.0.pkg -target /
          ./scripts/build-docs.sh

      - name: Deploy to gh-pages
        uses: JamesIves/github-pages-deploy-action@v4
        with:
          folder: docs/_site
          branch: gh-pages
          clean: true
```

- [ ] **Step 2: Commit**

```bash
git add .github/workflows/ghpages.yml
git commit -m "ci: add ghpages deploy workflow"
```

---

### Task 6: End-to-end verification

**Files:**
- None (verification only)

- [ ] **Step 1: Verify all files exist**

Run: `ls -la .gitignore scripts/build-docs.sh docs/_config.yaml docs/_data/project.yaml docs/content/index.md .github/workflows/ghpages.yml`
Expected: All six files exist.

- [ ] **Step 2: Verify .gitignore covers generated directories**

Run: `git check-ignore docs/_site/ docs/.flatmark-cache docs/static/config/`
Expected: All three paths are ignored (no error output — check-ignore prints the path if ignored, errors if not).

- [ ] **Step 3: Verify the build script is syntactically valid**

Run: `bash -n scripts/build-docs.sh`
Expected: No output (exit code 0).

- [ ] **Step 4: Verify the workflow YAML is valid**

Run: `python3 -c "import yaml; yaml.safe_load(open('.github/workflows/ghpages.yml'))" 2>&1 || echo "If yaml module unavailable, visually verify the file"`
Expected: No parse errors.

- [ ] **Step 5: Run the build script again to confirm clean output**

Run: `bash scripts/build-docs.sh`
Expected: Same output as Task 4 Step 3. All three plugin .pkl files appear under `docs/static/config/`.

- [ ] **Step 6: Verify git log shows all commits**

Run: `git log --oneline -6`
Expected: Five commits in order (Task 1-5).

- [ ] **Step 7: Push to remote**

```bash
git push origin main
```
