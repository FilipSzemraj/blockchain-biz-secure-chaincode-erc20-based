#!/bin/bash

# Wait for files or directories to exist with timeout
# Usage: wait-for-files.sh <timeout_seconds> <path1> [path2] [path3] ...
#
# Examples:
#   wait-for-files.sh 30 "_shared_certs/furnituresmakers-msp"
#   wait-for-files.sh 60 "_shared_certs/furnituresmakers-msp" "_shared_certs/woodsupply-msp" "_shared_certs/yachtsales-msp"
#   wait-for-files.sh 45 "_config_files/configtx/output/genesis_block_YFW.pb"
#
# Returns 0 on success, 1 on timeout or failure

set -e

TIMEOUT=${1:-60}
shift
PATHS=("$@")

if [ ${#PATHS[@]} -eq 0 ]; then
    echo "❌ Error: No paths provided"
    echo "Usage: $0 <timeout> <path1> [path2] ..."
    exit 1
fi

echo "⏳ Waiting for files/directories (timeout: ${TIMEOUT}s)"
for path in "${PATHS[@]}"; do
    echo "   Path: $path"
done

START_TIME=$(date +%s)
ELAPSED=0

while [ $ELAPSED -lt $TIMEOUT ]; do
    ALL_EXIST=true
    MISSING_PATHS=()

    for path in "${PATHS[@]}"; do
        if [ ! -e "$path" ]; then
            ALL_EXIST=false
            MISSING_PATHS+=("$path")
        fi
    done

    if $ALL_EXIST; then
        echo "✓ All ${#PATHS[@]} paths exist"
        echo ""
        echo "Verification:"
        for path in "${PATHS[@]}"; do
            if [ -f "$path" ]; then
                SIZE=$(du -h "$path" | cut -f1)
                echo "   [FILE] $path ($SIZE)"
            elif [ -d "$path" ]; then
                COUNT=$(find "$path" -type f 2>/dev/null | wc -l)
                echo "   [DIR]  $path ($COUNT files)"
            fi
        done
        return 0
    else
        READY_COUNT=$((${#PATHS[@]} - ${#MISSING_PATHS[@]}))
        echo "   ⏳ $READY_COUNT/${#PATHS[@]} exist (${ELAPSED}s/${TIMEOUT}s)"

        # Show first 3 missing paths
        for i in "${!MISSING_PATHS[@]}"; do
            if [ $i -lt 3 ]; then
                echo "      Missing: ${MISSING_PATHS[$i]}"
            fi
        done
    fi

    sleep 2
    CURRENT_TIME=$(date +%s)
    ELAPSED=$((CURRENT_TIME - START_TIME))
done

echo ""
echo "❌ Timeout after ${TIMEOUT}s waiting for paths"
echo ""
echo "Missing paths:"
for path in "${MISSING_PATHS[@]}"; do
    echo "   - $path"
done
return 1