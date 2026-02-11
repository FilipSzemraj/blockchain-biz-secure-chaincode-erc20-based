# Hyperledger Fabric Network - Infrastructure Guide

Working directory for all commands: `infrastructure/ca/`

## Network overview

- 3 organizations: **FurnituresMakers** (port prefix 70xx), **WoodSupply** (80xx), **YachtSales** (90xx)
- Each org has: 1 TLS CA, 1 Org CA``, 1 orderer (orderer0), 3 peers (peer0 anchor + peer1, peer2)
- Channel: `yfw-channel`
- Chaincode: `basic` (Java, initialized with `IntrinsicCoin/IC/18`)
- Hyperledger Fabric v2.5, Fabric CA v1.5.17
- Runs on WSL2 with NTFS mount

## Port map

| Service | FurnituresMakers | WoodSupply | YachtSales |
|---------|-----------------|------------|------------|
| CA | 7054 | 8054 | 9054 |
| Orderer | 7050 | 8050 | 9050 |
| Orderer Admin | 9443 | 9443 | 9443 |
| Peer0 | 7051 | 8051 | 9051 |
| Peer0 Chaincode | 7052 | 8052 | 9052 |

## Identities per org

Defined in `_scripts/users_array.sh`:

| Identity | Password | Type | Purpose |
|----------|----------|------|---------|
| `<hostname>-ca` | capw | - | Org CA server identity |
| admin | adminpw | admin | Org admin (channel ops, chaincode) |
| peer0 | peer0pw | peer | Anchor peer |
| peer1 | peer1pw | peer | Additional peer |
| peer2 | peer2pw | peer | Additional peer |
| orderer0 | orderer0pw | orderer | Raft orderer |
| production_api | productionapipw | client | API user |
| sales_api | salesapipw | client | API user |
| logistics_api | logisticsapipw | client | API user |

TLS CA bootstrap: `tlsadmin:tlsadminpw`
Org CA bootstrap: `admin:adminpw`

---

## FULL STARTUP SEQUENCE

there is prepared script that is running all of the steps from below, but for first run it is recommended to run it semi-manual to get some insight on how network works

```bash
bash _scripts/run-whole-network.sh //todo
```

### Step 0: Prerequisites

Docker Desktop running with WSL2 (on Windows) integration enabled, or Docker Engine on Linux.

### Step 0: Understanding Network State

The Fabric network consists of multiple layers of state:

**1. Crypto Material (Certificates & Keys)**
- Location: `Furnitures_Makers/crypto/`, `Wood_Supply/crypto/`, `Yacht_Sales/crypto/`
- Shared MSP configs: `_shared_certs/furnituresmakers-msp/`, `woodsupply-msp/`, `yachtsales-msp/`
- Genesis block: `_config_files/configtx/output/genesis_block_YFW.pb`
- Can be regenerated: ✓ Yes (by re-running CA enrollment scripts)
- Preserved on shutdown: ✓ Yes (stored on host filesystem)

**2. Ledger Data & Channel State**
- Location: **Inside containers** at `/var/hyperledger/production` (NOT mounted to host)
- Contains: Transaction history, world state database, installed chaincode
- Can be regenerated: ✗ No (transaction history is permanent record)
- Preserved on shutdown: **Depends on shutdown method** (see below)

**3. Installed Chaincode Packages**
- Location: Inside peer containers at `/var/hyperledger/production/lifecycle/`
- Preserved on shutdown: **Depends on shutdown method**

**Shutdown Methods:**
- `docker compose stop` → **Preserves ledger & chaincode** (use `stop-network.sh`)
- `docker compose down` → **Destroys ledger & chaincode** (use `teardown-network.sh`)

### Step 0: Checking Running Containers

**Quick check** - What's currently running:

```bash
docker ps -a \
  --filter "name=initializer" \
  --filter "name=ca" \
  --filter "name=peer" \
  --filter "name=orderer" \
  --filter "name=admin" \
  --format "table {{.Names}}\t{{.Status}}"
```

**Comprehensive check** - All Fabric containers (useful for teardown verification):

```bash
# List ALL Fabric-related containers with detailed info
docker ps -a --filter "name=orderer\|peer\|admin\|ca\|initializer\|configtx" \
  --format "table {{.Names}}\t{{.Status}}\t{{.Image}}\t{{.Ports}}"

# Count containers by status
echo "Running: $(docker ps --filter "name=orderer\|peer\|admin\|ca\|initializer\|configtx" -q | wc -l)"
echo "Stopped: $(docker ps -a --filter "name=orderer\|peer\|admin\|ca\|initializer\|configtx" --filter "status=exited" -q | wc -l)"
echo "Total: $(docker ps -a --filter "name=orderer\|peer\|admin\|ca\|initializer\|configtx" -q | wc -l)"
```

**Expected states depending on network phase:**

| Phase | Initializers | CAs | Orderers | Peers | Configtx |
|-------|--------------|-----|----------|-------|----------|
| Fresh start | Not exists | Not exists | Not exists | Not exists | Not exists |
| After Step 1 (CAs) | Exited (0) | Up | Not exists | Not exists | Not exists |
| After Step 6 (Orderers) | Exited (0) | Up | Up | Not exists | Exited (0) |
| Fully running | Exited (0) | Up | Up | Up | Exited (0) |
| Stopped (preserving) | Exited (0) | Exited | Exited | Exited | Exited |
| After teardown | Not exists | Not exists | Not exists | Not exists | Not exists |

### Step 0: Stopping the Network (Preserving State)

Use this when you want to pause the network and resume later:

```bash
bash stop-network.sh
```

