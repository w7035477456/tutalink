#!/usr/bin/env bash
# Curl health on every host in deploy/hosts.txt (:4100).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEPLOY_SSH="${HOME}/code/main/scripts/deploy-ssh-mac.sh"

resolve() {
  case "$1" in
    xbox3) echo "192.168.202.203" ;;
    xbox4) echo "192.168.202.204" ;;
    xbox1) echo "192.168.202.201" ;;
    *) echo "$1" ;;
  esac
}

while IFS= read -r host || [[ -n "$host" ]]; do
  [[ -z "$host" || "$host" =~ ^# ]] && continue
  ip=$(resolve "$host")
  echo -n "$host ($ip): "
  if [[ -x "$DEPLOY_SSH" ]]; then
    "$DEPLOY_SSH" "lawsen0@$ip" 'curl -fsS http://localhost:4100/tutalink/actuator/health' || echo FAIL
  else
    curl -fsS "http://${ip}:4100/tutalink/actuator/health" || echo FAIL
  fi
  echo
done < "$ROOT/deploy/hosts.txt"
