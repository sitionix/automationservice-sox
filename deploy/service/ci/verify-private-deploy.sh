#!/usr/bin/env bash

set -euo pipefail

require_env() {
  local variable_name="$1"
  if [[ -z "${!variable_name:-}" ]]; then
    echo "Missing required environment variable: ${variable_name}" >&2
    exit 1
  fi
}

wait_for_up_status() {
  local url="$1"
  local label="$2"
  local response_file
  response_file="$(mktemp)"

  for _ in $(seq 1 20); do
    if curl -sS -o "${response_file}" -w '%{http_code}' "${url}" | grep -qx '200'; then
      if python3 - "${response_file}" <<'PY'
import json
import sys

with open(sys.argv[1], encoding="utf-8") as handle:
    payload = json.load(handle)

if payload.get("status") != "UP":
    raise SystemExit(1)
PY
      then
        rm -f "${response_file}"
        return 0
      fi
    fi
    sleep 1
  done

  echo "${label} did not become UP at ${url}" >&2
  cat "${response_file}" >&2 || true
  rm -f "${response_file}"
  exit 1
}

for variable_name in \
  DEPLOY_VM_HOST \
  DEPLOY_VM_USER \
  DEPLOY_VM_SSH_PRIVATE_KEY \
  SITIONIX_RELEASE_ID; do
  require_env "${variable_name}"
done

key_path="$(mktemp)"
tunnel_pid=""
local_port="${SITIONIX_PRIVATE_TUNNEL_PORT:-19083}"

cleanup() {
  if [[ -n "${tunnel_pid}" ]]; then
    kill "${tunnel_pid}" 2>/dev/null || true
    wait "${tunnel_pid}" 2>/dev/null || true
  fi
  rm -f "${key_path}"
}

trap cleanup EXIT

printf '%s\n' "${DEPLOY_VM_SSH_PRIVATE_KEY}" > "${key_path}"
chmod 0600 "${key_path}"

ssh \
  -i "${key_path}" \
  -o IdentitiesOnly=yes \
  -o StrictHostKeyChecking=accept-new \
  -o ExitOnForwardFailure=yes \
  -o ConnectTimeout=15 \
  -o ServerAliveInterval=15 \
  -o ServerAliveCountMax=3 \
  -N \
  -L "${local_port}:127.0.0.1:9083" \
  -p "${DEPLOY_VM_PORT:-22}" \
  "${DEPLOY_VM_USER}@${DEPLOY_VM_HOST}" &
tunnel_pid=$!

for _ in $(seq 1 20); do
  if bash -c ">/dev/tcp/127.0.0.1/${local_port}" 2>/dev/null; then
    break
  fi
  sleep 1
done

if ! bash -c ">/dev/tcp/127.0.0.1/${local_port}" 2>/dev/null; then
  echo "Automation SSH tunnel did not become ready on 127.0.0.1:${local_port}" >&2
  exit 1
fi

readiness_url="http://127.0.0.1:${local_port}/atmssox/actuator/health/readiness"
health_url="http://127.0.0.1:${local_port}/atmssox/actuator/health"

wait_for_up_status "${readiness_url}" "Automation private readiness"
wait_for_up_status "${health_url}" "Automation private health"

echo "Automation private smoke passed for release ${SITIONIX_RELEASE_ID}"
