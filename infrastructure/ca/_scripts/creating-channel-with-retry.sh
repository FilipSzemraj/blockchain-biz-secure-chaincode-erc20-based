#!/bin/bash
# Enhanced version of creating-channel.sh with retry logic for orderer availability

export FABRIC_CFG_PATH=/etc/hyperledger/config
echo $FABRIC_CFG_PATH

echo 'Start of script creating-channel-with-retry.sh'

# Step 1: Create genesis block if it doesn't exist
if [ ! -f ../output/genesis_block_YFW.pb ]; then
  echo 'genesis_block_YFW is not existing - creating it now'
  configtxgen -profile YFW -outputBlock ../output/genesis_block_YFW.pb -channelID yfw-channel
  sleep 2
  echo 'Genesis block created successfully'
else
  echo 'Genesis block already exists - skipping creation'
fi

# Step 2: Try to join channel with retry logic
echo ''
echo 'Attempting osnadmin channel join with retry...'

MAX_RETRIES=30
RETRY_INTERVAL=5
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_RETRIES ]; do
  ATTEMPT=$((ATTEMPT + 1))
  echo "Attempt $ATTEMPT/$MAX_RETRIES: Trying to join channel on orderer0.${ORG_NAME}.com:9443"

  # Try osnadmin channel join
  osnadmin channel join \
    --channelID yfw-channel \
    --config-block ../output/genesis_block_YFW.pb \
    -o orderer0.${ORG_NAME}.com:9443 \
    --ca-file $OSN_TLS_CA_ROOT_CERT \
    --client-cert $ADMIN_TLS_SIGN_CERT \
    --client-key $ADMIN_TLS_PRIVATE_KEY 2>&1 | tee /tmp/osnadmin_output.log

  EXIT_CODE=${PIPESTATUS[0]}

  # Check exit code
  if [ $EXIT_CODE -eq 0 ]; then
    echo "✓ Successfully joined channel yfw-channel"
    exit 0
  fi

  # Check if already joined (405 Method Not Allowed)
  if grep -q "405" /tmp/osnadmin_output.log || grep -q "already exists" /tmp/osnadmin_output.log; then
    echo "✓ Channel already joined (expected if re-running)"
    exit 0
  fi

  # If it's a connection error, retry
  if grep -q "no such host\|connection refused\|dial tcp" /tmp/osnadmin_output.log; then
    echo "  Orderer not available yet. Waiting ${RETRY_INTERVAL}s before retry..."
    sleep $RETRY_INTERVAL
  else
    # Unknown error - log and exit
    echo "✗ Unexpected error from osnadmin:"
    cat /tmp/osnadmin_output.log
    exit 1
  fi
done

echo "✗ Failed to join channel after $MAX_RETRIES attempts"
echo "This may be expected if orderers haven't been started yet."
echo "Genesis block was created successfully at ../output/genesis_block_YFW.pb"
exit 1
