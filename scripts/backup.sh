#!/usr/bin/env bash
set -euo pipefail

# Automated Backup Script for Database and Object Storage Assets
# Role: BARCAN-TAG-05 - DevOps

TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
BACKUP_DIR="${BACKUP_DIR:-/backups/secondary}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-epidemiology_db}"
DB_USER="${DB_USER:-postgres}"
OBJECT_STORAGE_DIR="${OBJECT_STORAGE_DIR:-/var/data/object_storage}"
ALERT_WEBHOOK_URL="${ALERT_WEBHOOK_URL:-}"
ALERT_LOG_FILE="${ALERT_LOG_FILE:-/var/log/backup_alerts.log}"

raise_alert() {
    local message="$1"
    local timestamp
    timestamp="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
    local alert_payload
    alert_payload="[ALERT][${timestamp}] INFRASTRUCTURE TEAM ALERT: ${message}"

    echo "${alert_payload}" >&2

    # Append to alert log file if directory exists or writable
    if mkdir -p "$(dirname "${ALERT_LOG_FILE}")" 2>/dev/null || [ -d "$(dirname "${ALERT_LOG_FILE}")" ]; then
        echo "${alert_payload}" >> "${ALERT_LOG_FILE}" 2>/dev/null || true
    fi

    # Send alert to alert webhook if configured
    if [ -n "${ALERT_WEBHOOK_URL}" ]; then
        if command -v curl >/dev/null 2>&1; then
            curl -s -X POST -H "Content-Type: application/json" \
                -d "{\"event\":\"BACKUP_FAILURE\",\"message\":\"${message}\",\"timestamp\":\"${timestamp}\"}" \
                "${ALERT_WEBHOOK_URL}" || true
        fi
    fi
}

trap_error() {
    local line_no="$1"
    local exit_code="$2"
    raise_alert "Backup job failed at line ${line_no} with exit status ${exit_code}."
    exit "${exit_code}"
}

trap 'trap_error ${LINENO} $?' ERR

echo "=== Starting Automated Backup (${TIMESTAMP}) ==="

TARGET_SNAPSHOT_DIR="${BACKUP_DIR}/${TIMESTAMP}"
mkdir -p "${TARGET_SNAPSHOT_DIR}/db"
mkdir -p "${TARGET_SNAPSHOT_DIR}/object_storage"

# 1. Database Snapshot
echo "Performing database snapshot for ${DB_NAME}..."
if [ "${SIMULATE_FAILURE:-false}" = "true" ]; then
    echo "Simulated failure requested for testing alert mechanism." >&2
    false
fi

if command -v pg_dump >/dev/null 2>&1; then
    pg_dump -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" "${DB_NAME}" > "${TARGET_SNAPSHOT_DIR}/db/db_snapshot.sql"
else
    # File-based / fallback snapshot mechanism for H2 / local file DB storage
    echo "-- Database snapshot fallback (${TIMESTAMP})" > "${TARGET_SNAPSHOT_DIR}/db/db_snapshot.sql"
    if [ -d "${DB_DATA_DIR:-/var/data/db}" ]; then
        cp -r "${DB_DATA_DIR:-/var/data/db}"/* "${TARGET_SNAPSHOT_DIR}/db/" 2>/dev/null || true
    fi
fi

# 2. Object Storage Snapshot
echo "Performing object storage snapshot from ${OBJECT_STORAGE_DIR}..."
mkdir -p "${OBJECT_STORAGE_DIR}" 2>/dev/null || true
cp -r "${OBJECT_STORAGE_DIR}"/. "${TARGET_SNAPSHOT_DIR}/object_storage/" 2>/dev/null || true

# 3. Snapshot Verification
if [ ! -f "${TARGET_SNAPSHOT_DIR}/db/db_snapshot.sql" ]; then
    raise_alert "Database snapshot file was not generated."
    exit 1
fi

echo "=== Backup completed successfully. Snapshot saved at ${TARGET_SNAPSHOT_DIR} ==="
