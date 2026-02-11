#!/bin/bash
# Gracefully stop the Fabric network (preserves ledger data and container state)
# Use this when you want to pause the network and resume it later

set -e

echo "════════════════════════════════════════════════════════════"
echo " Stopping Hyperledger Fabric Network (Preserving State)"
echo "════════════════════════════════════════════════════════════"

# Stop in reverse order of startup
echo ""
echo "[1/5] Stopping inner peers (peer1, peer2)..."
if command -v bash &> /dev/null && [ -f "_scripts/stop-all-inner-peers.sh" ]; then
    bash _scripts/stop-all-inner-peers.sh 2>/dev/null || true
else
    echo "  ⚠ Warning: stop-all-inner-peers.sh not found or bash not available"
fi

echo ""
echo "[2/5] Stopping anchor peers (peer0)..."
docker compose -f docker-compose.peer.all.yaml stop 2>/dev/null || true

echo ""
echo "[3/5] Stopping orderers..."
docker compose -f docker-compose.orderer.all.yaml stop 2>/dev/null || true

echo ""
echo "[4/5] Stopping configtx containers (if running)..."
docker compose -f docker-compose.configtx.all.yaml stop 2>/dev/null || true

echo ""
echo "[5/5] Stopping Certificate Authorities..."
docker compose stop 2>/dev/null || true

echo ""
echo "════════════════════════════════════════════════════════════"
echo " ✓ Network stopped successfully"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "To resume the network:"
echo "  1. docker compose up -d                              (start CAs)"
echo "  2. docker compose -f docker-compose.orderer.all.yaml up -d"
echo "  3. docker compose -f docker-compose.peer.all.yaml up -d"
echo ""
echo "Note: Ledger data and installed chaincode are preserved in stopped containers."
echo ""