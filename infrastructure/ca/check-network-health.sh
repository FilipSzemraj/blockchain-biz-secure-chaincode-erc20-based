#!/bin/bash

echo "════════════════════════════════════════════════════════════"
echo " Hyperledger Fabric Network Health Check"
echo "════════════════════════════════════════════════════════════"
echo ""

# Count containers
CA_COUNT=$(docker ps --filter "name=ca" -q | wc -l)
ORDERER_COUNT=$(docker ps --filter "name=orderer" -q | wc -l)
PEER_COUNT=$(docker ps --filter "name=peer" -q | wc -l)

echo "Container Status:"
echo "  CAs:      $CA_COUNT / 3 expected"
echo "  Orderers: $ORDERER_COUNT / 3 expected"
echo "  Peers:    $PEER_COUNT / 3 + 3 chaincode containers (anchor) or additional 6 (with inner peers) expected"
echo ""

# Check CAs
echo "Certificate Authorities:"
for org in furnituresmakers woodsupply yachtsales; do
  STATUS=$(docker inspect --format='{{.State.Status}}' ${org}-ca 2>/dev/null || echo "not found")
  if [ "$STATUS" = "running" ]; then
    echo "  ✓ $org CA: running"
  else
    echo "  ✗ $org CA: $STATUS"
  fi
done
echo ""

# Check shared certs
echo "Shared Certificates:"
if [ -d "_shared_certs" ]; then
  CERT_COUNT=$(find _shared_certs -name "config.yaml" 2>/dev/null | wc -l)
  echo "  Found $CERT_COUNT / 3 MSP configurations (could be more due to other peer (orderer etc) certs)"
  for org in furnituresmakers woodsupply yachtsales; do
    if [ -f "_shared_certs/${org}-msp/config.yaml" ]; then
      echo "    ✓ ${org}-msp"
    else
      echo "    ✗ ${org}-msp (missing)"
    fi
  done
else
  echo "  ✗ _shared_certs directory not found"
fi
echo ""

# Check orderers + channel membership
echo "Orderers:"
for org in furnituresmakers woodsupply yachtsales; do
  STATUS=$(docker inspect --format='{{.State.Status}}' orderer0.${org}.com 2>/dev/null || echo "not found")
  if [ "$STATUS" = "running" ]; then
    # Check if joined channel
    CHANNEL_CHECK=$(docker exec admin.${org}.com bash -c "osnadmin channel list \
      -o orderer0.\$ORG_NAME.com:9443 \
      --ca-file \$OSN_TLS_CA_ROOT_CERT \
      --client-cert \$ADMIN_TLS_SIGN_CERT \
      --client-key \$ADMIN_TLS_PRIVATE_KEY" 2>&1 | grep -c "yfw-channel" || echo "0")

    if [ "$CHANNEL_CHECK" -gt 0 ]; then
      echo "  ✓ orderer0.$org.com: running + joined yfw-channel"
    else
      echo "  ⚠ orderer0.$org.com: running but NOT joined to channel"
    fi
  else
    echo "  ✗ orderer0.$org.com: $STATUS"
  fi
done
echo ""

# Check Raft leader
echo "Raft Consensus:"
LEADER_FOUND=false
for org in furnituresmakers woodsupply yachtsales; do
  LEADER_LOG=$(docker logs orderer0.${org}.com 2>&1 | grep "became leader" | tail -1 || echo "")
  if [ -n "$LEADER_LOG" ]; then
    echo "  ★ $org is Raft leader"
    LEADER_FOUND=true
  fi
done
if ! $LEADER_FOUND; then
  echo "  ⚠ No Raft leader detected (consensus may be initializing)"
fi
echo ""

# Check anchor peers
echo "Anchor Peers:"
for org in furnituresmakers woodsupply yachtsales; do
  STATUS=$(docker inspect --format='{{.State.Status}}' peer0.${org}.com 2>/dev/null || echo "not found")
  if [ "$STATUS" = "running" ]; then
    # Check channel membership
    CHANNEL_LIST=$(docker exec peer0.${org}.com peer channel list 2>&1 | grep -c "yfw-channel" || echo "0")
    if [ "$CHANNEL_LIST" -gt 0 ]; then
      echo "  ✓ peer0.$org.com: running + joined yfw-channel"
    else
      echo "  ⚠ peer0.$org.com: running but NOT joined to channel"
    fi
  else
    echo "  ✗ peer0.$org.com: $STATUS"
  fi
done
echo ""

# Check chaincode
echo "Chaincode Status:"
CHAINCODE_COMMITTED=$(docker exec peer0.furnituresmakers.com peer lifecycle chaincode querycommitted \
  -C yfw-channel -n basic 2>&1 | grep -c "Version: 1.0" || echo "0")

if [ "$CHAINCODE_COMMITTED" -gt 0 ]; then
  echo "  ✓ Chaincode 'basic' v1.0 committed to yfw-channel"

  # Check chaincode containers
  CC_CONTAINERS=$(docker ps --filter "name=basic_1.0" -q | wc -l)
  echo "  ✓ Chaincode containers running: $CC_CONTAINERS / 3 expected"
else
  echo "  ✗ Chaincode 'basic' NOT committed to channel"
fi
echo ""

# Genesis block
echo "Channel Configuration:"
if [ -f "_config_files/configtx/output/genesis_block_YFW.pb" ]; then
  BLOCK_SIZE=$(du -h "_config_files/configtx/output/genesis_block_YFW.pb" | cut -f1)
  echo "  ✓ Genesis block exists ($BLOCK_SIZE)"
else
  echo "  ✗ Genesis block not found"
fi
echo ""

echo "════════════════════════════════════════════════════════════"
echo " Summary"
echo "════════════════════════════════════════════════════════════"
if [ $CA_COUNT -eq 3 ] && [ $ORDERER_COUNT -eq 3 ] && [ $PEER_COUNT -ge 3 ]; then
  echo " ✓ Network is running"
  echo ""
  echo " Quick tests:"
  echo "   - Test chaincode query:"
  echo "     docker exec peer0.furnituresmakers.com peer chaincode query \\"
  echo "       -C yfw-channel -n basic -c '{\"function\":\"ClientAccountBalance\",\"Args\":[]}'"
else
  echo " ✗ Network is NOT fully running"
  echo ""
  echo " Missing components - run these commands to start:"
  [ $CA_COUNT -lt 3 ] && echo "   docker compose up --build -d"
  [ $ORDERER_COUNT -lt 3 ] && echo "   bash _scripts/run-orderer.sh"
  [ $PEER_COUNT -lt 3 ] && echo "   bash _scripts/run-anchor-peers.sh"
fi
echo ""
