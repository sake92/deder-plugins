#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PLUGIN_DIR="$(dirname "$SCRIPT_DIR")"

PKL_FILE="$PLUGIN_DIR/resources/SourcegenPlugin.pkl"
PLUGIN_DIR_TEMP=$(mktemp -d --suffix=_PKL_JAVA_GEN)

echo "Generating Java bindings from config: $PKL_FILE"
pkl-codegen-java "$PKL_FILE" -o "$PLUGIN_DIR_TEMP"

cp -r "$PLUGIN_DIR_TEMP/java/." "$PLUGIN_DIR/src"
cp -r "$PLUGIN_DIR_TEMP/resources/." "$PLUGIN_DIR/resources"
rm -rf "$PLUGIN_DIR_TEMP"

echo "Done! Generated Java bindings in $PLUGIN_DIR"
