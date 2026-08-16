#!/usr/bin/env bash
set -euo pipefail

# Automated Data Restoration Script for Database and Object Storage Assets
# Role: BARCAN-TAG-06 - QA Verification

BACKUP_DIR="${BACKUP_DIR:-/backups/secondary}"
SNAPSHOT_DIR="${SNAPSHOT_DIR:-}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-epidemiology_db}"
DB_USER="${DB_USER:-postgres}"
OBJECT_STORAGE_DIR="${OBJECT_STORAGE_DIR:-/var/data/object_storage}"
ALERT_LOG_FILE="${ALERT_LOG_FILE:-/var/log/backup_alerts.log}"

raise_alert() {
    local message="$1"
    local timestamp
    timestamp="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
    local alert_payload
    alert_payload="[ALERT][${timestamp}] RESTORE FAILURE ALERT: ${message}"

    echo "${alert_payload}" >&2

    if mkdir -p "$(dirname "${ALERT_LOG_FILE}")" 2>/dev/null; then
        echo "${alert_payload}" >> "${ALERT_LOG_FILE}"
    fi
}

trap_error() {
    local line_no="$1"
    local exit_code="$2"
    raise_alert "Restore job failed at line ${line_no} with exit status ${exit_code}."
    exit "${exit_code}"
}

trap 'trap_error ${LINENO} $?' ERR

echo "=== Starting Data Restoration Procedure ==="

if [ -z "${SNAPSHOT_DIR}" ]; then
    if [ -d "${BACKUP_DIR}" ]; then
        SNAPSHOT_DIR="$(find "${BACKUP_DIR}" -mindepth 1 -maxdepth 1 -type d | sort -r | head -n 1)"
    fi
fi

if [ -z "${SNAPSHOT_DIR}" ] || [ ! -d "${SNAPSHOT_DIR}" ]; then
    raise_alert "Valid snapshot directory not found (SNAPSHOT_DIR=${SNAPSHOT_DIR})."
    exit 1
fi

echo "Restoring from snapshot directory: ${SNAPSHOT_DIR}"

if [ "${SIMULATE_FAILURE:-false}" = "true" ]; then
    echo "Simulated failure requested for testing restore alert mechanism." >&2
    false
fi

# 1. Restore Database
echo "Restoring database into target..."
if [ -f "${SNAPSHOT_DIR}/db/db_snapshot.sql" ]; then
    if command -v psql >/dev/null 2>&1; then
        psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" < "${SNAPSHOT_DIR}/db/db_snapshot.sql"
    else
        echo "Fallback DB restore: psql not found or H2/file-based DB restoration."
        if [ -d "${DB_DATA_DIR:-/var/data/db}" ]; then
            mkdir -p "${DB_DATA_DIR:-/var/data/db}"
            cp -r "${SNAPSHOT_DIR}/db"/* "${DB_DATA_DIR:-/var/data/db}/" 2>/dev/null || true
        fi
    fi
else
    raise_alert "Database snapshot file missing in ${SNAPSHOT_DIR}/db."
    exit 1
fi

# 2. Restore Object Storage
echo "Restoring object storage to ${OBJECT_STORAGE_DIR}..."
if [ -d "${SNAPSHOT_DIR}/object_storage" ]; then
    mkdir -p "${OBJECT_STORAGE_DIR}"
    cp -r "${SNAPSHOT_DIR}/object_storage"/. "${OBJECT_STORAGE_DIR}/"
else
    raise_alert "Object storage snapshot directory missing in ${SNAPSHOT_DIR}."
    exit 1
fi

echo "=== Data Restoration Completed Successfully from ${SNAPSHOT_DIR} ==="
