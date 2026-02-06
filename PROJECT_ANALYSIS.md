# Project Archaeology & Unification Analysis

## 1. Executive Summary

This analysis examines two related directories that together constitute a **Hyperledger Fabric-based B2B transaction security system** built for a bachelor's thesis:

| Directory | Role | Key Content |
|-----------|------|-------------|
| `blockchain-biz-secure` | **Infrastructure & Application repo** | CA setup, Docker orchestration, backend APIs, frontend, network scripts |
| `blockchain-biz-secure-chaincode-erc20-based` | **Chaincode source repo** (contaminated) | ERC20 smart contract Java source + *accidentally pushed copies* of API/infra files |

**Root cause of the split:** The chaincode was intentionally separated into its own repository for cleaner development. However, an accidental `git push` from the wrong directory merged API/infrastructure files into the chaincode repo, as confirmed by the README note:

> *"d93115d - jest ostatnim poprawnym commitem, do przywrócenia w repo. Przez pomyłkę zrobiłem push nie z tego repo co trzeba..."*
> ("d93115d is the last correct commit to restore. By mistake I pushed from the wrong repo...")

---

## 2. System Architecture (from Thesis)

The system implements a decentralized application (dApp) for a 3-organization consortium:

```
┌─────────────────────────────────────────────────────────────────┐
│                    FRONTEND (React + TypeScript + Vite)          │
│  Dashboard, Token Ops, Delivery Management, Auth (JWT)          │
└───────────────────────────────┬─────────────────────────────────┘
                                │ REST API (HTTP/JSON)
                                ▼
┌───────────────────────────────────────────────────────────────┐
│              BACKEND (Spring Boot 3.4.1 + Java 21)             │
│  Auth (JWT/Spring Security) │ Fabric Gateway SDK │ SSE Events  │
│  PostgreSQL (users, tokens) │ Bank API Client                  │
│                                                                 │
│  Instances per org:  logistics_api / sales_api / production_api │
└───────────────────────────────┬─────────────────────────────────┘
                                │ gRPC + TLS/mTLS
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│            HYPERLEDGER FABRIC NETWORK (v2.5)                     │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │ YachtSales   │  │ Furnitures   │  │ WoodSupply   │           │
│  │  Peer0,1,2   │  │  Makers      │  │  Peer0,1,2   │           │
│  │  Orderer     │  │  Peer0,1,2   │  │  Orderer     │           │
│  │  CA + TLS CA │  │  Orderer     │  │  CA + TLS CA │           │
│  └──────────────┘  │  CA + TLS CA │  └──────────────┘           │
│                     └──────────────┘                              │
│                                                                   │
│  Channel: yfw-channel    Consensus: Raft    Containers: ~24      │
│                                                                   │
│  CHAINCODE (Java 11):                                             │
│    - ERC20TokenContract (mint, burn, transfer, approve)           │
│    - IBANVoteContract (propose, vote, 3-of-3 consensus)           │
└─────────────────────────────────────────────────────────────────┘
                                │
                    ┌───────────┘
                    ▼
          ┌─────────────────────┐
          │  BANK API            │
          │  (Spring Boot)       │
          │  RSA encryption      │
          │  Port 8081           │
          └─────────────────────┘
```

### Organizations (Consortium)
1. **Yacht Sales** — peer0.yachtsales.com:9051
2. **Furnitures Makers** — peer0.furnituresmakers.com:7051
3. **Wood Supply** — peer0.woodsupply.com:8051

### Data Flow
1. User interacts via React frontend
2. Frontend calls REST API on Spring Boot backend
3. Backend uses Fabric Gateway SDK to submit transactions via gRPC
4. Peers execute chaincode, orderers sequence via Raft consensus
5. Events propagated back to frontend via Server-Sent Events (SSE)
6. Bank API handles external transaction confirmations with RSA encryption

---

## 3. Directory Comparison

### 3.1 `blockchain-biz-secure` (Infrastructure & App Repository)

