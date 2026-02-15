#!/bin/bash

# Wait for containers to reach expected state with timeout
# Usage: wait-for-containers.sh <timeout_seconds> <expected_state> <name_filter1> [name_filter2] ...
#
# Examples:
#   wait-for-containers.sh 60 "exited" "initializer"
#   wait-for-containers.sh 90 "up" "ca" "orderer"
#
# Expected states:
#   - "exited" or "exited(0)" - container exited successfully
#   - "up" or "running" - container is running
#
# Returns 0 on success, 1 on timeout or failure

set -e

TIMEOUT=${1:-60}
EXPECTED_STATE=${2:-"up"}
shift 2
NAME_FILTERS=("$@")

if [ ${#NAME_FILTERS[@]} -eq 0 ]; then
    echo "❌ Error: No container name filters provided"
    echo "Usage: $0 <timeout> <expected_state> <name_filter1> [name_filter2] ..."
    exit 1
fi

# Normalize expected state
case "${EXPECTED_STATE,,}" in
    exited|exited\(0\)|exit)
        CHECK_TYPE="exited"
        ;;
    up|running)
        CHECK_TYPE="running"
        ;;
    *)
        echo "❌ Error: Unknown state '$EXPECTED_STATE'. Use 'exited' or 'up'"
        exit 1
        ;;
esac

echo "⏳ Waiting for containers (timeout: ${TIMEOUT}s, state: ${CHECK_TYPE})"
for filter in "${NAME_FILTERS[@]}"; do
    echo "   Filter: name=$filter"
done

START_TIME=$(date +%s)
ELAPSED=0

while [ $ELAPSED -lt $TIMEOUT ]; do
    ALL_READY=true

    # Build docker ps command with all filters
    FILTER_ARGS=""
    for filter in "${NAME_FILTERS[@]}"; do
        FILTER_ARGS="$FILTER_ARGS --filter name=$filter"
    done

    # Get container list
    CONTAINERS=$(docker ps -a $FILTER_ARGS --format "{{.Names}}\t{{.Status}}" 2>/dev/null || echo "")

    if [ -z "$CONTAINERS" ]; then
        echo "   ⏳ No containers found yet (${ELAPSED}s/${TIMEOUT}s)"
        ALL_READY=false
    else
        # Count containers by state
        TOTAL_COUNT=$(echo "$CONTAINERS" | wc -l)

        case "$CHECK_TYPE" in
            exited)
                READY_COUNT=$(echo "$CONTAINERS" | grep -c "Exited (0)" || echo "0")
                NOT_READY=$(echo "$CONTAINERS" | grep -v "Exited (0)" || echo "")
                ;;
            running)
                READY_COUNT=$(echo "$CONTAINERS" | grep -c "Up" || echo "0")
                NOT_READY=$(echo "$CONTAINERS" | grep -v "Up" || echo "")
                ;;
        esac

        if [ "$READY_COUNT" -eq "$TOTAL_COUNT" ] && [ "$TOTAL_COUNT" -gt 0 ]; then
            echo "✓ All $TOTAL_COUNT containers are ready (state: $CHECK_TYPE)"
            echo ""
            echo "Container status:"
            echo "$CONTAINERS" | awk '{printf "   %-40s %s\n", $1, $2" "$3" "$4" "$5}'
            return 0
        else
            echo "   ⏳ $READY_COUNT/$TOTAL_COUNT ready (${ELAPSED}s/${TIMEOUT}s)"
            if [ -n "$NOT_READY" ]; then
                echo "$NOT_READY" | head -3 | awk '{printf "      - %-35s %s\n", $1, $2" "$3" "$4}'
            fi
            ALL_READY=false
        fi
    fi

    sleep 2
    CURRENT_TIME=$(date +%s)
    ELAPSED=$((CURRENT_TIME - START_TIME))
done

echo ""
echo "❌ Timeout after ${TIMEOUT}s waiting for containers"
echo ""
echo "Current container states:"
docker ps -a $FILTER_ARGS --format "table {{.Names}}\t{{.Status}}\t{{.Image}}" 2>/dev/null || echo "No containers found"
return 1