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
  echo "Error: No plugins found (expected */resources/*Plugin.pkl files)"
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
