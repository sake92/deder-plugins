#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
"$ROOT_DIR/deder-protobuf/scripts/gen-plugin-bindings.sh" "$@"
"$ROOT_DIR/deder-build-info/scripts/gen-plugin-bindings.sh" "$@"
"$ROOT_DIR/deder-web-dashboard/scripts/gen-plugin-bindings.sh" "$@"
