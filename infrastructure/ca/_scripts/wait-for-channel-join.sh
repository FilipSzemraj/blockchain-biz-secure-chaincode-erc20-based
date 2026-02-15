#!/bin/bash

# Wait for ALL orderers to join a channel via osnadmin API
# Usage: wait-for-channel-join.sh <timeout> <channel_name>
#
# Examples:
#   wait-for-channel-join.sh 90 yfw-channel
#   wait-for-channel-join.sh 120 mychannel
#
# Returns 0 if all orderers joined, 1 on timeout or failure

set -e

TIMEOUT=${1:-90}
CHANNEL_NAME=${2:-"yfw-channel"}

if [ -z "$CHANNEL_NAME" ]; then
    echo "❌ Error: Channel name required"
    echo "Usage: $0 <timeout> <channel_name>"
    exit 1
fi

# All organizations to check
ORGS=("furnituresmakers" "woodsupply" "yachtsales")

echo "⏳ Waiting for all orderers to join channel (timeout: ${TIMEOUT}s)"
echo "   Channel: $CHANNEL_NAME"
echo "   Checking: ${ORGS[@]}"
echo ""

START_TIME=$(date +%s)
ELAPSED=0

# Track which orgs have joined
declare -A JOINED_STATUS
for org in "${ORGS[@]}"; do
    JOINED_STATUS[$org]=0
done

while [ $ELAPSED -lt $TIMEOUT ]; do
    ALL_JOINED=true
    JOINED_COUNT=0

    # Check each orderer
    for org in "${ORGS[@]}"; do
        # Skip if already confirmed joined
        if [ "${JOINED_STATUS[$org]}" -eq 1 ]; then
            JOINED_COUNT=$((JOINED_COUNT + 1))
            continue
        fi

        ADMIN_CONTAINER="admin.${org}.com"

        # Query orderer's channel list via osnadmin API
        CHANNEL_CHECK=$(docker exec "$ADMIN_CONTAINER" bash -c "
            osnadmin channel list \
                -o orderer0.\$ORG_NAME.com:9443 \
                --ca-file \$OSN_TLS_CA_ROOT_CERT \
                --client-cert \$ADMIN_TLS_SIGN_CERT \
                --client-key \$ADMIN_TLS_PRIVATE_KEY
        " 2>&1 || echo "ERROR")

        # Check if channel appears in the list
        if echo "$CHANNEL_CHECK" | grep -q "\"name\": \"$CHANNEL_NAME\""; then
            JOINED_STATUS[$org]=1
            JOINED_COUNT=$((JOINED_COUNT + 1))
            echo "   ✓ $org orderer joined"
        else
            ALL_JOINED=false
        fi
    done

    # Check if all joined
    if $ALL_JOINED; then
        echo ""
        echo "✓ All ${#ORGS[@]} orderers joined channel '$CHANNEL_NAME' successfully"

        # Show confirmation for all orgs
        echo ""
        echo "Final verification:"
        for org in "${ORGS[@]}"; do
            docker exec "admin.${org}.com" bash -c "
                osnadmin channel list \
                    -o orderer0.\$ORG_NAME.com:9443 \
                    --ca-file \$OSN_TLS_CA_ROOT_CERT \
                    --client-cert \$ADMIN_TLS_SIGN_CERT \
                    --client-key \$ADMIN_TLS_PRIVATE_KEY
            " 2>&1 | grep -E "\"name\": \"$CHANNEL_NAME\"" | sed "s/^/   [$org] /" || echo "   [$org] ⚠ Could not verify"
        done

        # Show Raft leader (if consensus has elected one)
        echo ""
        echo "Raft consensus status:"
        LEADER_FOUND=false
        for org in "${ORGS[@]}"; do
            LEADER_LOG=$(docker logs "orderer0.${org}.com" 2>&1 | grep "became leader" | tail -1 || echo "")
            if [ -n "$LEADER_LOG" ]; then
                echo "   ★ $org is Raft leader"
                LEADER_FOUND=true
            else
                echo "   - $org is follower"
            fi
        done

        if ! $LEADER_FOUND; then
            echo "   ⚠ No leader elected yet (consensus may still be initializing)"
        fi

        return 0
    fi

    # Show progress
    echo "   ⏳ $JOINED_COUNT/${#ORGS[@]} orderers joined (${ELAPSED}s/${TIMEOUT}s)"

    # Show which orgs are still pending
    for org in "${ORGS[@]}"; do
        if [ "${JOINED_STATUS[$org]}" -eq 0 ]; then
            echo "      - $org: waiting..."
        fi
    done

    sleep 3
    CURRENT_TIME=$(date +%s)
    ELAPSED=$((CURRENT_TIME - START_TIME))
    echo ""
done

echo ""
echo "❌ Timeout after ${TIMEOUT}s waiting for all orderers to join"
echo ""
echo "Join status:"
for org in "${ORGS[@]}"; do
    if [ "${JOINED_STATUS[$org]}" -eq 1 ]; then
        echo "   ✓ $org - joined"
    else
        echo "   ✗ $org - NOT joined"
    fi
done

echo ""
echo "Troubleshooting:"
echo "  1. Check admin logs:"
for org in "${ORGS[@]}"; do
    if [ "${JOINED_STATUS[$org]}" -eq 0 ]; then
        echo "     docker logs admin.${org}.com"
    fi
done
echo "  2. Verify genesis block: ls -lh _config_files/configtx/output/genesis_block_YFW.pb"
echo "  3. Check orderer logs for failed orgs"

return 1