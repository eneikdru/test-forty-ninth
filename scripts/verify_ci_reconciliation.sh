#!/usr/bin/env bash
set -euo pipefail

# CI Status Reconciliation Verification Script
# Role: BARCAN-TAG-05 - DevOps

echo "=== Starting CI Status Reconciliation Verification ==="

echo "1. Running backend task state reconciliation unit and integration tests..."
if mvn test -Dtest=TaskStateSyncServiceTest,TaskStateSyncIntegrationTest; then
    echo "✓ Task state reconciliation backend tests passed successfully."
else
    echo "✗ Task state reconciliation tests failed." >&2
    exit 1
fi

echo "=== All CI status reconciliation verification checks passed! ==="