Or manually:
```bash
bash _scripts/stop-all-inner-peers.sh              # Stop peer1, peer2
docker compose -f docker-compose.peer.all.yaml stop
docker compose -f docker-compose.orderer.all.yaml stop
docker compose -f docker-compose.configtx.all.yaml stop
docker compose stop
```

**What's preserved:** Ledger data, installed chaincode, crypto material, channel state
**To resume:** Start containers in the same order (CAs → Orderers → Peers)

### Step 0: Complete Teardown (Destructive)

⚠ **WARNING:** This removes all containers and deletes ledger data!

```bash
bash teardown-network.sh
```

The script will automatically verify all containers are removed. Or manually:
```bash
bash _scripts/stop-all-inner-peers.sh
docker compose -f docker-compose.peer.all.yaml down
docker compose -f docker-compose.orderer.all.yaml down
docker compose -f docker-compose.configtx.all.yaml down
docker compose down
```

**What's lost:** Ledger data, installed chaincode
**What's preserved:** Crypto material (certificates)

**Verify complete teardown:**
```bash
# Check for any remaining Fabric containers (should return empty)
docker ps -a --filter "name=orderer\|peer\|admin\|ca\|initializer\|configtx" \
  --format "table {{.Names}}\t{{.Status}}\t{{.Image}}"

# If any remain, force remove:
docker rm -f $(docker ps -a --filter "name=orderer\|peer\|admin\|ca\|initializer\|configtx" -q)
```

### Step 0: Removing Crypto Material (Complete Clean Slate)

Only run this if you want to regenerate ALL certificates from scratch:

```bash
bash clearing-cert.sh
```

Or manually:
```bash
rm -rf \
  Furnitures_Makers/crypto/server/ Furnitures_Makers/crypto/client/ \
  Wood_Supply/crypto/server/ Wood_Supply/crypto/client/ \
  Yacht_Sales/crypto/server/ Yacht_Sales/crypto/client/ \
  _shared_certs/ \
  _config_files/configtx/output/
```

**Verify clean state:**
```bash
# Should show only Dockerfile or be empty
ls Furnitures_Makers/crypto/

# Should not exist
ls _shared_certs/ 2>/dev/null
ls _config_files/configtx/output/ 2>/dev/null
```

**What `_shared_certs/` contains:**
- `furnituresmakers-msp/cacerts/` - Organization CA root certificate
- `furnituresmakers-msp/tlscacerts/` - TLS CA root certificate
- `furnituresmakers-msp/config.yaml` - NodeOU configuration (enables admin/peer/client/orderer role enforcement)
- Similar structure for `woodsupply-msp/` and `yachtsales-msp/`

These shared certs allow peers/orderers from different orgs to verify each other's identities.

### Step 0: Chaincode Storage & Lifecycle

**Source Code Location:**
- **Host path:** `../../chaincode/` (project root `/chaincode/`)
- **Container mount:** `/etc/hyperledger/chaincode/chaincode-java/`
- **Persistence:** ✓ Source code always preserved (lives on host filesystem)

**Compiled Chaincode Packages:**

When chaincode is installed on a peer, Fabric performs these steps:
1. **Build:** Compiles Java source into `.jar` (happens inside peer container)
2. **Package:** Creates `.tar.gz` containing compiled code + metadata (label, type, source hash)
3. **Sign:** Package is cryptographically signed by installing organization's identity
4. **Store:** Saved in peer container at `/var/hyperledger/production/lifecycle/chaincodes/<package-id>.tar.gz`

The **package ID** is a hash of the package contents - identical source code produces identical package IDs across all peers.

**Reusability across network restarts:**

| Scenario | Chaincode Package Status | Action Required |
|----------|-------------------------|-----------------|
| `stop-network.sh` (preserving) | ✓ **Preserved** in stopped containers | Just restart containers - chaincode remains installed |
| `teardown-network.sh` (destructive) | ✗ **Lost** (containers removed) | Must re-install: package → install → approve → commit |

**Why packages can't be manually reused after teardown:**
- Packages are stored inside container filesystem (not mounted to host)
- Each package is tied to the peer's identity that signed it
- Fabric tracks installation via internal database (also lost on teardown)
- Even if you saved the `.tar.gz`, you'd need to re-install and re-approve across all orgs

**After teardown, you must:**
1. Re-package chaincode from source (generates new signatures)
2. Re-install on each peer
3. Re-approve for each organization
4. Re-commit the chaincode definition to the channel

The source code in `/chaincode/` remains unchanged, but the installation process must be repeated.

### Step 0: Copy Scripts into Docker Build Contexts

Several Dockerfiles `COPY` scripts from `_scripts/` into their build contexts. These must be copied before running `docker compose build`.

**Option 1:** Use helper scripts (recommended - they copy + run automatically):
```bash
bash _scripts/run-orderer.sh       # Copies scripts → builds images → starts all orderers
bash _scripts/run-anchor-peers.sh  # Copies scripts → builds images → starts anchor peers
bash _scripts/run-configtx-all.sh  # Copies scripts → builds images → runs configtx
```

**Option 2:** Manual copy (if building directly with docker compose):
```bash
# For configtx containers
cp -f _scripts/creating-channel.sh _config_files/configtx/
cp -f _scripts/healthcheck-admin.sh _config_files/configtx/

# For anchor peer containers
cp -f _scripts/start-anchor-peer.sh _config_files/peer/
cp -f _scripts/add-anchor-peers.sh _config_files/peer/

# For regular peer containers (inner peers)
cp -f _scripts/start-peer.sh _config_files/peer/
cp -f _scripts/fetch-block-as-peer.sh _config_files/peer/

# For orderer containers
cp -f _scripts/start-orderer.sh _config_files/orderer/
```

