#!/usr/bin/env bash
# Rolling deploy to every host in deploy/hosts.txt (Mac → Ubuntu via hardened SSH).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
HOSTS_FILE="$ROOT/deploy/hosts.txt"
DEPLOY_SSH="${DEPLOY_SSH:-$ROOT/../main/scripts/deploy-ssh-mac.sh}"
if [[ ! -x "$DEPLOY_SSH" ]]; then
  DEPLOY_SSH="${HOME}/code/main/scripts/deploy-ssh-mac.sh"
fi

SSH_USER="${SSH_USER:-lawsen0}"
SUMMARY=()

while IFS= read -r host || [[ -n "$host" ]]; do
  [[ -z "$host" || "$host" =~ ^# ]] && continue
  echo "=== Deploying to $host ==="
  TARGET="${SSH_USER}@${host}"
  # Resolve xboxN → IP if needed: prefer Host entries; fall back to known map
  case "$host" in
    xbox3) TARGET="${SSH_USER}@192.168.202.203" ;;
    xbox4) TARGET="${SSH_USER}@192.168.202.204" ;;
    xbox1) TARGET="${SSH_USER}@192.168.202.201" ;;
  esac

  "$DEPLOY_SSH" "$TARGET" 'cd ~/code/tutalink && git fetch && git reset --hard origin/main'
  "$DEPLOY_SSH" "$TARGET" 'cd ~/code/tutalink && ./mvnw -q -DskipTests package'
  "$DEPLOY_SSH" "$TARGET" 'cd ~/code/tutalink && (pm2 describe tutalinkPM2Process >/dev/null 2>&1 && pm2 restart tutalinkPM2Process || pm2 startOrReload ecosystem.config.js)'

  ok=0
  for i in $(seq 1 90); do
    if "$DEPLOY_SSH" "$TARGET" 'curl -fs http://localhost:4100/tutalink/actuator/health' | grep -q UP; then
      ok=1
      break
    fi
    sleep 1
  done
  if [[ "$ok" -ne 1 ]]; then
    echo "Health check FAILED on $host — aborting deploy"
    "$DEPLOY_SSH" "$TARGET" 'pm2 logs tutalinkPM2Process --lines 50 --nostream' || true
    exit 1
  fi
  hash=$("$DEPLOY_SSH" "$TARGET" 'cd ~/code/tutalink && git rev-parse --short HEAD')
  SUMMARY+=("$host $hash")
  echo "=== $host OK ($hash) ==="
done < "$HOSTS_FILE"

echo "---- Deploy summary ----"
printf '%s\n' "${SUMMARY[@]}"
