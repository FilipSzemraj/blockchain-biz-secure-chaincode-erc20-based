#!/bin/bash
# Complete certificate cleanup - removes ALL crypto material for fresh enrollment

BASE_DIR="/home/filip/Dokumenty/blockchain-biz-secure-chaincode-erc20-based/infrastructure/ca"

ORGANIZATIONS=(
    "Furnitures_Makers"
    "Wood_Supply"
    "Yacht_Sales"
)

echo "════════════════════════════════════════════════════════════"
echo " Clearing ALL Crypto Material (Complete Clean Slate)"
echo "════════════════════════════════════════════════════════════"
echo ""

# Remove genesis block
GENESIS_BLOCK="$BASE_DIR/_config_files/configtx/output/genesis_block_YFW.pb"
if [ -f "$GENESIS_BLOCK" ]; then
    rm -f "$GENESIS_BLOCK"
    echo "✓ Genesis block cleared: $GENESIS_BLOCK"
else
    echo "○ Genesis block not found (already clean)"
fi

# Remove shared certs
SHARED_CERTS_GLOBAL="$BASE_DIR/_shared_certs"
if [ -d "$SHARED_CERTS_GLOBAL" ]; then
    rm -rf "$SHARED_CERTS_GLOBAL"
    echo "✓ Shared certs cleared: $SHARED_CERTS_GLOBAL"
else
    echo "○ Shared certs not found (already clean)"
fi

echo ""

# Clean each organization
for ORG in "${ORGANIZATIONS[@]}"; do
    echo "Cleaning organization: $ORG"
    echo "─────────────────────────────────────────"

    CLIENT_DIR="$BASE_DIR/$ORG/crypto/client"
    SERVER_DIR="$BASE_DIR/$ORG/crypto/server"

    # Remove entire client directory (all enrolled identities)
    if [ -d "$CLIENT_DIR" ]; then
        rm -rf "$CLIENT_DIR"
        echo "  ✓ Removed client enrollments: $CLIENT_DIR"
    else
        echo "  ○ Client directory not found"
    fi

    # Preserve fabric-ca-server-config.yaml files before deletion
    TEMP_CONFIG_DIR="$BASE_DIR/_temp_configs/$ORG"
    mkdir -p "$TEMP_CONFIG_DIR"

    if [ -f "$SERVER_DIR/ca/fabric-ca-server-config.yaml" ]; then
        cp "$SERVER_DIR/ca/fabric-ca-server-config.yaml" "$TEMP_CONFIG_DIR/ca-config.yaml"
        echo "  ○ Preserved CA config: ca/fabric-ca-server-config.yaml"
    fi

    if [ -f "$SERVER_DIR/tls-ca/fabric-ca-server-config.yaml" ]; then
        cp "$SERVER_DIR/tls-ca/fabric-ca-server-config.yaml" "$TEMP_CONFIG_DIR/tls-ca-config.yaml"
        echo "  ○ Preserved TLS CA config: tls-ca/fabric-ca-server-config.yaml"
    fi

    # Remove entire server directory (CA database and crypto)
    if [ -d "$SERVER_DIR" ]; then
        rm -rf "$SERVER_DIR"
        echo "  ✓ Removed CA server data: $SERVER_DIR"
    fi

    # Recreate directory structure and restore config files
    if [ -f "$TEMP_CONFIG_DIR/ca-config.yaml" ]; then
        mkdir -p "$SERVER_DIR/ca"
        cp "$TEMP_CONFIG_DIR/ca-config.yaml" "$SERVER_DIR/ca/fabric-ca-server-config.yaml"
        echo "  ○ Restored CA config"
    fi

    if [ -f "$TEMP_CONFIG_DIR/tls-ca-config.yaml" ]; then
        mkdir -p "$SERVER_DIR/tls-ca"
        cp "$TEMP_CONFIG_DIR/tls-ca-config.yaml" "$SERVER_DIR/tls-ca/fabric-ca-server-config.yaml"
        echo "  ○ Restored TLS CA config"
    fi

    echo ""
done

# Clean up temporary config directory
if [ -d "$BASE_DIR/_temp_configs" ]; then
    rm -rf "$BASE_DIR/_temp_configs"
fi

echo "════════════════════════════════════════════════════════════"
echo " ✓ All crypto material cleared successfully"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "What was removed:"
echo "  • All enrolled identities (admin, peers, orderers, api users)"
echo "  • All TLS certificates"
echo "  • CA server databases"
echo "  • CA server crypto material"
echo "  • Genesis block"
echo "  • Shared MSP configurations"
echo ""
echo "What was preserved:"
echo "  • Docker Compose files"
echo "  • Enrollment scripts"
echo "  • fabric-ca-server-config.yaml files (if they existed)"
echo ""
echo "To start fresh, run: docker compose up --build -d"
echo ""