**Helper scripts overview:**

| Script | Purpose |
|--------|---------|
| `run-orderer.sh` | Copy scripts → build images → start all 3 orderers |
| `run-anchor-peers.sh` | Copy scripts → build images → start anchor peers (peer0) |
| `run-configtx-all.sh` | Copy scripts → build images → run genesis + channel join |
| `run-all-inner-peers.sh` | Start peer1 + peer2 for all orgs (uses `run-peer.sh`) |
| `run-peer.sh` | Start a single inner peer (generates .env, runs compose) |
| `stop-all-inner-peers.sh` | Stop all peer1/peer2 containers | 

### Step 1: Start CAs (Phase 1: TLS + Phase 2: Org identity)

**Note:** There is no separate helper script for this step - it's handled directly by `docker compose up` in the main `infrastructure/ca/` directory.

```bash
docker compose up --build -d
```

This starts 6 containers (2 per org):

**Phase 1 - TLS Initializer** (e.g. `initializer_furnituresmakers`):
- Starts TLS CA server with bootstrap identity `tlsadmin:tlsadminpw`
- Runs `enroll-tls-new.sh` which:
  - Registers all identities (admin, peer0, peer1, peer2, orderer0, *_api users)
  - Enrolls each identity to generate TLS certificates in `crypto/client/tls-ca/<identity>/`
  - Copies Org CA server's TLS cert/key to `crypto/server/ca/tls/` (needed for Phase 2)
- Kills TLS CA server and exits (container exits with code 0)
- TLS CA is temporary - once certs are issued, it's no longer needed

**Phase 2 - Org CA** (e.g. `furnituresmakers-ca`):
- Waits for Phase 1 to complete (depends_on condition)
- **First start (enrollment phase):**
  - Starts Org CA server with bootstrap identity `admin:adminpw`
  - Runs `enroll-ca-new.sh` which:
    - Enrolls admin identity from CA
    - Fixes admin NodeOU: `fabric-ca-client identity modify admin --type admin` (critical for channel operations)
    - Re-enrolls admin to get updated MSP with correct OU=admin
    - Registers + enrolls all other identities (peers, orderer, api users) in `crypto/client/ca/<identity>/`
    - Calls `share-certs.sh` which:
      - Exports CA root cert to `_shared_certs/<org>-msp/cacerts/`
      - Exports TLS CA root cert to `_shared_certs/<org>-msp/tlscacerts/`
      - Generates `config.yaml` (NodeOU configuration) in `_shared_certs/<org>-msp/`
      - Writes TLS env vars to `_env/env` file
  - Kills the initial CA server with `pkill fabric-ca-server`
- **Second start (mutual TLS mode):**
  - Sources `_env/env` which contains:
    - `FABRIC_CA_SERVER_TLS_ENABLED=true`
    - `FABRIC_CA_SERVER_TLS_CERTFILE=/etc/hyperledger/server/ca/tls/cert.pem`
    - `FABRIC_CA_SERVER_TLS_KEYFILE=/etc/hyperledger/server/ca/tls/key.pem`
  - Restarts `fabric-ca-server` with mutual TLS enabled
  - CA now requires clients to present valid TLS certificates for all connections
  - Container continues running in this state

**Why the restart is necessary:** The Org CA server needs its own TLS certificate before it can enable TLS, but it can't issue its own certificate (chicken-and-egg problem). Solution: Phase 1 TLS CA issues the cert, then Phase 2 restarts with TLS enabled using that cert.

**What gets populated:**
- `Furnitures_Makers/crypto/client/` - All enrolled identity MSPs (admin, peers, orderer, api users)
- `Furnitures_Makers/crypto/server/` - CA server data (home directory, database, TLS certs)
- `_shared_certs/` - **Exported MSP configs for cross-org verification** (filled during this step)

**Wait ~30 seconds**, then verify:
```bash
docker ps -a \
  --filter "name=initializer" \
  --filter "name=ca" \
  --format "table {{.Names}}\t{{.Status}}"
```

Expected:
- 3 initializers: `Exited (0)` (completed successfully)
- 3 org CAs: `Up` (running with mutual TLS enabled)

Verify shared certs were created:
```bash
ls _shared_certs/
```
Expected: `furnituresmakers-msp/  woodsupply-msp/  yachtsales-msp/`

Verify each MSP contains required files:
```bash
ls _shared_certs/furnituresmakers-msp/
```
Expected: `cacerts/  tlscacerts/  config.yaml`

If a CA failed, check logs:
```bash
docker logs initializer_furnituresmakers 2>&1 | tail -50
docker logs furnituresmakers-ca 2>&1 | tail -50
```

### Step 2: Create genesis block & initialize channel

Use the helper script (recommended):
```bash
bash _scripts/run-configtx-all.sh
```

Or manually:
```bash
# Copy latest scripts to build context
cp -f _scripts/creating-channel-with-retry.sh _config_files/configtx/
cp -f _scripts/healthcheck-admin-smart.sh _config_files/configtx/

# Start all 3 admin containers
docker compose -f docker-compose.configtx.all.yaml up --build -d
```

This starts 3 admin containers (one per org) that run `creating-channel-with-retry.sh`, which:
1. **Creates genesis block** via `configtxgen` (only `admin.furnituresmakers.com` creates it - others skip if it exists)
2. **Attempts osnadmin channel join** with retry logic (30 attempts, 5s intervals)
   - If orderers are running → joins channel successfully
   - If orderers not started yet → retries for up to 150 seconds, then times out gracefully