```
blockchain-biz-secure/
├── blockchain-biz-secure-api/
│   ├── bankAPI/              ← Bank integration API (Spring Boot)
│   │   ├── Dockerfile        ← Present here, absent in chaincode repo
│   │   └── src/...
│   ├── client_api/           ← Main backend API (Spring Boot)
│   │   ├── Dockerfile        ← Present here, absent in chaincode repo
│   │   ├── crypto/           ← Dev certificates
│   │   ├── crypto_second/    ← Secondary dev certificates
│   │   ├── blockchain/       ← Production certificates
│   │   └── src/...
│   └── client_frontend/      ← React UI
│       └── src/...
├── ca/                       ← Hyperledger Fabric CA infrastructure
│   ├── _config_files/        ← configtx.yaml, orderer.yaml, core.yaml
│   ├── _scripts/             ← Enrollment, peer startup, channel creation
│   ├── docker-compose*.yaml  ← 13 compose files for network orchestration
│   └── {org}/crypto/         ← Generated crypto material per org
├── external_bank/            ← Python RSA encryption utilities
├── fabric-ca-server-config.yaml
├── Kroki do uruchomienia sieci.docx  ← Network startup guide
├── prezentacja.docx          ← Presentation
└── README.md                 ← Points to blockchain-biz-secure-api
```

**Git history:** 14 commits, focused on infrastructure setup and thesis defense state.

### 3.2 `blockchain-biz-secure-chaincode-erc20-based` (Chaincode Repository — Contaminated)

```
blockchain-biz-secure-chaincode-erc20-based/
├── src/                      ← UNIQUE: Chaincode Java source code
│   ├── main/java/com/blockchainbiz/erc20/
│   │   ├── ERC20TokenContract.java    (982 lines - main contract)
│   │   ├── IBANVoteContract.java      (IBAN voting)
│   │   ├── ConfirmationVerifier.java  (RSA verification)
│   │   ├── ContractUtility.java       (helpers)
│   │   ├── ContractConstants.java     (org identifiers)
│   │   ├── ContractErrors.java        (error enums)
│   │   └── model/                     (Approval, Transfer, BurnRequest, etc.)
│   └── test/java/...                  (unit tests)
├── build.gradle              ← UNIQUE: Root Gradle build (Shadow JAR → chaincode.jar)
├── settings.gradle           ← UNIQUE: Project name: token_erc20
├── config/                   ← UNIQUE: Checkstyle configuration
├── public_key.pem            ← UNIQUE: RSA public key at root
│
│ ── CONTAMINATION (accidental push from wrong repo) ──
├── blockchain-biz-secure-api/  ← DUPLICATE: copy from other repo
├── ca/                         ← DUPLICATE: copy from other repo
├── external_bank/              ← DUPLICATE: copy from other repo
├── fabric-ca-server-config.yaml ← DUPLICATE
├── Kroki do uruchomienia sieci.docx ← DUPLICATE
├── prezentacja.docx            ← DUPLICATE
└── README.md                   ← Note about accidental push
```

**Git history:** 11 commits, initially focused purely on chaincode development.

### 3.3 Actual Differences Between Directories

| File/Directory | `blockchain-biz-secure` | `chaincode-erc20-based` | Status |
|---------------|------------------------|------------------------|--------|
| `src/` (chaincode) | Missing | Present | **Unique to chaincode repo** |
| `build.gradle` (root) | Missing | Present | **Unique to chaincode repo** |
| `settings.gradle` | Missing | Present | **Unique to chaincode repo** |
| `config/checkstyle/` | Missing | Present | **Unique to chaincode repo** |
| `public_key.pem` (root) | Missing | Present | **Unique to chaincode repo** |
| `bankAPI/Dockerfile` | Present | Missing | **Unique to infra repo** |
| `client_api/Dockerfile` | Present | Missing | **Unique to infra repo** |
| `blockchain-biz-secure-api/` | Original home | Accidental copy | Duplicate |
| `ca/` | Original home | Accidental copy | Duplicate |
| `external_bank/` | Original home | Accidental copy | Duplicate |

