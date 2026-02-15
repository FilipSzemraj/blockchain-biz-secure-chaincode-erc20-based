# Shell Script Waiting Patterns for Fabric Network

## Quick Reference

### 1. Wait for containers to exit successfully
```bash
source _scripts/wait-for-containers.sh 90 "exited" "initializer"
```

### 2. Wait for containers to be running
```bash
source _scripts/wait-for-containers.sh 60 "up" "ca" "orderer"
```

### 3. Wait for files/directories to exist
```bash
source _scripts/wait-for-files.sh 30 "_shared_certs/furnituresmakers-msp"
```

### 4. Wait for multiple paths
```bash
source _scripts/wait-for-files.sh 60 \
    "_shared_certs/furnituresmakers-msp" \
    "_shared_certs/woodsupply-msp" \
    "_shared_certs/yachtsales-msp"
```

---

## Advanced Patterns

### Custom polling loop with retry logic
```bash
MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if docker exec orderer0.furnituresmakers.com bash -c 'test -f /var/hyperledger/production/orderer/chains/yfw-channel/blockfile_000000'; then
        echo "✓ Channel blockfile exists"
        break
    fi

    ATTEMPT=$((ATTEMPT + 1))
    echo "⏳ Waiting for channel blockfile... ($ATTEMPT/$MAX_ATTEMPTS)"
    sleep 2
done

if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
    echo "❌ Timeout waiting for channel blockfile"
    exit 1
fi
```

### Wait for specific log message
```bash
wait_for_log() {
    local CONTAINER=$1
    local PATTERN=$2
    local TIMEOUT=${3:-60}

    echo "⏳ Waiting for log pattern in $CONTAINER: '$PATTERN'"

    START=$(date +%s)
    while [ $(($(date +%s) - START)) -lt $TIMEOUT ]; do
        if docker logs "$CONTAINER" 2>&1 | grep -q "$PATTERN"; then
            echo "✓ Found log pattern"
            return 0
        fi
        sleep 2
    done

    echo "❌ Timeout waiting for log pattern"
    return 1
}

# Usage:
wait_for_log "orderer0.furnituresmakers.com" "became leader at term" 90
```

### Wait for Fabric orderer to join channel (osnadmin API)
```bash
wait_for_orderer_channel() {
    local ORDERER=$1
    local CHANNEL=$2
    local ADMIN_CONTAINER=${3:-"admin.furnituresmakers.com"}
    local TIMEOUT=${4:-90}

    echo "⏳ Waiting for $ORDERER to join $CHANNEL"

    START=$(date +%s)
    while [ $(($(date +%s) - START)) -lt $TIMEOUT ]; do
        RESULT=$(docker exec "$ADMIN_CONTAINER" bash -c "
            osnadmin channel list \
                -o ${ORDERER}:9443 \
                --ca-file \$OSN_TLS_CA_ROOT_CERT \
                --client-cert \$ADMIN_TLS_SIGN_CERT \
                --client-key \$ADMIN_TLS_PRIVATE_KEY
        " 2>&1 || echo "")

        if echo "$RESULT" | grep -q "\"name\": \"$CHANNEL\""; then
            echo "✓ Channel join confirmed"
            return 0
        fi

        sleep 3
    done

    echo "❌ Timeout waiting for channel join"
    return 1
}

# Usage:
wait_for_orderer_channel "orderer0.furnituresmakers.com" "yfw-channel" "admin.furnituresmakers.com" 90
```

### Wait for HTTP endpoint to respond
```bash
wait_for_http() {
    local URL=$1
    local TIMEOUT=${2:-60}

    echo "⏳ Waiting for HTTP endpoint: $URL"

    START=$(date +%s)
    while [ $(($(date +%s) - START)) -lt $TIMEOUT ]; do
        if curl -s -f -o /dev/null "$URL"; then
            echo "✓ Endpoint is responding"
            return 0
        fi
        sleep 3
    done

    echo "❌ Timeout waiting for endpoint"
    return 1
}

# Usage:
wait_for_http "http://localhost:7054/healthz" 60
```

### Wait for container health status (if using HEALTHCHECK)
```bash
wait_for_healthy() {
    local CONTAINER=$1
    local TIMEOUT=${2:-120}

    echo "⏳ Waiting for $CONTAINER to be healthy"

    START=$(date +%s)
    while [ $(($(date +%s) - START)) -lt $TIMEOUT ]; do
        HEALTH=$(docker inspect --format='{{.State.Health.Status}}' "$CONTAINER" 2>/dev/null || echo "none")

        if [ "$HEALTH" = "healthy" ]; then
            echo "✓ Container is healthy"
            return 0
        fi

        echo "   Health status: $HEALTH (${ELAPSED}s/${TIMEOUT}s)"
        sleep 3
    done

    echo "❌ Timeout waiting for healthy status"
    return 1
}

# Usage:
wait_for_healthy "peer0.furnituresmakers.com" 120
```

