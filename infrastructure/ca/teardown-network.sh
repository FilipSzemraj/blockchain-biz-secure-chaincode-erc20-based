#!/bin/bash
# Completely tear down the Fabric network (removes all containers and data)
# ⚠ WARNING: This will DESTROY all ledger data and chaincode installations!

set -e

echo "════════════════════════════════════════════════════════════"
echo " ⚠  DESTRUCTIVE TEARDOWN - Removing All Containers & Data"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "This will:"
echo "  • Remove all containers (CAs, orderers, peers)"
echo "  • Delete all ledger data (transaction history)"
echo "  • Remove installed chaincode packages"
echo "  • Preserve crypto material (certificates) unless you run clearing-cert.sh"
echo ""
read -p "Are you sure you want to proceed? (yes/no): " -r
if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
    echo "Teardown cancelled."
    exit 0
fi

echo ""
echo "[1/5] Removing inner peers (peer1, peer2)..."
bash _scripts/stop-all-inner-peers.sh 2>/dev/null || true

echo ""
echo "[2/5] Removing anchor peers (peer0)..."
docker compose -f docker-compose.peer.all.yaml down 2>/dev/null || true

echo ""
echo "[3/5] Removing orderers..."
docker compose -f docker-compose.orderer.all.yaml down 2>/dev/null || true

echo ""
echo "[4/5] Removing configtx containers..."
docker compose -f docker-compose.configtx.all.yaml down 2>/dev/null || true

echo ""
echo "[5/5] Removing Certificate Authorities..."
docker compose down 2>/dev/null || true

echo ""
echo "════════════════════════════════════════════════════════════"
echo " ✓ Network torn down successfully"
echo "════════════════════════════════════════════════════════════"
echo ""

# Verify no Fabric containers remain
echo "Verifying all Fabric containers removed..."
REMAINING=$(docker ps -a --filter "name=orderer\|peer\|admin\|ca\|initializer\|configtx" --format "{{.Names}}" | wc -l)

if [ "$REMAINING" -eq 0 ]; then
    echo "✓ All containers removed successfully"
else
    echo "⚠ Warning: $REMAINING Fabric container(s) still exist:"
    docker ps -a --filter "name=orderer\|peer\|admin\|ca\|initializer\|configtx" \
      --format "table {{.Names}}\t{{.Status}}\t{{.Image}}"
    echo ""
    echo "To remove remaining containers manually:"
    echo "  docker rm -f \$(docker ps -a --filter \"name=orderer\|peer\|admin\|ca\|initializer\|configtx\" -q)"
fi

echo ""
echo "Containers removed. Crypto material preserved in:"
echo "  • Furnitures_Makers/crypto/"
echo "  • Wood_Supply/crypto/"
echo "  • Yacht_Sales/crypto/"
echo "  • _shared_certs/"
echo "  • _config_files/configtx/output/"
echo ""
echo "To remove crypto material and start completely fresh:"
echo "  bash clearing-cert.sh"
echo ""