---

## 4. Subsystem Inventory

### 4.1 Chaincode (Smart Contracts)

| Attribute | Detail |
|-----------|--------|
| **Purpose** | ERC20 token management + IBAN voting governance on Hyperledger Fabric |
| **Technology** | Java 11, fabric-chaincode-shim 2.5, Gradle, Shadow JAR |
| **Location** | `chaincode-erc20-based/src/` |
| **Status** | **Complete** — 14 Java files, unit tests, 80% coverage requirement |
| **Entry Point** | `org.hyperledger.fabric.contract.ContractRouter` |
| **Build Output** | `chaincode.jar` (fat JAR via Shadow plugin) |

**Key contracts:**
- `ERC20TokenContract` — mint, burn, transfer, approve, hash verification
- `IBANVoteContract` — propose IBAN, vote (3-of-3 org consensus)

### 4.2 Client API (Main Backend)

| Attribute | Detail |
|-----------|--------|
| **Purpose** | REST API bridge between frontend and Fabric network |
| **Technology** | Java 21, Spring Boot 3.4.1, PostgreSQL 15, Fabric Gateway SDK 1.7.0, JWT |
| **Location** | `blockchain-biz-secure-api/client_api/` (both repos) |
| **Status** | **Complete** — ~60 Java classes, auth, blockchain integration, event streaming |
| **Entry Point** | `com.blockchainbiz.app.client_api.ClientApiApplication` |
| **Database** | PostgreSQL — `fabric_client_db` (User, RefreshToken, Certificate entities) |

**Key endpoints:** Token operations (mint/burn/transfer), delivery management, event subscription (SSE), auth (login/register/refresh).

### 4.3 Bank API (External Bank Integration)

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Intermediary bank service for RSA-encrypted transaction confirmations |
| **Technology** | Java 21, Spring Boot 3.4.1, RSA encryption |
| **Location** | `blockchain-biz-secure-api/bankAPI/` (both repos) |
| **Status** | **Complete** — 10 Java classes |
| **Entry Point** | `commonbank.bankapi.BankApiApplication` (port 8081) |

### 4.4 Frontend (Web Application)

| Attribute | Detail |
|-----------|--------|
| **Purpose** | User interface for token management, deliveries, and transactions |
| **Technology** | React 18.3, TypeScript 5.6, Vite 6.0, Redux Toolkit, Axios, SCSS |
| **Location** | `blockchain-biz-secure-api/client_frontend/` (both repos) |
| **Status** | **Complete** — 25+ components, Redux state management |
| **Entry Point** | `src/main.tsx` |

### 4.5 Blockchain Network Infrastructure

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Hyperledger Fabric network with 3 organizations, CAs, peers, orderers |
| **Technology** | Docker Compose, Fabric CA, Bash scripts, YAML configs |
| **Location** | `ca/` directory (both repos) |
| **Status** | **Complete** — 13 compose files, 8+ automation scripts |
| **Containers** | ~24 (peers, orderers, CAs, configtx tools) |

### 4.6 External Bank Utilities

| Attribute | Detail |
|-----------|--------|
| **Purpose** | RSA key generation and confirmation encryption/serialization |
| **Technology** | Python, RSA-2048 |
| **Location** | `external_bank/` (both repos) |
| **Status** | **Complete** — 3 Python scripts + key pair |

---

## 5. Historical Reconstruction

Based on git history, README notes, and thesis context:

1. **Phase 1 — Network Infrastructure** (`blockchain-biz-secure`):
   Created as the main project repository. Set up Fabric CA, generated certificates, configured peers/orderers, established the blockchain network.

2. **Phase 2 — Chaincode Development** (`blockchain-biz-secure-chaincode-erc20-based`):
   Created as a **separate, dedicated repository** for developing the ERC20 chaincode in isolation. This follows a common Hyperledger Fabric pattern where chaincode is developed independently and packaged as a tar.gz for installation on peers.

