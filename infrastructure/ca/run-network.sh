#!/bin/bash

set -e

echo "════════════════════════════════════════════════════════════"
echo " Running Hyperledger Fabric Network (cold start)"
echo "════════════════════════════════════════════════════════════"

echo "This will:"
echo "  • Remove all containers (CAs, orderers, peers)"
echo "  • Delete all ledger data (transaction history)"
echo "  • Remove installed chaincode packages"
echo "  • Remove crypto material (certificates) by running clearing-cert.sh"
echo ""
read -p "Are you sure you want to proceed? (yes/no): " -r
if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
    echo "Cold start cancelled."
    exit 0
fi

echo ""
echo "[1/5] teardown-network.sh"
bash teardown-network.sh 2>/dev/null || true


echo ""
echo "[2/5] clearing-certs.sh"
bash clearing-cert.sh 2>/dev/null || true


echo ""
echo "[3/6] Starting Certificate Authorities (CAs)"
echo "────────────────────────────────────────────────"
docker compose up --build -d

# Wait for TLS initializers to complete
source _scripts/wait-for-containers.sh 90 "exited" "initializer"
if [ $? -ne 0 ]; then
    echo "❌ TLS initializers failed to complete"
    echo "Check logs: docker logs initializer_furnituresmakers"
    exit 1
fi

# Wait for Org CAs to be running
source _scripts/wait-for-containers.sh 60 "up" "ca"
if [ $? -ne 0 ]; then
    echo "❌ Org CAs failed to start"
    echo "Check logs: docker logs furnituresmakers-ca"
    exit 1
fi

# Verify shared certificates were created
source _scripts/wait-for-files.sh 30 \
    "_shared_certs/furnituresmakers-msp" \
    "_shared_certs/woodsupply-msp" \
    "_shared_certs/yachtsales-msp"
if [ $? -ne 0 ]; then
    echo "❌ Shared certificates not created"
    exit 1
fi

echo "✓ CAs are ready"
echo ""


echo "[4/6] Creating Genesis Block & Channel"
echo "────────────────────────────────────────────────"
bash _scripts/run-configtx-all.sh

# Wait for genesis block to be created
source _scripts/wait-for-files.sh 60 "_config_files/configtx/output/genesis_block_YFW.pb"
if [ $? -ne 0 ]; then
    echo "❌ Genesis block not created"
    echo "Check logs: docker logs admin.furnituresmakers.com"
    exit 1
fi

echo "✓ Genesis block created"
echo ""


echo "[5/6] Starting Orderers"
echo "────────────────────────────────────────────────"
bash _scripts/run-orderer.sh

# Wait for orderers to be running
source _scripts/wait-for-containers.sh 90 "up" "orderer"
if [ $? -ne 0 ]; then
    echo "❌ Orderers failed to start"
    echo "Check logs: docker logs orderer0.furnituresmakers.com"
    exit 1
fi

# Wait for ALL orderers to join channel (checks all 3 orgs automatically)
source _scripts/wait-for-channel-join.sh 90 yfw-channel
if [ $? -ne 0 ]; then
    echo "❌ Not all orderers joined channel"
    echo "   Check logs: docker logs admin.furnituresmakers.com"
    exit 1
fi

echo ""


echo "[6/6] Starting Anchor Peers & Installing Chaincode"
echo "────────────────────────────────────────────────"
echo "⚠ This step includes chaincode installation which may take 5-10 minutes on first run"
echo ""
bash _scripts/run-anchor-peers.sh

# Wait for complete peer startup sequence with detailed progress
source _scripts/wait-for-peers.sh 600 yfw-channel
if [ $? -ne 0 ]; then
    echo ""
    echo "❌ Peer startup sequence failed"
    echo ""
    echo "Troubleshooting steps:"
    echo "  1. Check peer logs:"
    echo "     docker logs peer0.furnituresmakers.com | tail -100"
    echo "     docker logs peer0.woodsupply.com | tail -100"
    echo "     docker logs peer0.yachtsales.com | tail -100"
    echo ""
    echo "  2. Check for chaincode compilation errors:"
    echo "     docker logs peer0.furnituresmakers.com | grep -A10 'BUILD FAILED'"
    echo ""
    echo "  3. Verify chaincode source is valid:"
    echo "     cd ../chaincode && ./gradlew build"
    echo ""
    exit 1
fi

echo "✓ All anchor peers ready with chaincode installed"
echo ""


echo "════════════════════════════════════════════════════════════"
echo " ✓ Network startup complete!"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "Network status:"
docker ps --format "table {{.Names}}\t{{.Status}}" | grep -E "(ca|orderer|peer0)"
echo ""
echo "To start inner peers (peer1, peer2):"
echo "  cd _scripts && bash run-all-inner-peers.sh"
