#!/bin/bash

# Wait for anchor peers to complete full startup sequence:
# 1. Fetch channel genesis block from orderer
# 2. Join yfw-channel
# 3. Package and install chaincode (can take 5-10 minutes on first run!)
# 4. Approve chaincode for organization
#
# Usage: wait-for-peers.sh <timeout> <channel_name>
#
# Examples:
#   wait-for-peers.sh 600 yfw-channel  # 10 minutes for chaincode install
#
# Returns 0 if all peers ready, 1 on timeout or failure

set -e

TIMEOUT=${1:-600}  # Default 10 minutes (chaincode install can be slow!)
CHANNEL_NAME=${2:-"yfw-channel"}

ORGS=("furnituresmakers" "woodsupply" "yachtsales")

echo "⏳ Waiting for anchor peers to complete startup (timeout: ${TIMEOUT}s)"
echo "   Channel: $CHANNEL_NAME"
echo "   Peers: peer0.{${ORGS[0]},${ORGS[1]},${ORGS[2]}}.com"
echo ""
echo "Expected sequence for each peer:"
echo "   1. Fetch channel genesis block"
echo "   2. Join channel"
echo "   3. Package chaincode"
echo "   4. Install chaincode (⚠ can take 5-10 min on first run due to Gradle build)"
echo "   5. Approve chaincode"
echo ""

START_TIME=$(date +%s)

# Step 1: Wait for containers to be up
echo "[1/5] Waiting for peer containers to be running..."
source "$(dirname "$0")/wait-for-containers.sh" 60 "up" "peer0"
if [ $? -ne 0 ]; then
    echo "❌ Peer containers failed to start"
    return 1
fi
echo "✓ All peer containers are running"
echo ""

# Step 2: Check each peer joined the channel
echo "[2/5] Verifying peers joined channel '$CHANNEL_NAME'..."

declare -A CHANNEL_JOINED
for org in "${ORGS[@]}"; do
    CHANNEL_JOINED[$org]=0
done

CHECK_START=$(date +%s)
CHECK_TIMEOUT=90

while [ $(($(date +%s) - CHECK_START)) -lt $CHECK_TIMEOUT ]; do
    ALL_JOINED=true

    for org in "${ORGS[@]}"; do
        if [ "${CHANNEL_JOINED[$org]}" -eq 1 ]; then
            continue
        fi

        PEER_CONTAINER="peer0.${org}.com"

        # Check peer logs for channel join success
        JOIN_CHECK=$(docker logs "$PEER_CONTAINER" 2>&1 | grep -E "(Joining channel|Successfully joined)" || echo "")

        if [ -n "$JOIN_CHECK" ]; then
            # Verify with peer command
            CHANNEL_LIST=$(docker exec "$PEER_CONTAINER" peer channel list 2>&1 || echo "")
            if echo "$CHANNEL_LIST" | grep -q "$CHANNEL_NAME"; then
                CHANNEL_JOINED[$org]=1
                echo "   ✓ $org peer joined $CHANNEL_NAME"
            else
                ALL_JOINED=false
            fi
        else
            ALL_JOINED=false
        fi
    done

    if $ALL_JOINED; then
        break
    fi

    sleep 3
done

JOINED_COUNT=0
for org in "${ORGS[@]}"; do
    JOINED_COUNT=$((JOINED_COUNT + ${CHANNEL_JOINED[$org]}))
done