3. **Healthcheck** (`healthcheck-admin-smart.sh`) considers container healthy once genesis block exists

**Container dependencies:**
- `admin.woodsupply.com` waits for `admin.furnituresmakers.com` to be healthy
- `admin.yachtsales.com` waits for `admin.furnituresmakers.com` to be healthy
- This ensures only FurnituresMakers creates the genesis block (others wait)

**Wait ~10 seconds**, verify genesis block:
```bash
ls -la _config_files/configtx/output/genesis_block_YFW.pb
```
Expected: ~40KB file.

**Check container status:**
```bash
docker ps -a \
  --filter "name=admin" \
  --format "table {{.Names}}\t{{.Status}}"
```

Expected states:
- **If orderers NOT started yet:** Containers stay `Up` (retry loop active or waiting in bash shell after timeout)
- **If orderers running:** Containers `Up` and healthy (channel join succeeded)

**Do NOT stop configtx containers yet** - they can be used later to verify channel join after orderers start.

### Step 6: Start orderers

Use the helper script (recommended):
```bash
bash _scripts/run-orderer.sh
```

Or manually:
```bash
# Copy scripts to build context
cp -f _scripts/start-orderer.sh _config_files/orderer/

# Start all 3 orderers
docker compose -f docker-compose.orderer.all.yaml up --build -d
```

**Modern Fabric v3 architecture:**
- Orderers start with **0 channels** (no system channel)
- They **wait** for `osnadmin channel join` API calls
- Genesis block is **delivered via osnadmin API**, not read from filesystem
- `General.GenesisFile = ""` (confirmed in logs)

This means orderers can start **before or after** Step 2 (genesis block creation).

**Wait ~10 seconds**, verify:
```bash
docker ps --filter "name=orderer" --format "table {{.Names}}\t{{.Status}}"
```
Expected: 3 orderers `Up`:
- `orderer0.furnituresmakers.com`
- `orderer0.woodsupply.com`
- `orderer0.yachtsales.com`

If an orderer crashes, check logs:
```bash
docker logs orderer0.furnituresmakers.com 2>&1 | tail -30
```

**Check channel participation:**
```bash
# Should show "number of application channels: 0" initially
docker logs orderer0.woodsupply.com 2>&1 | grep "application channels"
```

**After configtx containers complete channel join** (Step 2 or Step 7), verify orderers joined:
```bash
docker exec admin.furnituresmakers.com osnadmin channel list \
  -o orderer0.furnituresmakers.com:9443 \
  --ca-file $OSN_TLS_CA_ROOT_CERT \
  --client-cert $ADMIN_TLS_SIGN_CERT \
  --client-key $ADMIN_TLS_PRIVATE_KEY
```
Expected output should show `yfw-channel` with `consensusRelation: "consenter"` status.

### Step 7: Verify orderers joined channel

**If you started orderers BEFORE Step 2 (genesis block creation):**
- Configtx containers automatically handled channel join with retry logic
- Skip to verification below

**If you started orderers AFTER Step 2:**
- Configtx containers timed out waiting for orderers (expected)
- You can either:
  - **Option A:** Restart configtx containers to retry channel join
  - **Option B:** Manually verify orderers discovered the channel via Raft consensus

**Option A - Restart configtx containers:**
```bash
docker compose -f docker-compose.configtx.all.yaml restart
```

Wait ~15 seconds for retry logic to complete, then verify.

**Verification - Check all 3 orderers joined:**
```bash
docker logs admin.furnituresmakers.com 2>&1 | grep -E "(Successfully joined|already exists)"
docker logs admin.woodsupply.com 2>&1 | grep -E "(Successfully joined|already exists)"
docker logs admin.yachtsales.com 2>&1 | grep -E "(Successfully joined|already exists)"
```
Expected: `✓ Successfully joined channel yfw-channel` or `✓ Channel already joined` for each.

**Alternative - Check orderer channel list:**
```bash
# Check each orderer has yfw-channel
docker exec admin.furnituresmakers.com osnadmin channel list \
  -o orderer0.furnituresmakers.com:9443 \
  --ca-file /etc/hyperledger/_shared_certs/furnituresmakers-msp/orderer/orderer0/msp/tls/tlscacerts/tls-ca-cert.pem \
  --client-cert /etc/hyperledger/_shared_certs/furnituresmakers-msp/admin/admin/msp/tls/signcerts/cert.pem \
  --client-key /etc/hyperledger/_shared_certs/furnituresmakers-msp/admin/admin/msp/tls/keystore/key.pem
```
Expected: JSON showing `yfw-channel` with `"consensusRelation": "consenter"`

**Clean up configtx containers (optional):**
```bash
docker compose -f docker-compose.configtx.all.yaml down
```
Note: You can leave them running - they're idle after completing their job.

### Step 8: Start anchor peers (peer0 for each org)

```bash
docker compose -f docker-compose.peer.all.yaml up --build -d
```

**Wait ~20 seconds**, verify:
```bash
docker ps --filter "name=peer0" --format "table {{.Names}}\t{{.Status}}"
```
Expected: 3 peers `Up`:
- `peer0.furnituresmakers.com`
- `peer0.woodsupply.com`
- `peer0.yachtsales.com`

Each peer's startup command (`add-anchor-peers.sh`) does:
1. Fetch channel genesis block from its orderer
2. Join the `yfw-channel`
3. Package, install, approve chaincode `basic`
4. **FurnituresMakers only**: commit chaincode + invoke `Initialize`

Check peer logs for success:
```bash
docker logs peer0.furnituresmakers.com 2>&1 | tail -40
```
Look for: "Joining channel..." and "Chaincode successfully installed and approved."

