#!/usr/bin/env bash
# Shared DEPLOY_ARCHITECTURE resolution for tutalink (repo-local only).
# Source this file from deploy / health / schema scripts.
#
# Reads (first match wins for the value):
#   1) DEPLOY_ARCHITECTURE already in the environment
#   2) ~/.ssh/tutalink/.env
#   3) ~/.ssh/be/.env  (same key TutaMall uses)
#
# Values:
#   ALL-IN-ONE      → app + DB on xbox0 (192.168.222.200); Mac: local Postgres both sides
#   MINI_PRODUCTION → Primary xbox1; web (tutalink) on xbox2 + xbox3
#   FULL_PRODUCTION → not implemented yet

# shellcheck disable=SC2034

_tutalink_read_arch_from_env_file() {
  local file="$1"
  [[ -f "$file" ]] || return 1
  # Only the DEPLOY_ARCHITECTURE line — never print or export other secrets from the file.
  local line
  line=$(grep -E '^[[:space:]]*DEPLOY_ARCHITECTURE=' "$file" | tail -n1) || return 1
  line="${line#DEPLOY_ARCHITECTURE=}"
  line="${line%%#*}"
  line="${line//[\"\']/}"
  line="${line//[[:space:]]/}"
  [[ -n "$line" ]] || return 1
  printf '%s' "$line"
}

tutalink_load_architecture() {
  local raw="${DEPLOY_ARCHITECTURE:-}"
  if [[ -z "$raw" ]]; then
    raw=$(_tutalink_read_arch_from_env_file "${HOME}/.ssh/tutalink/.env" || true)
  fi
  if [[ -z "$raw" ]]; then
    raw=$(_tutalink_read_arch_from_env_file "${HOME}/.ssh/be/.env" || true)
  fi
  if [[ -z "$raw" ]]; then
    raw="ALL-IN-ONE"
  fi
  raw=$(printf '%s' "$raw" | tr '[:lower:]' '[:upper:]')
  case "$raw" in
    ALL-IN-ONE|ALLINONE|ALL_IN_ONE) raw="ALL-IN-ONE" ;;
    MINI-PRODUCTION|MINIPRODUCTION|MINI_PRODUCTION) raw="MINI_PRODUCTION" ;;
    FULL-PRODUCTION|FULLPRODUCTION|FULL_PRODUCTION) raw="FULL_PRODUCTION" ;;
  esac

  TUTALINK_ARCHITECTURE="$raw"
  TUTALINK_SSH_USER="${SSH_USER:-lawsen0}"

  # Canonical IPs (override with TUTALINK_XBOX*_IP if needed)
  TUTALINK_XBOX0_IP="${TUTALINK_XBOX0_IP:-192.168.222.200}"
  TUTALINK_XBOX1_IP="${TUTALINK_XBOX1_IP:-192.168.202.201}"
  TUTALINK_XBOX2_IP="${TUTALINK_XBOX2_IP:-192.168.222.202}"
  TUTALINK_XBOX3_IP="${TUTALINK_XBOX3_IP:-192.168.202.203}"

  TUTALINK_WEB_HOSTS=()
  TUTALINK_PRIMARY_HOST=""
  TUTALINK_PRIMARY_IP=""

  case "$TUTALINK_ARCHITECTURE" in
    ALL-IN-ONE)
      # Single box: xbox0 holds HAProxy, Postgres Primary, and the web app.
      TUTALINK_WEB_HOSTS=(xbox0)
      TUTALINK_PRIMARY_HOST=xbox0
      TUTALINK_PRIMARY_IP="$TUTALINK_XBOX0_IP"
      ;;
    MINI_PRODUCTION)
      # xbox1 = DB Primary (+ HAProxy). xbox2 + xbox3 = webservers (each with local Replica).
      TUTALINK_WEB_HOSTS=(xbox2 xbox3)
      TUTALINK_PRIMARY_HOST=xbox1
      TUTALINK_PRIMARY_IP="$TUTALINK_XBOX1_IP"
      ;;
    FULL_PRODUCTION)
      echo "DEPLOY_ARCHITECTURE=FULL_PRODUCTION is not implemented for tutalink yet." >&2
      echo "Use ALL-IN-ONE or MINI_PRODUCTION." >&2
      return 2
      ;;
    *)
      echo "Unknown DEPLOY_ARCHITECTURE='$TUTALINK_ARCHITECTURE' (from env / ~/.ssh/be/.env)." >&2
      echo "Expected: ALL-IN-ONE | MINI_PRODUCTION | FULL_PRODUCTION" >&2
      return 1
      ;;
  esac
  return 0
}

tutalink_host_ip() {
  case "$1" in
    xbox0) printf '%s' "$TUTALINK_XBOX0_IP" ;;
    xbox1) printf '%s' "$TUTALINK_XBOX1_IP" ;;
    xbox2) printf '%s' "$TUTALINK_XBOX2_IP" ;;
    xbox3) printf '%s' "$TUTALINK_XBOX3_IP" ;;
    *) printf '%s' "$1" ;;
  esac
}

tutalink_ssh_target() {
  local host="$1"
  printf '%s@%s' "$TUTALINK_SSH_USER" "$(tutalink_host_ip "$host")"
}

tutalink_print_architecture() {
  tutalink_load_architecture || return $?
  echo "DEPLOY_ARCHITECTURE=$TUTALINK_ARCHITECTURE"
  echo "primary_host=$TUTALINK_PRIMARY_HOST ($TUTALINK_PRIMARY_IP)"
  echo "web_hosts=${TUTALINK_WEB_HOSTS[*]}"
  local h
  for h in "${TUTALINK_WEB_HOSTS[@]}"; do
    echo "  web $h -> $(tutalink_ssh_target "$h")"
  done
}
