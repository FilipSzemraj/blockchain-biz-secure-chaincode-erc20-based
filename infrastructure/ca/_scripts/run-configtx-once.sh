#!/bin/bash
# Run configtx containers, wait for completion, then auto-cleanup
# Use this for genesis block creation and channel joining without manual cleanup

set -e

COMPOSE_FILE="../docker-compose.configtx.all.yaml"
GENESIS_BLOCK="../_config_files/configtx/output/genesis_block_YFW.pb"
MAX_WAIT=60  # Maximum wait time in seconds

echo "════════════════════════════════════════════════════════════"
echo " Running Configtx (Genesis + Channel Join) with Auto-Cleanup"
echo "════════════════════════════════════════════════════════════"

# Copy required scripts to build context
echo ""
echo "[1/5] Copying scripts to build context..."
cp -f creating-channel.sh ../_config_files/configtx/creating-channel.sh
cp -f healthcheck-admin.sh ../_config_files/configtx/healthcheck-admin.sh
echo "  ✓ Scripts copied"

# Clean up any previous run
echo ""
echo "[2/5] Cleaning up previous configtx containers..."
docker compose -f "$COMPOSE_FILE" down 2>/dev/null || true

# Build images
echo ""
echo "[3/5] Building configtx image..."
docker compose -f "$COMPOSE_FILE" build --quiet

# Start containers in background
echo ""
echo "[4/5] Starting configtx containers..."
docker compose -f "$COMPOSE_FILE" up -d

# Wait for work to complete
echo ""
echo "[5/5] Waiting for genesis block creation and channel join..."
ELAPSED=0
INTERVAL=2

while [ $ELAPSED -lt $MAX_WAIT ]; do
    # Check if genesis block exists
    if [ -f "$GENESIS_BLOCK" ]; then
        echo "  ✓ Genesis block created at: $GENESIS_BLOCK"

        # Wait a bit more for osnadmin channel join to complete
        sleep 5

        # Check logs for successful channel join from all 3 orgs
        echo ""
        echo "  Checking osnadmin channel join status:"
        for ORG in furnituresmakers woodsupply yachtsales; do
            STATUS=$(docker logs admin.${ORG}.com 2>&1 | grep -o '"Status": [0-9]*' | tail -1 | awk '{print $2}' || echo "unknown")
            if [ "$STATUS" = "201" ]; then
                echo "    ✓ admin.${ORG}.com - Status 201 (created)"
            elif [ "$STATUS" = "405" ]; then
                echo "    ✓ admin.${ORG}.com - Status 405 (already joined)"
            else
                echo "    ⚠ admin.${ORG}.com - Status $STATUS (check logs if not 201/405)"
            fi
        done

        break
    fi

    sleep $INTERVAL
    ELAPSED=$((ELAPSED + INTERVAL))
    echo "  Waiting... (${ELAPSED}s / ${MAX_WAIT}s)"
done

if [ $ELAPSED -ge $MAX_WAIT ]; then
    echo ""
    echo "  ✗ Timeout waiting for genesis block creation!"
    echo "  Check logs: docker logs admin.furnituresmakers.com"
    echo ""
    echo "  Containers are still running. Manual cleanup:"
    echo "    docker compose -f $COMPOSE_FILE down"
    exit 1
fi

# Auto-cleanup: stop and remove containers
echo ""
echo "════════════════════════════════════════════════════════════"
echo " Work Complete - Cleaning Up"
echo "════════════════════════════════════════════════════════════"
docker compose -f "$COMPOSE_FILE" down

echo ""
echo "✓ Configtx completed successfully and containers removed."
echo ""
echo "Results:"
echo "  • Genesis block: $GENESIS_BLOCK"
echo "  • Orderers joined to channel: yfw-channel"
echo ""
echo "Next step: Start anchor peers"
echo "  docker compose -f docker-compose.peer.all.yaml up --build -d"
echo ""