### Step 9: Verify full infrastructure

```bash
docker ps --format "table {{.Names}}\t{{.Status}}" | grep -E "(ca|orderer|peer)"
```

Expected: **3 CAs + 3 orderers + 3 anchor peers = 9 containers** all `Up`.

### Step 10 (optional): Start inner peers (peer1, peer2)

```bash
cd infrastructure/ca/_scripts && bash run-all-inner-peers.sh
```

This starts peer1 and peer2 for each org (6 more containers) using `docker-compose.peer.yaml` with dynamically generated `.env` files. Each inner peer:
- Gets gossip bootstrap pointing to peer0 and other peers of the same org
- Fetches channel block and joins `yfw-channel`
- Sleeps 15s between each peer start

---

## TEARDOWN (full)

**Recommended** - Use the teardown script (includes automatic verification):
```bash
bash teardown-network.sh
```

Or **manual teardown**:
```bash
# Stop inner peers
bash _scripts/stop-all-inner-peers.sh

# Stop anchor peers
docker compose -f docker-compose.peer.all.yaml down

# Stop orderers
docker compose -f docker-compose.orderer.all.yaml down

# Stop configtx (if running)
docker compose -f docker-compose.configtx.all.yaml down

# Stop CAs
docker compose down
```

**Verify all containers removed:**
```bash
# Should return no containers
docker ps -a --filter "name=orderer\|peer\|admin\|ca\|initializer\|configtx" \
  --format "table {{.Names}}\t{{.Status}}\t{{.Image}}"

# If any remain, force remove them
docker rm -f $(docker ps -a --filter "name=orderer\|peer\|admin\|ca\|initializer\|configtx" -q) 2>/dev/null
```

**Clean all crypto** (optional - only if you want fresh certs):
```bash
bash clearing-cert.sh

# Or manually:
rm -rf \
  Furnitures_Makers/crypto/server/ Furnitures_Makers/crypto/client/ \
  Wood_Supply/crypto/server/ Wood_Supply/crypto/client/ \
  Yacht_Sales/crypto/server/ Yacht_Sales/crypto/client/ \
  _shared_certs/ \
  _config_files/configtx/output/
```

---

## RESTART (without re-generating crypto)

If crypto is already generated and you just want to restart everything:

```bash
# Tear down
docker compose -f docker-compose.peer.all.yaml down 2>/dev/null
docker compose -f docker-compose.orderer.all.yaml down 2>/dev/null
docker compose -f docker-compose.configtx.all.yaml down 2>/dev/null
docker compose down 2>/dev/null

# Start CAs (they detect existing crypto and just start the server)
docker compose up --build -d
# Wait ~30s for CAs

# Start orderers
docker compose -f docker-compose.orderer.all.yaml up --build -d
# Wait ~10s

# Join orderers to channel
docker compose -f docker-compose.configtx.all.yaml up --build -d
# Wait ~15s, then stop configtx
docker compose -f docker-compose.configtx.all.yaml down

# Start peers
docker compose -f docker-compose.peer.all.yaml up --build -d
```

> **Note**: This may not work cleanly if the CA databases are gone. For a guaranteed clean start, do the full teardown+clean from Steps 1-2 first.

---

## CERTIFICATE & KEY VERIFICATION

### Understanding Mutual TLS in Hyperledger Fabric

Each Fabric component (peer, orderer, CA) uses **two separate certificate/key pairs**:

1. **Organization MSP Identity** (`msp/signcerts/cert.pem` + `msp/keystore/key.pem`)
   - Used for: Transaction signing, identity verification, MSP validation
   - Issued by: Organization CA
   - OU (Organizational Unit): `peer`, `orderer`, `admin`, or `client`

2. **TLS Transport Encryption** (`msp/tls/signcerts/cert.pem` + `msp/tls/keystore/key.pem`)
   - Used for: Network communication encryption (gRPC, HTTPS)
   - Issued by: TLS CA
   - Purpose: Secure channel establishment between components

### Verifying Certificate/Key Pairs Match

A certificate and private key are a valid pair when their **public keys match**. Use these commands to verify:

#### Extract and compare public key hashes:

```bash
# Extract public key from private key and hash it
openssl ec -in path/to/keystore/key.pem -pubout 2>/dev/null | openssl md5

# Extract public key from certificate and hash it
openssl x509 -in path/to/signcerts/cert.pem -pubkey -noout 2>/dev/null | openssl md5

# If hashes match → valid pair ✓
# If hashes differ → MISMATCH ✗
```

**Example - Verifying orderer TLS cert/key:**
```bash
ORG=furnituresmakers  # or woodsupply, yachtsales

# Check TLS private key hash
openssl ec -in _shared_certs/${ORG}-msp/orderer/orderer0/msp/tls/keystore/key.pem -pubout 2>/dev/null | openssl md5

# Check TLS certificate public key hash
openssl x509 -in _shared_certs/${ORG}-msp/orderer/orderer0/msp/tls/signcerts/cert.pem -pubkey -noout 2>/dev/null | openssl md5

# These MUST output the same hash
```

**Example - Verifying MSP identity cert/key:**
```bash
ORG=furnituresmakers

# Check MSP private key hash
openssl ec -in _shared_certs/${ORG}-msp/orderer/orderer0/msp/keystore/key.pem -pubout 2>/dev/null | openssl md5

# Check MSP certificate public key hash
openssl x509 -in _shared_certs/${ORG}-msp/orderer/orderer0/msp/signcerts/cert.pem -pubkey -noout 2>/dev/null | openssl md5
```

