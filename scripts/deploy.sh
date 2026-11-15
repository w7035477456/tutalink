#!/usr/bin/env bash
# Rolling deploy of tutalink to web hosts selected by DEPLOY_ARCHITECTURE.
#   ALL-IN-ONE      → xbox0
#   MINI_PRODUCTION → xbox2 + xbox3  (Primary stays on xbox1; Flyway runs there via app start)
#   FULL_PRODUCTION → refused until implemented
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/architecture.sh
source "$ROOT/scripts/lib/architecture.sh"

tutalink_load_architecture

DEPLOY_SSH="${DEPLOY_SSH:-$ROOT/../main/scripts/deploy-ssh-mac.sh}"
if [[ ! -x "$DEPLOY_SSH" ]]; then
  DEPLOY_SSH="${HOME}/code/main/scripts/deploy-ssh-mac.sh"
fi
if [[ ! -x "$DEPLOY_SSH" ]]; then
  echo "deploy-ssh-mac.sh not found — expected at ~/code/main/scripts/deploy-ssh-mac.sh" >&2
  exit 1
fi

echo "tutalink deploy: DEPLOY_ARCHITECTURE=$TUTALINK_ARCHITECTURE"
echo "  Primary (DB/Flyway target): $TUTALINK_PRIMARY_HOST ($TUTALINK_PRIMARY_IP)"
echo "  Web hosts: ${TUTALINK_WEB_HOSTS[*]}"

SUMMARY=()

for host in "${TUTALINK_WEB_HOSTS[@]}"; do
  TARGET="$(tutalink_ssh_target "$host")"
  echo "=== Deploying to $host ($TARGET) ==="

  "$DEPLOY_SSH" "$TARGET" 'cd ~/code/tutalink && git fetch && git reset --hard origin/main'
  "$DEPLOY_SSH" "$TARGET" 'cd ~/code/tutalink && ./mvnw -q -DskipTests package'
  "$DEPLOY_SSH" "$TARGET" 'cd ~/code/tutalink && (pm2 describe tutalinkPM2Process >/dev/null 2>&1 && pm2 restart tutalinkPM2Process || pm2 startOrReload ecosystem.config.js)'

  ok=0
  for _i in $(seq 1 90); do
    if "$DEPLOY_SSH" "$TARGET" 'curl -fs http://localhost:4100/tutalink/actuator/health' | grep -q UP; then
      ok=1
      break
    fi
    sleep 1
  done
  if [[ "$ok" -ne 1 ]]; then
    echo "Health check FAILED on $host — aborting deploy" >&2
    "$DEPLOY_SSH" "$TARGET" 'pm2 logs tutalinkPM2Process --lines 50 --nostream' || true
    exit 1
  fi
  hash=$("$DEPLOY_SSH" "$TARGET" 'cd ~/code/tutalink && git rev-parse --short HEAD')
  SUMMARY+=("$host $hash")
  echo "=== $host OK ($hash) ==="
done

echo "---- Deploy summary ($TUTALINK_ARCHITECTURE) ----"
printf '%s\n' "${SUMMARY[@]}"
