#!/usr/bin/env bash
# Compare tutalink schema on Mac vs Ubuntu Primary. Prints "same" or "difference".
set -euo pipefail

MAC_DUMP=$(mktemp)
UBU_DUMP=$(mktemp)
trap 'rm -f "$MAC_DUMP" "$UBU_DUMP"' EXIT

PGHOST="${PGHOST:-127.0.0.1}"
PGPORT="${PGPORT:-50010}"
PGUSER="${PGUSER:-test_user1}"
PGDATABASE="${PGDATABASE:-outdatedDBOct2021}"

pg_dump --schema-only -n tutalink -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" \
  | grep -vE '^--|^SET |^SELECT pg_catalog|^$' | sed 's/[ \t]*$//' | sort > "$MAC_DUMP"

UBUNTU_PSQL="${UBUNTU_PSQL:-$HOME/code/main/scripts/ubuntu-psql.sh}"
# Dump schema on Ubuntu Primary via a one-shot pg_dump over SSH if available
if [[ -x "$HOME/code/main/scripts/deploy-ssh-mac.sh" ]]; then
  PRIMARY="${UBUNTU_PRIMARY:-lawsen0@192.168.202.201}"
  "$HOME/code/main/scripts/deploy-ssh-mac.sh" "$PRIMARY" \
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