### Inspecting Certificate Details

```bash
# View full certificate details
openssl x509 -in path/to/cert.pem -text -noout

# Check certificate validity dates
openssl x509 -in path/to/cert.pem -noout -dates

# View certificate subject and issuer
openssl x509 -in path/to/cert.pem -noout -subject -issuer

# Check certificate SANs (Subject Alternative Names)
openssl x509 -in path/to/cert.pem -noout -ext subjectAltName
```

**Example - Verify orderer certificate details:**
```bash
openssl x509 -in _shared_certs/furnituresmakers-msp/orderer/orderer0/msp/signcerts/cert.pem -text -noout | grep -A5 "Subject:"

# Should show:
# Subject: C=PL, ST=Swietokrzyskie, L=Kielce, O=FurnituresMakers, OU=orderer, CN=orderer0
```

### Common Certificate/Key Issues

#### Issue 1: Multiple keys in keystore (wrong key selected)

**Symptom:** `tls: private key does not match public key`

**Diagnosis:**
```bash
# List all keys in the keystore
ls -la Furnitures_Makers/crypto/client/tls-ca/orderer0/msp/keystore/

# If multiple files exist (e.g., key.pem + *_sk):
# - *_sk file is the actual enrollment key
# - key.pem might be old/wrong

# Check which one matches the certificate
openssl ec -in Furnitures_Makers/crypto/client/tls-ca/orderer0/msp/keystore/0edb...699_sk -pubout 2>/dev/null | openssl md5
openssl ec -in Furnitures_Makers/crypto/client/tls-ca/orderer0/msp/keystore/key.pem -pubout 2>/dev/null | openssl md5
openssl x509 -in _shared_certs/furnituresmakers-msp/orderer/orderer0/msp/tls/signcerts/cert.pem -pubkey -noout 2>/dev/null | openssl md5
```

**Fix:**
```bash
# Copy the correct *_sk file to replace wrong key.pem
cp Furnitures_Makers/crypto/client/tls-ca/orderer0/msp/keystore/ACTUAL_HASH_sk \
   _shared_certs/furnituresmakers-msp/orderer/orderer0/msp/tls/keystore/key.pem
```

#### Issue 2: Orphaned key without certificate

**Symptom:** `KeyMaterial not found in SigningIdentityInfo` or cert/key timestamps differ significantly

**Diagnosis:**
```bash
# Check creation timestamps
stat -c "%y %n" Yacht_Sales/crypto/client/ca/orderer0/msp/keystore/key.pem
stat -c "%y %n" Yacht_Sales/crypto/client/ca/orderer0/msp/signcerts/cert.pem

# If timestamps differ by hours/days → enrollment failed after key generation
```

**Fix:**
```bash
# Re-enroll the identity (requires CA running)
# OR regenerate all crypto with clearing-cert.sh
```

#### Issue 3: Certificate expired

**Diagnosis:**
```bash
# Check certificate validity period
openssl x509 -in path/to/cert.pem -noout -dates

# Example output:
# notBefore=Feb 11 07:49:00 2026 GMT
# notAfter=Feb 11 07:54:00 2027 GMT  ← Expiry date
```

**Fix:** Re-enroll the identity to get a new certificate with updated validity period.

### Verifying Complete Orderer Configuration

Check all certificates/keys for an orderer before starting:

```bash
ORG=furnituresmakers  # or woodsupply, yachtsales

echo "=== TLS Certificate/Key Pair ==="
TLS_KEY_HASH=$(openssl ec -in _shared_certs/${ORG}-msp/orderer/orderer0/msp/tls/keystore/key.pem -pubout 2>/dev/null | openssl md5 | awk '{print $2}')
TLS_CERT_HASH=$(openssl x509 -in _shared_certs/${ORG}-msp/orderer/orderer0/msp/tls/signcerts/cert.pem -pubkey -noout 2>/dev/null | openssl md5 | awk '{print $2}')

if [ "$TLS_KEY_HASH" = "$TLS_CERT_HASH" ]; then
  echo "✓ TLS cert/key MATCH: $TLS_KEY_HASH"
else
  echo "✗ TLS MISMATCH! Key: $TLS_KEY_HASH, Cert: $TLS_CERT_HASH"
fi

echo ""
echo "=== MSP Identity Certificate/Key Pair ==="
MSP_KEY_HASH=$(openssl ec -in _shared_certs/${ORG}-msp/orderer/orderer0/msp/keystore/key.pem -pubout 2>/dev/null | openssl md5 | awk '{print $2}')
MSP_CERT_HASH=$(openssl x509 -in _shared_certs/${ORG}-msp/orderer/orderer0/msp/signcerts/cert.pem -pubkey -noout 2>/dev/null | openssl md5 | awk '{print $2}')

if [ "$MSP_KEY_HASH" = "$MSP_CERT_HASH" ]; then
  echo "✓ MSP cert/key MATCH: $MSP_KEY_HASH"
else
  echo "✗ MSP MISMATCH! Key: $MSP_KEY_HASH, Cert: $MSP_CERT_HASH"
fi

echo ""
echo "=== Certificate Details ==="
echo "TLS Cert Subject:"
openssl x509 -in _shared_certs/${ORG}-msp/orderer/orderer0/msp/tls/signcerts/cert.pem -noout -subject

echo ""
echo "MSP Cert Subject:"
openssl x509 -in _shared_certs/${ORG}-msp/orderer/orderer0/msp/signcerts/cert.pem -noout -subject

echo ""
echo "MSP Cert OU (should be 'orderer'):"
openssl x509 -in _shared_certs/${ORG}-msp/orderer/orderer0/msp/signcerts/cert.pem -noout -subject | grep -o "OU=[^,]*"
```