### Wait for port to be listening
```bash
wait_for_port() {
    local HOST=$1
    local PORT=$2
    local TIMEOUT=${3:-60}

    echo "⏳ Waiting for $HOST:$PORT to be listening"

    START=$(date +%s)
    while [ $(($(date +%s) - START)) -lt $TIMEOUT ]; do
        if nc -z "$HOST" "$PORT" 2>/dev/null; then
            echo "✓ Port is listening"
            return 0
        fi
        sleep 2
    done

    echo "❌ Timeout waiting for port"
    return 1
}

# Usage:
wait_for_port "localhost" 7054 60
```

---

## Error Handling Patterns

### Check exit code and show troubleshooting hint
```bash
source _scripts/wait-for-containers.sh 90 "exited" "initializer"
if [ $? -ne 0 ]; then
    echo "❌ TLS initializers failed"
    echo ""
    echo "Troubleshooting:"
    echo "  1. Check logs: docker logs initializer_furnituresmakers"
    echo "  2. Verify CRLF: perl -pi -e 's/\\r\\n/\\n/g' _scripts/*.sh"
    echo "  3. Check disk space: df -h"
    exit 1
fi
```

### Continue on timeout with warning
```bash
source _scripts/wait-for-containers.sh 30 "up" "peer1"
if [ $? -ne 0 ]; then
    echo "⚠ Warning: Inner peers not ready yet, continuing anyway..."
else
    echo "✓ Inner peers are ready"
fi
```

### Retry on failure
```bash
RETRY_COUNT=3
RETRY_DELAY=10

for i in $(seq 1 $RETRY_COUNT); do
    echo "Attempt $i/$RETRY_COUNT: Starting orderers..."

    bash _scripts/run-orderer.sh
    source _scripts/wait-for-containers.sh 60 "up" "orderer"

    if [ $? -eq 0 ]; then
        echo "✓ Orderers started successfully"
        break
    fi

    if [ $i -lt $RETRY_COUNT ]; then
        echo "⚠ Attempt $i failed, retrying in ${RETRY_DELAY}s..."
        sleep $RETRY_DELAY
    else
        echo "❌ All $RETRY_COUNT attempts failed"
        exit 1
    fi
done
```

---

## Testing Your Wait Scripts

### Test with expected success
```bash
# Start CAs
docker compose up --build -d

# This should succeed
bash _scripts/wait-for-containers.sh 90 "exited" "initializer"
echo "Exit code: $?"  # Should be 0
```

### Test with expected timeout
```bash
# This should timeout (looking for non-existent containers)
bash _scripts/wait-for-containers.sh 10 "up" "nonexistent"
echo "Exit code: $?"  # Should be 1
```

### Test file waiting
```bash
# This should succeed (assuming CAs created certs)
bash _scripts/wait-for-files.sh 30 "_shared_certs/furnituresmakers-msp"
echo "Exit code: $?"  # Should be 0
```

---

## When to Use Each Pattern

| Scenario | Recommended Pattern | Why |
|----------|-------------------|-----|
| Container must exit successfully (initializers) | `wait-for-containers.sh <timeout> "exited"` | Verifies container completed its job |
| Long-running service must be up (CA, orderer, peer) | `wait-for-containers.sh <timeout> "up"` | Ensures service is running |
| File must be created (genesis block, certs) | `wait-for-files.sh` | Confirms file system changes |
| Check specific log message | Custom `wait_for_log()` | Verifies internal state |
| HTTP service must be ready | Custom `wait_for_http()` | Tests actual service availability |
| Container with HEALTHCHECK | `wait_for_healthy()` | Uses Docker's built-in health status |
| Network port must be listening | `wait_for_port()` | Verifies network connectivity |

---

## Common Mistakes to Avoid

### ❌ Don't use fixed sleep without verification
```bash
# BAD - race condition if containers start slowly
docker compose up -d
sleep 30  # Hope it's enough?
```

### ✓ Use polling with actual state check
```bash
# GOOD - wait until actually ready
docker compose up -d
source _scripts/wait-for-containers.sh 90 "up" "ca"
```

### ❌ Don't ignore exit codes
```bash
# BAD - continues even if wait failed
source _scripts/wait-for-containers.sh 60 "up" "orderer"
docker logs orderer0.furnituresmakers.com
```

### ✓ Check exit codes and handle failures
```bash
# GOOD - stops on failure
source _scripts/wait-for-containers.sh 60 "up" "orderer"
if [ $? -ne 0 ]; then
    echo "❌ Orderers failed to start"
    docker logs orderer0.furnituresmakers.com
    exit 1
fi
```

### ❌ Don't use `docker wait` for long-running services
```bash
# BAD - docker wait only works for containers that EXIT
docker compose up -d
docker wait furnituresmakers-ca  # Will hang forever (CA never exits)
```

### ✓ Use status polling for long-running services
```bash
# GOOD - polls container status
docker compose up -d
source _scripts/wait-for-containers.sh 60 "up" "ca"
```