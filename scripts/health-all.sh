#!/usr/bin/env bash
# Curl tutalink health on every web host for the current DEPLOY_ARCHITECTURE.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/architecture.sh
source "$ROOT/scripts/lib/architecture.sh"

tutalink_load_architecture

DEPLOY_SSH="${HOME}/code/main/scripts/deploy-ssh-mac.sh"

echo "tutalink health: DEPLOY_ARCHITECTURE=$TUTALINK_ARCHITECTURE"

for host in "${TUTALINK_WEB_HOSTS[@]}"; do
  ip="$(tutalink_host_ip "$host")"
  echo -n "$host ($ip): "
  if [[ -x "$DEPLOY_SSH" ]]; then
    "$DEPLOY_SSH" "$(tutalink_ssh_target "$host")" \
      'curl -fsS http://localhost:4100/tutalink/actuator/health' || echo FAIL
  else
    curl -fsS "http://${ip}:4100/tutalink/actuator/health" || echo FAIL
  fi
  echo
done