### Verifying Mutual TLS Trust Chain

In mutual TLS, each party must trust the other's CA root certificate.

**Check orderer's client CA trust:**
```bash
ORG=furnituresmakers

# Orderers must trust admin TLS certs for osnadmin API
ls -la _shared_certs/${ORG}-msp/admin/admin/msp/tls/tlscacerts/tls-ca-cert.pem

# This TLS CA cert is referenced in orderer's orderer.yaml:
# Admin.TLS.ClientRootCAs = [/etc/hyperledger/_shared_certs/${ORG}-msp/admin/admin/msp/tls/tlscacerts/tls-ca-cert.pem]
```

**Verify all orgs have each other's CA certs:**
```bash
# Each orderer should have all 3 org CA certs for cross-org verification
ls -la _shared_certs/furnituresmakers-msp/orderer/orderer0/msp/cacerts/
# Should contain: ca-cert.pem, woodsupply-ca-cert.pem, yachtsales-ca-cert.pem
```

### Quick Certificate Health Check Script

Save this as `check-certs.sh`:

```bash
#!/bin/bash

ORG=${1:-furnituresmakers}

check_pair() {
  local KEY_PATH=$1
  local CERT_PATH=$2
  local NAME=$3

  if [ ! -f "$KEY_PATH" ]; then
    echo "✗ $NAME: Key not found at $KEY_PATH"
    return 1
  fi

  if [ ! -f "$CERT_PATH" ]; then
    echo "✗ $NAME: Cert not found at $CERT_PATH"
    return 1
  fi

  KEY_HASH=$(openssl ec -in "$KEY_PATH" -pubout 2>/dev/null | openssl md5 | awk '{print $2}')
  CERT_HASH=$(openssl x509 -in "$CERT_PATH" -pubkey -noout 2>/dev/null | openssl md5 | awk '{print $2}')

  if [ "$KEY_HASH" = "$CERT_HASH" ]; then
    echo "✓ $NAME: MATCH ($KEY_HASH)"
  else
    echo "✗ $NAME: MISMATCH (Key: $KEY_HASH, Cert: $CERT_HASH)"
  fi
}

echo "Checking certificates for $ORG orderer0..."
echo ""

check_pair \
  "_shared_certs/${ORG}-msp/orderer/orderer0/msp/tls/keystore/key.pem" \
  "_shared_certs/${ORG}-msp/orderer/orderer0/msp/tls/signcerts/cert.pem" \
  "TLS"

check_pair \
  "_shared_certs/${ORG}-msp/orderer/orderer0/msp/keystore/key.pem" \
  "_shared_certs/${ORG}-msp/orderer/orderer0/msp/signcerts/cert.pem" \
  "MSP"
```

**Usage:**
```bash
bash check-certs.sh furnituresmakers
bash check-certs.sh woodsupply
bash check-certs.sh yachtsales
```

---

## KEY FILES REFERENCE

### Docker Compose files

| File | Purpose |
|------|---------|
| `docker-compose.yaml` | Main entry - includes 3 org compose files |
| `docker-compose.furnituresmakers.yaml` | FurnituresMakers TLS init + Org CA |
| `docker-compose.woodsupply.yaml` | WoodSupply TLS init + Org CA |
| `docker-compose.yachtsales.yaml` | YachtSales TLS init + Org CA |
| `docker-compose.orderer.all.yaml` | All 3 orderers |
| `docker-compose.peer.all.yaml` | All 3 anchor peers (peer0) |
| `docker-compose.peer.yaml` | Template for inner peers (peer1, peer2) |
| `docker-compose.configtx.all.yaml` | Genesis block + osnadmin channel join |
| `docker-compose.orderer.yaml` | Template for individual orderer |
| `docker-compose.configtx.yaml` | Template for individual configtx |

### Enrollment & bootstrap scripts (`_scripts/`)

| Script | Runs in | Purpose |
|--------|---------|---------|
| `enroll-tls-new.sh` | TLS initializer container | Register+enroll all TLS identities, copy TLS cert for org CA |
| `enroll-ca-new.sh` | Org CA container | Enroll admin (fix type to admin), register+enroll all org identities |
| `share-certs.sh` | Org CA container (called by enroll-ca-new.sh) | Export certs to `_shared_certs/`, generate NodeOU config.yaml, write env file |
| `users_array.sh` | Sourced by other scripts | Defines user list and org list |

### Runtime scripts (copied to build context)

| Script | Copied to | Purpose |
|--------|-----------|---------|
| `start-orderer.sh` | `_config_files/orderer/` | Copy config.yaml + other-org CA certs to orderer MSP |
| `start-anchor-peer.sh` | `_config_files/peer/` | Copy config.yaml + other-org CA certs to peer MSP |
| `add-anchor-peers.sh` | `_config_files/peer/` | Fetch block, join channel, install+approve chaincode |
| `start-peer.sh` | `_config_files/peer/` | Same as start-anchor-peer.sh but for inner peers |
| `fetch-block-as-peer.sh` | `_config_files/peer/` | Fetch channel block for inner peer join |
| `creating-channel.sh` | `_config_files/configtx/` | Run configtxgen + osnadmin channel join |
| `healthcheck-admin.sh` | `_config_files/configtx/` | Healthcheck for admin.furnituresmakers.com |

### Helper scripts (`_scripts/`)

