#!/bin/bash

# Color codes for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== Checking Chaincode Installation Status ===${NC}\n"

# Array of peer containers
PEERS=("peer0.furnituresmakers.com" "peer0.woodsupply.com" "peer0.yachtsales.com")
MSPS=("FurnituresMakers" "WoodSupply" "YachtSales")

for i in "${!PEERS[@]}"; do
    PEER=${PEERS[$i]}
    MSP=${MSPS[$i]}

    echo -e "${GREEN}--- Checking $PEER (MSP: $MSP) ---${NC}"

    # Query installed chaincode
    docker exec $PEER peer lifecycle chaincode queryinstalled \
        --peerAddresses $PEER:$(echo $PEER | grep -oP '\d+(?=51)' || echo 7)051 2>/dev/null

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Query successful${NC}\n"
    else
        echo -e "${RED}✗ Query failed - chaincode might not be installed${NC}\n"
    fi
done

echo -e "${YELLOW}=== Checking Committed Chaincode on Channel ===${NC}\n"

# Check committed chaincode on channel (only need to run on one peer)
echo -e "${GREEN}--- Querying channel yfw-channel ---${NC}"
docker exec peer0.furnituresmakers.com peer lifecycle chaincode querycommitted \
    -C yfw-channel 2>/dev/null || echo -e "${RED}✗ No chaincode committed or channel not joined${NC}"