3. **Phase 3 — API & Frontend** (`blockchain-biz-secure`):
   Backend APIs and React frontend were developed and added to the main infrastructure repo.

4. **Phase 4 — Accidental Contamination**:
   At some point, a `git push` was executed from the wrong directory, causing the infrastructure/API files to be pushed into the chaincode repository. The author noted this in the README and identified commit `d93115d` as the last clean state of the chaincode repo.

5. **Phase 5 — Thesis Defense** (Feb 2025):
   Both repos were frozen in their current state for the thesis defense. The frontend was added as a final commit.

---

## 6. Why Two Repositories Existed

The separation was **intentional and architecturally sound**:

1. **Hyperledger Fabric workflow**: Chaincode is packaged as a standalone artifact (tar.gz/JAR) and installed on peers. It has its own build lifecycle, dependencies (fabric-chaincode-shim), and Java version (11 vs 21 for APIs). A separate repo is standard practice.

2. **Different build systems**: The chaincode uses Shadow JAR (Gradle) to produce a fat JAR with `ContractRouter` as the main class. The APIs use Spring Boot's bootJar. These are fundamentally different build targets.

3. **Different Java versions**: Chaincode targets Java 11 (Fabric constraint), while Spring Boot APIs use Java 21.

4. **Deployment separation**: Chaincode is installed directly on Fabric peers via `peer lifecycle` commands. APIs are deployed as Docker containers. Different deployment pipelines benefit from separate repos.

The problem was not the split itself — it was the accidental cross-contamination.

---

## 7. Unification Roadmap

### Option A: Monorepo (Recommended)

Unify everything into a single repository with clear module boundaries:

```
blockchain-biz-secure/
├── chaincode/                  ← From chaincode-erc20-based/src + build files
│   ├── src/
│   ├── build.gradle
│   ├── settings.gradle
│   └── config/checkstyle/
├── backend/
│   ├── client-api/             ← From blockchain-biz-secure-api/client_api
│   └── bank-api/               ← From blockchain-biz-secure-api/bankAPI
├── frontend/                   ← From blockchain-biz-secure-api/client_frontend
├── infrastructure/
│   ├── ca/                     ← From ca/
│   ├── docker/                 ← All Docker Compose files, Dockerfiles
│   └── scripts/                ← Startup/deployment scripts
├── external-bank/              ← From external_bank/
├── docs/
│   ├── network-setup.md        ← Converted from "Kroki do uruchomienia sieci.docx"
│   └── architecture.md
├── docker-compose.yml          ← Root-level full-system compose
├── Makefile                    ← Build targets for all modules
└── README.md
```

### Unification Steps

**Step 1: Establish clean baseline**
- Start from `blockchain-biz-secure` (the infrastructure repo) as the base
- Verify it's at the thesis-defense commit (`38c8753`)

**Step 2: Extract chaincode from the second repo**
- From `blockchain-biz-secure-chaincode-erc20-based` at commit `d93115d` (the last clean chaincode commit), extract:
  - `src/` directory
  - `build.gradle` (root)
  - `settings.gradle`
  - `config/` directory
  - `public_key.pem`

**Step 3: Restructure into monorepo layout**
- Move chaincode files into `chaincode/` subdirectory
- Move API projects into `backend/` subdirectory
- Move frontend into `frontend/` subdirectory
- Move CA and Docker configs into `infrastructure/`
- Move external bank utilities into `external-bank/`

**Step 4: Fix build configuration**
- Update Gradle settings to reflect new directory structure
- Update Docker Compose volume mounts and build contexts
- Update frontend API endpoint configs
- Ensure certificate paths remain valid

**Step 5: Add missing pieces**
- Convert .docx documentation to Markdown
- Add proper `.env.example` files (remove hardcoded secrets)
- Add root-level Makefile or script for full-system startup
- Restore missing Dockerfiles (from `blockchain-biz-secure`)
- Add comprehensive README with architecture diagram

