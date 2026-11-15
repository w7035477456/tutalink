#!/usr/bin/env bash
# Compare tutalink schema on Mac vs Ubuntu Primary (host depends on DEPLOY_ARCHITECTURE).
# Prints "same" or "difference".
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/architecture.sh
source "$ROOT/scripts/lib/architecture.sh"

tutalink_load_architecture

MAC_DUMP=$(mktemp)
UBU_DUMP=$(mktemp)
trap 'rm -f "$MAC_DUMP" "$UBU_DUMP"' EXIT

PGHOST="${PGHOST:-127.0.0.1}"
PGPORT="${PGPORT:-50010}"
PGUSER="${PGUSER:-test_user1}"
PGDATABASE="${PGDATABASE:-outdatedDBOct2021}"

pg_dump --schema-only -n tutalink -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" \
  | grep -vE '^--|^SET |^SELECT pg_catalog|^$' | sed 's/[ \t]*$//' | sort > "$MAC_DUMP"

PRIMARY_TARGET="$(tutalink_ssh_target "$TUTALINK_PRIMARY_HOST")"
echo "Comparing Mac tutalink schema vs Primary $TUTALINK_PRIMARY_HOST ($PRIMARY_TARGET) [$TUTALINK_ARCHITECTURE]" >&2

if [[ -x "$HOME/code/main/scripts/deploy-ssh-mac.sh" ]]; then
  "$HOME/code/main/scripts/deploy-ssh-mac.sh" "$PRIMARY_TARGET" \
    "pg_dump --schema-only -n tutalink -h 127.0.0.1 -p 50010 -U test_user1 -d outdatedDBOct2021" \
    | grep -vE '^--|^SET |^SELECT pg_catalog|^$' | sed 's/[ \t]*$//' | sort > "$UBU_DUMP"
else
  echo "difference"
  echo "(deploy-ssh-mac.sh not found; cannot reach Ubuntu Primary)" >&2
  exit 1
fi

if diff -q "$MAC_DUMP" "$UBU_DUMP" >/dev/null; then
  echo "same"
else
  echo "difference"
  if [[ "${1:-}" == "--verbose" ]]; then
    diff -u "$MAC_DUMP" "$UBU_DUMP" || true
  fi
  exit 1
fi
