# Network Setup Guide

Step-by-step guide for deploying the Hyperledger Fabric network.

> **Source:** Converted from the original "Kroki do uruchomienia sieci.docx"

## Prerequisites

- Docker & Docker Compose (20.10+)
- Java JDK 21 (for backend APIs)
- Node.js 18+ and npm (for frontend)
- Python 3.8+ (for bank key generation)

> **Note:** Java 11 and Gradle are only needed for [running chaincode tests locally](../README.md#testing). Chaincode is compiled inside Docker by Fabric's `fabric-javaenv` image.

## Step 1: Start Certificate Authorities

```bash
cd infrastructure/ca
docker-compose up -d
```

This starts the identity and TLS Certificate Authorities for all three organizations:
- `ca-yachtsales`, `tls-ca-yachtsales`
- `ca-furnituresmakers`, `tls-ca-furnituresmakers`
- `ca-woodsupply`, `tls-ca-woodsupply`

## Step 2: Spread NodeOUS Configuration

```bash
./_config_files/shared/spread-config.sh
```

Distributes the `config.yaml` file responsible for NodeOUS (Organizational Unit) settings to all organizations.

## Step 3: Enroll Identities

```bash
cd _scripts
./enroll-ca-new.sh       # Enroll identity certificates
./enroll-tls-new.sh      # Enroll TLS certificates
```

This generates crypto material for peers, orderers, and admin users.

## Step 4: Share Certificates

```bash
./share-certs.sh
```

Distributes TLS CA certificates between organizations so they can verify each other.

## Step 5: Start Orderers

```bash
cd ..
docker-compose -f docker-compose.orderer.all.yaml up -d
```

Starts Raft orderer nodes for all three organizations.

## Step 6: Create Channel

```bash
docker-compose -f docker-compose.configtx.all.yaml up
```

Creates the `yfw-channel` and generates the genesis block using `configtx.yaml`.

## Step 7: Start Anchor Peers

```bash
cd _scripts
./run-anchor-peers.sh
```

Starts the anchor peer (peer0) for each organization and joins them to the channel.

> **Note:** On Windows, run this from Ubuntu WSL — otherwise Docker socket mapping won't work for chaincode container creation.

## Step 8: Start Inner Peers

```bash
./run-all-inner-peers.sh
```

Starts additional peers (peer1, peer2) for each organization.

## Step 9: Add Anchor Peer Configuration

```bash
./add-anchor-peers.sh
```

Updates the channel configuration with anchor peer addresses for cross-organization gossip.

## Step 10: Chaincode Installation (Automatic)

Chaincode is installed automatically by `add-anchor-peers.sh` during anchor peer startup (Step 7). The peer Docker containers mount `chaincode/` from the repository root directly, and Fabric's `fabric-javaenv` image compiles the Java source inside Docker — no local JDK 11 or Gradle is needed.

The script handles the full lifecycle: package → install → approve → commit → initialize.

For reference, the manual equivalent of what the script does:

```bash
# Package (runs inside peer container, source mounted from chaincode/)
peer lifecycle chaincode package basic.tar.gz \
  --path ../chaincode/chaincode-java/ --lang java --label basic_1.0

# Install on each organization's peer
peer lifecycle chaincode install basic.tar.gz

# Approve for each organization
peer lifecycle chaincode approveformyorg \
  --channelID yfw-channel --name basic --version 1.0 \
  --package-id <PACKAGE_ID> --sequence 1 \
  --tls --cafile $ORDERER_CA

# Commit (furnituresmakers org triggers this)
peer lifecycle chaincode commit \
  --channelID yfw-channel --name basic --version 1.0 \
  --sequence 1 --tls --cafile $ORDERER_CA \
  --peerAddresses peer0.furnituresmakers.com:7051 \
  --peerAddresses peer0.yachtsales.com:9051 \
  --peerAddresses peer0.woodsupply.com:8051
```

## Step 11: Start Backend Services

```bash
cd ../backend/client-api
docker-compose up -d    # PostgreSQL + API instances for all 3 orgs
```

## Step 12: Start Bank API

```bash
cd ../bank-api
docker-compose up -d    # Bank API on port 8081
```

## Step 13: Start Frontend

```bash
cd ../../frontend
npm install
npm run dev             # Development server at http://localhost:5173
```

## Verification

```bash
# Query token symbol from chaincode
peer chaincode query -C yfw-channel -n basic -c '{"Args":["TokenSymbol"]}' \
  --tls --cafile $CA_FILE

# Expected: status:200 with token symbol
```

## Stopping the Network

```bash
cd infrastructure/ca/_scripts
./stop-all-inner-peers.sh

cd ..
docker-compose -f docker-compose.orderer.all.yaml down
docker-compose down
```

## Troubleshooting

- **CA not starting:** Check Docker logs with `docker-compose logs ca-furnituresmakers`
- **Channel creation fails:** Ensure orderers are healthy: `./healthcheck-admin.sh`
- **Peer can't join channel:** Verify TLS certs were shared: check `crypto/` directories
- **Chaincode install fails:** Verify `chaincode/` contains `src/`, `build.gradle`, and `settings.gradle` — these are mounted into peer containers and built by `fabric-javaenv`