**Step 6: Clean up**
- Remove duplicate crypto material directories (keep one canonical set)
- Remove `ca/trash/` directory
- Remove `logi.txt` and other development artifacts
- Ensure `.gitignore` covers all generated files

### Option B: Keep Separate (Simpler but Less Maintainable)

If monorepo feels too complex:
1. Clean the chaincode repo by resetting to `d93115d`
2. Keep the infrastructure repo as-is
3. Add clear cross-references in both READMEs
4. Accept the operational overhead of managing two repos

---

## 8. Identified Issues & Recommendations

### Security Concerns
- **JWT secret hardcoded** in `application.properties` — move to environment variable
- **Database credentials hardcoded** — use Docker secrets or env vars
- **Private keys committed to git** — `external_bank/private_key.pem`, `bankAPI/keys/private_key.pem`
- **Multiple crypto material copies** — `crypto/`, `crypto_second/`, `blockchain/` — consolidate

### Technical Debt
- **No database migrations** — relies on Hibernate `ddl-auto=update` (not production-safe)
- **No API documentation** — no Swagger/OpenAPI specs
- **Unused imports** in `ClientApiApplication.java` (Fabric SDK imports at app entry point)
- **Hardcoded vote threshold** (3) in `IBANVoteContract` — should be configurable
- **No CI/CD pipeline** — no GitHub Actions or similar

### Missing Components
- **No environment variable templates** (`.env.example`)
- **No health check endpoints** for APIs
- **No logging configuration** beyond basic Spring Boot defaults
- **No API rate limiting** or input sanitization layer

---

## 9. Network Startup Procedure (from "Kroki do uruchomienia sieci.docx")

Working directory: `blockchain-biz-secure/ca/`

1. **Start CA servers** (TLS and identity):
   ```bash
   docker-compose up
   ```

2. **Distribute NodeOUs config**:
   ```bash
   ca/_config_files/shared/spread-config.sh
   ```

3. **Start orderers**:
   ```bash
   docker-compose -f docker-compose.orderer.all.yaml up
   ```

4. **Create channel and add orderers**:
   ```bash
   docker-compose -f docker-compose.configtx.all.yaml up
   ```

5. **Start anchor peers** (from WSL/Linux due to Docker socket mapping):
   ```bash
   cd ca/_scripts
   ./run-anchor-peers.sh
   ```

6. **Start inner peers** (peer1, peer2 per org):
   ```bash
   ./run-peer.sh peer1 Furnitures_Makers 7051
   # or run all at once:
   ./run-all-inner-peers.sh
   ```

7. **Install and activate chaincode** (standard Fabric lifecycle):
   ```bash
   peer chaincode query -C yfw-channel -n basic -c '{"Args":["TokenSymbol"]}' --tls --cafile $CA_FILE
   ```

8. **Start application layer**: Backend (Spring Boot) + Frontend (React)

---

## 10. Key Commit References

### `blockchain-biz-secure` (Infrastructure Repo)
| Commit | Description | Significance |
|--------|-------------|-------------|
| `4fc6eaf` | Setting CA and generating certificates | Initial network setup |
| `39a835d` | Working blockchain network to chaincode stage | Network operational |
| `38c8753` | Thesis defense state | **Stable baseline** |
| `1d0a155` | Frontend added | Final feature |
| `2dad863` | README revision | Latest commit |

### `blockchain-biz-secure-chaincode-erc20-based` (Chaincode Repo)
| Commit | Description | Significance |
|--------|-------------|-------------|
| `d93115d` | Initialize chaincode-java repository | **Last clean chaincode commit** |
| `6f9289a` | Working blockchain network (contamination starts) | Accidental push |
| `590ffdd` | Thesis defense state | Frozen with contamination |
| `8a5fd7f` | Note about restoration | Points to d93115d |
