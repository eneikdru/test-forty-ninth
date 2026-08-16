#!/usr/bin/env bash
set -euo pipefail

# Verification script for automated restoration jobs and recovery verification

TEST_TEMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TEST_TEMP_DIR}"' EXIT

BACKUP_DIR="${TEST_TEMP_DIR}/secondary"
SOURCE_OBJECT_STORAGE="${TEST_TEMP_DIR}/source_object_storage"
RESTORED_OBJECT_STORAGE="${TEST_TEMP_DIR}/restored_object_storage"
ALERT_LOG_FILE="${TEST_TEMP_DIR}/restore_alerts.log"

mkdir -p "${SOURCE_OBJECT_STORAGE}"
echo "Sample protocol document content pre-backup" > "${SOURCE_OBJECT_STORAGE}/protocol_202.pdf"

echo "=== Step 1: Execute Backup ==="
BACKUP_DIR="${BACKUP_DIR}" \
OBJECT_STORAGE_DIR="${SOURCE_OBJECT_STORAGE}" \
ALERT_LOG_FILE="${ALERT_LOG_FILE}" \
SIMULATE_FAILURE=false \
./scripts/backup.sh

SNAPSHOT_DIR="$(find "${BACKUP_DIR}" -mindepth 1 -maxdepth 1 -type d | sort -r | head -n 1)"

if [ -z "${SNAPSHOT_DIR}" ] || [ ! -f "${SNAPSHOT_DIR}/db/db_snapshot.sql" ]; then
    echo "FAILED: Backup snapshot failed to produce required files." >&2
    exit 1
fi

echo "=== Step 2: Execute Full Restore into Isolated Environment ==="
SNAPSHOT_DIR="${SNAPSHOT_DIR}" \
OBJECT_STORAGE_DIR="${RESTORED_OBJECT_STORAGE}" \
ALERT_LOG_FILE="${ALERT_LOG_FILE}" \
SIMULATE_FAILURE=false \
./scripts/restore.sh

echo "=== Step 3: Validate Restored Artifacts Integrity ==="
if [ ! -f "${RESTORED_OBJECT_STORAGE}/protocol_202.pdf" ]; then
    echo "FAILED: Restored object storage document missing." >&2
    exit 1
fi

ORIGINAL_HASH="$(sha256sum "${SOURCE_OBJECT_STORAGE}/protocol_202.pdf" | awk '{print $1}')"
RESTORED_HASH="$(sha256sum "${RESTORED_OBJECT_STORAGE}/protocol_202.pdf" | awk '{print $1}')"

if [ "${ORIGINAL_HASH}" != "${RESTORED_HASH}" ]; then
    echo "FAILED: Restored document hash mismatch! (Original: ${ORIGINAL_HASH}, Restored: ${RESTORED_HASH})" >&2
    exit 1
fi

echo "Integrity Verification Passed: Restored object storage files match original checksums."

echo "=== Step 4: Validate Restore Failure Handling ==="
set +e
SNAPSHOT_DIR="${SNAPSHOT_DIR}" \
OBJECT_STORAGE_DIR="${RESTORED_OBJECT_STORAGE}" \
ALERT_LOG_FILE="${ALERT_LOG_FILE}" \
SIMULATE_FAILURE=true \
./scripts/restore.sh >/dev/null 2>&1
EXIT_CODE=$?
set -e

if [ "${EXIT_CODE}" -eq 0 ]; then
    echo "FAILED: Restore script should have exited with non-zero status on simulated failure." >&2
    exit 1
fi

if [ ! -f "${ALERT_LOG_FILE}" ] || ! grep -q "RESTORE FAILURE ALERT" "${ALERT_LOG_FILE}"; then
    echo "FAILED: Restore failure alert was not raised." >&2
    exit 1
fi

echo "Test Passed: Restore alert correctly logged on failure."
echo "=== All restore verification script tests completed successfully! ==="