if [ $JOINED_COUNT -ne ${#ORGS[@]} ]; then
    echo ""
    echo "❌ Not all peers joined channel:"
    for org in "${ORGS[@]}"; do
        if [ "${CHANNEL_JOINED[$org]}" -eq 0 ]; then
            echo "   ✗ $org - NOT joined"
            echo "     Logs: docker logs peer0.${org}.com | grep -A5 'Joining channel'"
        fi
    done
    return 1
fi

echo "✓ All peers joined channel"
echo ""

# Step 3: Monitor chaincode packaging
echo "[3/5] Monitoring chaincode packaging..."

declare -A CHAINCODE_PACKAGED
for org in "${ORGS[@]}"; do
    CHAINCODE_PACKAGED[$org]=0
done

CHECK_START=$(date +%s)
CHECK_TIMEOUT=60

while [ $(($(date +%s) - CHECK_START)) -lt $CHECK_TIMEOUT ]; do
    ALL_PACKAGED=true

    for org in "${ORGS[@]}"; do
        if [ "${CHAINCODE_PACKAGED[$org]}" -eq 1 ]; then
            continue
        fi

        PEER_CONTAINER="peer0.${org}.com"
        PACKAGE_CHECK=$(docker logs "$PEER_CONTAINER" 2>&1 | grep "Packaging chaincode" || echo "")

        if [ -n "$PACKAGE_CHECK" ]; then
            CHAINCODE_PACKAGED[$org]=1
            echo "   ✓ $org packaged chaincode"
        else
            ALL_PACKAGED=false
        fi
    done

    if $ALL_PACKAGED; then
        break
    fi

    sleep 2
done

echo "✓ Chaincode packaging initiated"
echo ""

# Step 4: Monitor chaincode installation (this is the SLOW step!)
echo "[4/5] Monitoring chaincode installation (this may take 5-10 minutes on first run)..."
echo "   ⚠ Gradle will download dependencies and compile Java chaincode inside fabric-javaenv"
echo ""

declare -A CHAINCODE_INSTALLED
for org in "${ORGS[@]}"; do
    CHAINCODE_INSTALLED[$org]=0
done

CHECK_START=$(date +%s)
# Use remaining timeout or minimum 300s for chaincode install
REMAINING=$((TIMEOUT - ($(date +%s) - START_TIME)))
CHECK_TIMEOUT=$((REMAINING > 300 ? REMAINING : 300))

LAST_STATUS_TIME=$CHECK_START
STATUS_INTERVAL=10  # Show status every 10 seconds

while [ $(($(date +%s) - CHECK_START)) -lt $CHECK_TIMEOUT ]; do
    ALL_INSTALLED=true
    CURRENT_TIME=$(date +%s)
    ELAPSED=$((CURRENT_TIME - CHECK_START))

    for org in "${ORGS[@]}"; do
        if [ "${CHAINCODE_INSTALLED[$org]}" -eq 1 ]; then
            continue
        fi

        PEER_CONTAINER="peer0.${org}.com"

        # Check for installation success message
        INSTALL_CHECK=$(docker logs "$PEER_CONTAINER" 2>&1 | grep -E "(Chaincode installed successfully|identifier:)" || echo "")

        if [ -n "$INSTALL_CHECK" ]; then
            # Extract package ID if available
            PACKAGE_ID=$(echo "$INSTALL_CHECK" | grep -oP '(?<=identifier: ).*' | head -1 || echo "")

            CHAINCODE_INSTALLED[$org]=1
            if [ -n "$PACKAGE_ID" ]; then
                echo "   ✓ $org installed chaincode (package: ${PACKAGE_ID:0:20}...)"
            else
                echo "   ✓ $org installed chaincode"
            fi
        else
            ALL_INSTALLED=false

            # Show build progress if we see Gradle output
            GRADLE_OUTPUT=$(docker logs "$PEER_CONTAINER" 2>&1 | tail -20 | grep -E "(Downloading|Download|BUILD|Task)" | tail -1 || echo "")
            if [ -n "$GRADLE_OUTPUT" ] && [ $((CURRENT_TIME - LAST_STATUS_TIME)) -ge $STATUS_INTERVAL ]; then
                echo "   ⏳ $org: ${GRADLE_OUTPUT:0:80}"
                LAST_STATUS_TIME=$CURRENT_TIME
            fi
        fi
    done

    if $ALL_INSTALLED; then
        break
    fi

    # Show periodic status update
    if [ $((CURRENT_TIME - LAST_STATUS_TIME)) -ge $STATUS_INTERVAL ]; then
        INSTALLED_COUNT=0
        for org in "${ORGS[@]}"; do
            INSTALLED_COUNT=$((INSTALLED_COUNT + ${CHAINCODE_INSTALLED[$org]}))
        done
        echo "   ⏳ $INSTALLED_COUNT/${#ORGS[@]} peers completed install (${ELAPSED}s/${CHECK_TIMEOUT}s)"
        LAST_STATUS_TIME=$CURRENT_TIME
    fi

    sleep 3
done

INSTALLED_COUNT=0
for org in "${ORGS[@]}"; do
    INSTALLED_COUNT=$((INSTALLED_COUNT + ${CHAINCODE_INSTALLED[$org]}))
done

if [ $INSTALLED_COUNT -ne ${#ORGS[@]} ]; then
    echo ""
    echo "❌ Not all peers installed chaincode:"
    for org in "${ORGS[@]}"; do
        if [ "${CHAINCODE_INSTALLED[$org]}" -eq 0 ]; then
            echo "   ✗ $org - Installation failed or timed out"
            echo "     Check logs: docker logs peer0.${org}.com | tail -50"
            echo "     Look for: compilation errors, timeout messages, Gradle failures"
        fi
    done
    return 1
fi

echo "✓ All peers installed chaincode"
echo ""

# Step 5: Verify chaincode approval
echo "[5/5] Verifying chaincode approval..."

declare -A CHAINCODE_APPROVED
for org in "${ORGS[@]}"; do
    CHAINCODE_APPROVED[$org]=0
done

CHECK_START=$(date +%s)
CHECK_TIMEOUT=60

while [ $(($(date +%s) - CHECK_START)) -lt $CHECK_TIMEOUT ]; do
    ALL_APPROVED=true

    for org in "${ORGS[@]}"; do
        if [ "${CHAINCODE_APPROVED[$org]}" -eq 1 ]; then
            continue
        fi

        PEER_CONTAINER="peer0.${org}.com"

        # Check for approval success
        APPROVE_CHECK=$(docker logs "$PEER_CONTAINER" 2>&1 | grep -E "(successfully installed and approved|Approving chaincode)" || echo "")

        if [ -n "$APPROVE_CHECK" ]; then
            CHAINCODE_APPROVED[$org]=1
            echo "   ✓ $org approved chaincode"
        else
            ALL_APPROVED=false
        fi
    done

    if $ALL_APPROVED; then
        break
    fi

    sleep 2
done

echo "✓ All peers approved chaincode"
echo ""

# Final verification
TOTAL_TIME=$(($(date +%s) - START_TIME))

echo "════════════════════════════════════════════════════════════"
echo " ✓ All anchor peers ready (completed in ${TOTAL_TIME}s)"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "Summary:"
for org in "${ORGS[@]}"; do
    echo "   ✓ peer0.$org.com"
    echo "      - Joined channel: ${CHANNEL_NAME}"
    echo "      - Chaincode installed and approved"
done

# Check if FurnituresMakers committed chaincode
echo ""
echo "Checking chaincode commit status..."
COMMIT_CHECK=$(docker logs peer0.furnituresmakers.com 2>&1 | grep -E "(lifecycle chaincode commit|Successfully committed)" || echo "")
if [ -n "$COMMIT_CHECK" ]; then
    echo "   ✓ FurnituresMakers committed chaincode to channel"

    # Check for initialization
    INIT_CHECK=$(docker logs peer0.furnituresmakers.com 2>&1 | grep -E "(Initialize|isInit)" || echo "")
    if [ -n "$INIT_CHECK" ]; then
        echo "   ✓ Chaincode initialized with IntrinsicCoin/IC/18"
    fi
else
    echo "   ⚠ Chaincode commit not detected (check peer0.furnituresmakers.com logs)"
fi

echo ""
return 0