#!/usr/bin/env bash
set -euo pipefail

# Verification script for automated backup jobs and alerting mechanism

TEST_TEMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TEST_TEMP_DIR}"' EXIT

BACKUP_DIR="${TEST_TEMP_DIR}/secondary"
OBJECT_STORAGE_DIR="${TEST_TEMP_DIR}/object_storage"
ALERT_LOG_FILE="${TEST_TEMP_DIR}/alerts.log"

mkdir -p "${OBJECT_STORAGE_DIR}"
echo "Sample protocol PDF content" > "${OBJECT_STORAGE_DIR}/protocol_101.pdf"

echo "=== Test 1: Successful Backup Execution ==="
BACKUP_DIR="${BACKUP_DIR}" \
OBJECT_STORAGE_DIR="${OBJECT_STORAGE_DIR}" \
ALERT_LOG_FILE="${ALERT_LOG_FILE}" \
SIMULATE_FAILURE=false \
./scripts/backup.sh

# Verify snapshot contents
SNAPSHOT_DIR="$(find "${BACKUP_DIR}" -mindepth 1 -maxdepth 1 -type d | head -n 1)"
if [ -z "${SNAPSHOT_DIR}" ]; then
    echo "FAILED: Snapshot directory was not created." >&2
    exit 1
fi

if [ ! -f "${SNAPSHOT_DIR}/db/db_snapshot.sql" ]; then
    echo "FAILED: Database snapshot file missing." >&2
    exit 1
fi

if [ ! -f "${SNAPSHOT_DIR}/object_storage/protocol_101.pdf" ]; then
    echo "FAILED: Object storage snapshot file missing." >&2
    exit 1
fi

echo "Test 1 Passed: Complete snapshot created successfully."

echo "=== Test 2: Backup Failure and Alert Raising ==="
set +e
BACKUP_DIR="${BACKUP_DIR}" \
OBJECT_STORAGE_DIR="${OBJECT_STORAGE_DIR}" \
ALERT_LOG_FILE="${ALERT_LOG_FILE}" \
SIMULATE_FAILURE=true \
./scripts/backup.sh >/dev/null 2>&1
EXIT_CODE=$?
set -e

if [ "${EXIT_CODE}" -eq 0 ]; then
    echo "FAILED: Backup script should have exited with non-zero status on failure." >&2
    exit 1
fi

if [ ! -f "${ALERT_LOG_FILE}" ] || ! grep -q "INFRASTRUCTURE TEAM ALERT" "${ALERT_LOG_FILE}"; then
    echo "FAILED: Alert was not raised to infrastructure team log." >&2
    exit 1
fi

echo "Test 2 Passed: Alert successfully raised upon backup job failure."
echo "=== All backup verification tests passed! ==="