| Script | Purpose |
|--------|---------|
| `run-all-inner-peers.sh` | Start peer1+peer2 for all 3 orgs |
| `run-peer.sh` | Start a single inner peer (generates .env, runs docker-compose.peer.yaml) |
| `run-anchor-peers.sh` | Copy scripts + start docker-compose.peer.all.yaml |
| `run-orderer.sh` | Copy scripts + start a single orderer |
| `run-configtx-all.sh` | Copy scripts + start docker-compose.configtx.all.yaml |
| `stop-all-inner-peers.sh` | Stop all peer1/peer2 containers |

### Certificate paths (inside containers)

| Path | Content |
|------|---------|
| `/etc/hyperledger/server/tls-ca/ca-cert.pem` | TLS CA root cert |
| `/etc/hyperledger/server/ca/ca-cert.pem` | Org CA root cert |
| `/etc/hyperledger/server/ca/tls/cert.pem` | Org CA server TLS cert (from Phase 1) |
| `/etc/hyperledger/server/ca/tls/key.pem` | Org CA server TLS key (from Phase 1) |
| `/etc/hyperledger/client/tls_root_cert/tls-ca-cert.pem` | TLS root cert (client-side copy) |
| `/etc/hyperledger/client/tls-ca/<user>/msp/` | TLS enrollment MSP per user |
| `/etc/hyperledger/client/ca/<user>/msp/` | Org CA enrollment MSP per user |
| `/etc/_shared_certs/<orgname>-msp/` | Shared certs directory (mounted to host `_shared_certs/`) |

---

## TROUBLESHOOTING

### CAs

**Initializer exits with non-zero code:**
```bash
docker logs initializer_furnituresmakers 2>&1 | tail -50
```
Common cause: CRLF in scripts. Fix: `perl -pi -e 's/\r\n/\n/g' _scripts/*.sh`

**Org CA fails to start:**
Check that TLS cert was created in Phase 1:
```bash
ls -la Furnitures_Makers/crypto/server/ca/tls/
```
Should have `cert.pem` and `key.pem`.

**`_api` users fail enrollment with `hf.iban` attribute error:**
Known issue - not blocking for infrastructure. To fix: add `--id.attrs "hf.iban=<value>:ecert"` to the register call in `enroll-ca-new.sh` for `_api` users.

### Orderers

**Orderer crashes immediately:**
Usually MSP or TLS cert issue. Check logs first:
```bash
docker logs orderer0.furnituresmakers.com 2>&1 | head -30
```

Common errors:
- `tls: private key does not match public key` → TLS cert/key mismatch
- `KeyMaterial not found in SigningIdentityInfo` → MSP keystore missing or mismatched
- `failed to initialize local MSP` → MSP configuration issue

**To diagnose certificate problems, see [CERTIFICATE & KEY VERIFICATION](#certificate--key-verification) section above.**

Quick verification:
```bash
# Run the check-certs.sh script from the verification section
bash check-certs.sh furnituresmakers
bash check-certs.sh woodsupply
bash check-certs.sh yachtsales
```

### Peers

**"The identity does not contain OU [ADMIN]":**
The admin identity type is wrong. The fix is in `enroll-ca-new.sh` (identity modify + re-enroll). If you see this, clean crypto and restart from Step 2.

**Docker bind mount: directory appears empty inside container:**
WSL2 + Docker Desktop bug. Restart Docker Desktop, then retry.

### General

**CRLF line endings break scripts:**
Any file edited on Windows/NTFS may get CRLF. Always fix after editing:
```bash
perl -pi -e 's/\r\n/\n/g' <file>
```

**configtx containers: osnadmin fails first time:**
This is expected. The first run creates the genesis block only. Orderers must be started before the second configtx run can do `osnadmin channel join`.

---

## ARCHITECTURE DIAGRAM

```
docker-compose.yaml
  includes:
    docker-compose.furnituresmakers.yaml    (TLS init + Org CA, port 7054)
    docker-compose.woodsupply.yaml         (TLS init + Org CA, port 8054)
    docker-compose.yachtsales.yaml         (TLS init + Org CA, port 9054)

docker-compose.orderer.all.yaml
    orderer0.furnituresmakers.com           (port 7050, admin 9443)
    orderer0.woodsupply.com                (port 8050, admin 9443)
    orderer0.yachtsales.com                (port 9050, admin 9443)

docker-compose.configtx.all.yaml
    admin.furnituresmakers.com             (creates genesis block, joins channel)
    admin.woodsupply.com                   (joins channel, depends on furnituresmakers)
    admin.yachtsales.com                   (joins channel, depends on furnituresmakers)

docker-compose.peer.all.yaml
    peer0.furnituresmakers.com             (port 7051, anchor peer)
    peer0.woodsupply.com                   (port 8051, anchor peer)
    peer0.yachtsales.com                   (port 9051, anchor peer)

docker-compose.peer.yaml (per-peer template, used by run-peer.sh)
    peer1/peer2 for each org               (gossip peers, fetch+join channel)
```

### Two-phase CA bootstrap flow

```
Phase 1 (TLS Initializer)                Phase 2 (Org CA)
========================                  ================
fabric-ca-server start                    [waits for Phase 1]
  -b tlsadmin:tlsadminpw                  fabric-ca-server start
         |                                  -b admin:adminpw
   enroll-tls-new.sh                              |
   - register + enroll all users            enroll-ca-new.sh
   - copy TLS cert for org CA              - enroll admin
         |                                 - identity modify admin --type admin
   pkill fabric-ca-server                  - re-enroll admin (now OU=admin)
   container exits                         - register + enroll all users
                                           - share-certs.sh (export certs, NodeOU config)
                                                  |
                                           pkill fabric-ca-server
                                           source /etc/_env/env
                                           fabric-ca-server start (with mutual TLS)
```
