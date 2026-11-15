#!/usr/bin/env bash
# Show which hosts tutalink will use for the current DEPLOY_ARCHITECTURE.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/architecture.sh
source "$ROOT/scripts/lib/architecture.sh"
tutalink_print_architecture
