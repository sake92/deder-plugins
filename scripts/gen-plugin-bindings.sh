#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PLUGIN_DIR="$ROOT_DIR/deder-protobuf"
PKL_FILE="$PLUGIN_DIR/src/main/resources/ProtobufPlugin.pkl"
TMP_DIR="$(mktemp -d --suffix=_deder_protobuf_pkl)"

cleanup() {
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

if ! command -v pkl-codegen-java >/dev/null 2>&1; then
  echo "pkl-codegen-java is required on PATH" >&2
  exit 1
fi

echo "Generating Java bindings from $PKL_FILE"
pkl-codegen-java "$PKL_FILE" -o "$TMP_DIR"
cp -r "$TMP_DIR/java/." "$PLUGIN_DIR/src/main/java"
cp -r "$TMP_DIR/resources/." "$PLUGIN_DIR/src/main/resources"
echo "Bindings updated under deder-protobuf/src/main/java and deder-protobuf/src/main/resources"
