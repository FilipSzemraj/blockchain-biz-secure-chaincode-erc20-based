#!/bin/bash
# Smart healthcheck that considers the container healthy if:
# 1. Genesis block exists (primary job done), OR
# 2. Orderer is reachable and channel is joined

# Check if genesis block exists
if [ -f /etc/hyperledger/output/genesis_block_YFW.pb ]; then
  echo 'Genesis block exists - container is healthy'

  # Optional: Try to verify channel join status (but don't fail if orderer unavailable)
  osnadmin channel list \
    -o orderer0.${ORG_NAME}.com:9443 \
    --ca-file $OSN_TLS_CA_ROOT_CERT \
    --client-cert $ADMIN_TLS_SIGN_CERT \
    --client-key $ADMIN_TLS_PRIVATE_KEY 2>/dev/null

  if [ $? -eq 0 ]; then
    echo 'Orderer is reachable and responding'
  else
    echo 'Orderer not reachable yet (this is OK if orderers not started)'
  fi

  exit 0  # Healthy because genesis block exists
fi

# Genesis block doesn't exist yet - container is still working on it
echo 'Genesis block not found yet - still initializing'
exit 1