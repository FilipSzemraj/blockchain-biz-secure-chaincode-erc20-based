# Blockchain B2B Secure — Enterprise Transaction Security System

[![Hyperledger Fabric](https://img.shields.io/badge/Hyperledger%20Fabric-2.5-blue?logo=hyperledger)](https://www.hyperledger.org/projects/fabric)
[![Java](https://img.shields.io/badge/Chaincode-Java%2011-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot%203.4-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/Frontend-React%2018-61DAFB?logo=react)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL%2015-336791?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Infrastructure-Docker-2496ED?logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-brightgreen)](LICENSE)

> A permissioned blockchain system for securing B2B transactions between enterprise organizations, built on Hyperledger Fabric with ERC-20 token management, multi-organization consensus, and cryptographic transaction verification.

---

## Architecture

![System Architecture](diagrams/architecture.svg)

The system models a consortium of three organizations trading goods and settling payments via blockchain-backed tokens. The **Client API** acts as the central hub — it proxies signing requests to the **Bank API** and submits signed transactions to the **Fabric network** via gRPC. The chaincode verifies RSA signatures against a hardcoded public key bundled in the chaincode JAR; the Bank API itself never communicates with the blockchain directly.

### Transaction Verification Flow

![Mint Verification Flow](diagrams/mint-verification-flow.svg)

<details>
<summary>Flow summary (text)</summary>

1. Client API proxies a confirmation to Bank API (`POST /api/transactions`)
2. Bank API serializes the confirmation, computes SHA-256 hash, signs hash with RSA-2048 private key
3. Bank API returns `{encryptedHash, hash, data}` — the signed confirmation JSON
4. Client API submits signed JSON to chaincode `Mint()` via Fabric Gateway (gRPC + mTLS)
5. Chaincode re-serializes confirmation, computes local SHA-256, decrypts signature with hardcoded `public_key.pem`
6. Chaincode compares hashes, checks replay prevention (`USED_TRANSACTIONS_{hash}`), validates IBANs
7. Tokens minted, `Transfer` event emitted → SSE → Frontend updates in real time
</details>

---

## Key Features

- **ERC-20 Token Standard on Hyperledger Fabric** — Mint, burn, transfer, and approve tokens with full allowance mechanism, adapted from Ethereum's ERC-20 to a permissioned network
- **Multi-Organization Consensus** — 3-org consortium (Yacht Sales, Furnitures Makers, Wood Supply) with independent CAs, peers, and orderers
- **Cryptographic Transaction Verification** — RSA-2048 signed bank confirmations verified in chaincode before token minting
- **IBAN Governance via Voting** — Propose and vote on IBAN changes with 3-of-3 organizational consensus required
- **Replay Attack Prevention** — Transaction ID deduplication on the ledger prevents double-spending
- **Real-Time Event Streaming** — Server-Sent Events (SSE) propagate blockchain events to the frontend instantly
- **Supply Chain Delivery Tracking** — Create, start, confirm, and dispute deliveries with partial delivery support
- **Role-Based Access Control** — Admin-only minting, per-organization identity management via Fabric MSP

---

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Smart Contracts** | Java · Hyperledger Fabric Chaincode Shim | 11 · 2.5 |
| **Backend** | Spring Boot · Spring Security · Fabric Gateway SDK | 3.4.1 · 1.7.0 |
| **Frontend** | React · TypeScript · Vite · Redux Toolkit | 18.3 · 5.6 · 6.0 |
| **Database** | PostgreSQL | 15 |
| **Auth** | JWT (access + refresh tokens) | — |
| **Crypto** | RSA-2048 (bank confirmations) · X.509 (Fabric identity) | — |
| **Infrastructure** | Docker · Docker Compose · Fabric CA | — |
| **Consensus** | Raft (ordering service) | — |
| **Build** | Gradle (Shadow JAR for chaincode, Spring Boot for APIs) | — |
| **Testing** | JUnit 5 · Mockito · JaCoCo (80% coverage) | — |

---

## Project Structure

```
├── chaincode/                    # Hyperledger Fabric smart contracts (Java 11)
│   ├── src/main/java/            #   ERC20TokenContract, IBANVoteContract, models
│   ├── src/test/java/            #   Unit tests (JUnit 5, Mockito)
│   ├── build.gradle              #   Shadow JAR → chaincode.jar
│   └── config/checkstyle/        #   Code quality rules
│
├── backend/
│   ├── client-api/               # Main REST API (Spring Boot, Java 21)
│   │   ├── src/                  #   Auth, blockchain integration, event streaming
│   │   ├── compose.yaml          #   PostgreSQL + API containers
│   │   └── Dockerfile            #   Production container image
│   └── bank-api/                 # RSA-2048 signing oracle (Spring Boot, Java 21)
│       ├── src/                  #   RSA signing, transaction serialization
│       ├── keys/                 #   RSA key pair (public_key.pem, private_key.pem)
│       └── Dockerfile            #   Bank service container (port 8081)
│
├── frontend/                     # Web application (React + TypeScript + Vite)
│   ├── src/components/           #   Dashboard, token ops, delivery forms
│   ├── src/redux/                #   State management (Redux Toolkit)
│   └── src/config/               #   API client configuration (axios)
│
├── infrastructure/
│   └── ca/                       # Hyperledger Fabric network infrastructure
│       ├── docker-compose*.yaml  #   13 compose files (CAs, peers, orderers)
│       ├── _scripts/             #   Enrollment, channel creation, cert distribution
│       ├── _config_files/        #   configtx.yaml, orderer.yaml, core.yaml
│       └── INFRASTRUCTURE_STEPS.md  # Comprehensive network deployment guide
│
├── external-bank/                # Bank simulation utilities (Python)
│   ├── generating_key_pair.py    #   RSA key generation
│   └── encryption_decryption.py  #   Signing and verification
│
├── diagrams/                     # Mermaid diagram sources + generated SVGs
│   ├── architecture.mermaid      #   System architecture diagram
│   └── mint-verification-flow.mermaid  # RSA signing/verification sequence
│
├── docs/                         # Documentation
│   ├── architecture.md           #   Detailed system architecture
│   └── thesis-topic.md           #   Academic context
│
└── .env.example                  # Environment variable template
```

---

## Getting Started

### Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| Docker & Docker Compose | 20.10+ | Container orchestration for all services |
| Java JDK | 21 | Backend APIs build and runtime |
| Node.js & npm | 18+ | Frontend build |
| Python 3 | 3.8+ | Bank RSA key generation utilities |

> **Note:** Java 11 and Gradle are only needed for [running chaincode tests locally](#testing). The chaincode itself is compiled inside Docker by Fabric's `fabric-javaenv` image during `peer lifecycle chaincode install`.

### Startup Order

The system has a strict startup dependency chain. Each layer requires the previous one to be fully operational:

```
1. Fabric Network        (CAs → orderers → channel → peers + chaincode)
2. Bank API              (independent — needs only Docker network)
3. Client API            (needs: Fabric peers + PostgreSQL + Bank API)
4. Frontend              (needs: Client API on :8080 + Bank API on :8081)
```

### Step 1: Clone and Configure

```bash
git clone <repository-url>
cd blockchain-biz-secure-chaincode-erc20-based

cp .env.example .env
# Edit .env with your settings (JWT secret, database credentials, etc.)
```

### Step 2: Start the Fabric Network

The blockchain infrastructure is the most complex component (~24 Docker containers). A comprehensive step-by-step guide with verification commands, troubleshooting, and architecture explanations is available in the dedicated infrastructure guide.

**[`infrastructure/ca/INFRASTRUCTURE_STEPS.md`](infrastructure/ca/INFRASTRUCTURE_STEPS.md)** — Full network deployment guide

Quick summary:

```bash
cd infrastructure/ca

# 1. Start Certificate Authorities (TLS bootstrap + org CAs)
docker compose up --build -d

# 2. Create genesis block and channel configuration
bash _scripts/run-configtx-all.sh

# 3. Start orderers (Raft consensus)
bash _scripts/run-orderer.sh

# 4. Start anchor peers (auto-installs + commits chaincode)
docker compose -f docker-compose.peer.all.yaml up --build -d

# 5. (Optional) Start inner peers
bash _scripts/run-all-inner-peers.sh
```

Verify the network is healthy:
```bash
bash check-network-health.sh
```

### Step 3: Start Bank API

<!-- TODO: detailed guide in docs/bank-api-setup.md -->

```bash
cd backend/bank-api
docker compose up -d    # Starts on port 8081
```

The Bank API auto-generates an RSA-2048 key pair on first start if `keys/` directory is empty. The matching `public_key.pem` must be bundled in `chaincode/src/main/resources/public_key.pem` before chaincode installation (already included in the repository).

### Step 4: Start Client API

<!-- TODO: detailed guide in docs/client-api-setup.md — per-org instances, crypto mounting, Fabric identity -->

```bash
cd backend/client-api
docker compose up -d    # Starts PostgreSQL + API on port 8080
```

The Client API connects to Fabric peers via gRPC+mTLS using crypto material mounted from the infrastructure layer. Each organization runs its own API instance with a distinct Fabric identity (MSP ID, certificates).

### Step 5: Start Frontend

<!-- TODO: detailed guide in docs/frontend-setup.md -->

```bash
cd frontend
npm install
npm run dev             # Development server at http://localhost:5173
```

The frontend connects to Client API (`localhost:8080`) and Bank API (`localhost:8081`) — both URLs are currently hardcoded in `src/config/`.

### Step 6: Verify End-to-End

```bash
# Query token name from chaincode (via peer container)
docker exec peer0.furnituresmakers.com peer chaincode query \
  -C yfw-channel -n basic -c '{"function":"TokenName","Args":[]}'
# Expected: IntrinsicCoin
```

---

## Smart Contract API Reference

### ERC20TokenContract

| Function | Parameters | Access | Description |
|----------|-----------|--------|-------------|
| `Mint` | `jsonContent` (signed confirmation JSON) | Admin | Create new tokens after RSA signature verification |
| `Burn` | `from`, `amount` | Owner | Destroy tokens (two-phase: request → finalize) |
| `Transfer` | `from`, `to`, `amount` | Owner | Transfer tokens between accounts |
| `Approve` | `spender`, `amount` | Owner | Authorize third-party spending |
| `TransferFrom` | `from`, `to`, `amount` | Approved | Execute approved transfer |
| `BalanceOf` | `account` | Any | Query token balance |
| `TotalSupply` | — | Any | Query total token supply |
| `TokenName` / `TokenSymbol` | — | Any | Returns `IntrinsicCoin` / `IC` |

### IBANVoteContract

| Function | Parameters | Access | Description |
|----------|-----------|--------|-------------|
| `ProposeIBAN` | `iban` | Any org member | Propose a new IBAN for the consortium |
| `VoteOnIBAN` | `proposalId` | Any org member | Cast organization's vote (3-of-3 required) |
| `GetCurrentIBAN` | — | Any | Query the current active IBAN |

---

## Network Topology

The system deploys **~24 Docker containers** across three organizations:

| Organization | Peer | Orderer | CA (Identity) | CA (TLS) |
|-------------|------|---------|---------------|----------|
| **Yacht Sales** | peer0:9051 | orderer0:9050 | ca-yachtsales:9054 | tls-ca-yachtsales |
| **Furnitures Makers** | peer0:7051 | orderer0:7050 | ca-furnituresmakers:7054 | tls-ca-furnituresmakers |
| **Wood Supply** | peer0:8051 | orderer0:8050 | ca-woodsupply:8054 | tls-ca-woodsupply |

**Channel:** `yfw-channel` (Yacht-Furnitures-Wood)
**Consensus:** Raft ordering service
**Security:** Mutual TLS between all nodes, X.509 certificate-based identity
**Docker network:** `fabric-network` (shared by all components)

---

## Testing

### Chaincode Tests

34 unit tests with 80% coverage enforced by JaCoCo. Requires JDK 11:

```bash
# Option A: Use Docker tools image (no local JDK needed)
cd chaincode
docker compose -f docker-compose.tools.yaml run --rm chaincode-tools test

# Option B: Local JDK 11 + Gradle
export JAVA_HOME=/path/to/jdk-11
cd chaincode
gradle test                    # Run unit tests
gradle jacocoTestReport        # Coverage report → build/reports/jacoco/
```

### Backend API Tests

```bash
cd backend/client-api
./gradlew test
```

---

## Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **Hyperledger Fabric over Ethereum** | Permissioned network | B2B transactions require identity verification and data privacy — public blockchains expose transaction data |
| **ERC-20 on Fabric** | Custom implementation | Fabric has no native token standard; implementing ERC-20 provides familiar semantics while leveraging Fabric's privacy features |
| **Java for chaincode** | Java 11 | Strong typing, enterprise ecosystem familiarity, official Fabric SDK support |
| **RSA-2048 bank verification** | External signing oracle | Separates financial verification from blockchain logic — the bank remains an independent trust anchor |
| **Hardcoded bank public key** | Bundled in chaincode JAR | Simpler deployment; key rotation requires chaincode upgrade (package → install → approve → commit across all orgs) |
| **3-of-3 IBAN voting** | Full consensus | All organizations must agree on payment routing changes — prevents unilateral financial decisions |
| **SSE over WebSockets** | Server-Sent Events | Simpler for unidirectional event push from blockchain; no bidirectional channel needed |

---

## Roadmap

- [x] ERC-20 token implementation (mint, burn, transfer, approve)
- [x] Multi-organization Fabric network with Raft consensus
- [x] RSA-signed bank confirmation verification
- [x] IBAN voting governance mechanism
- [x] Spring Boot REST API with JWT auth
- [x] React frontend with Redux state management
- [x] Supply chain delivery tracking
- [x] Real-time SSE event streaming
- [ ] Swagger/OpenAPI documentation for REST endpoints
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Environment variable externalization (remove hardcoded secrets)
- [ ] Database migrations (replace Hibernate auto-DDL)
- [ ] Integration test suite
- [ ] Bank public key rotation via ledger governance (eliminate chaincode redeployment)

---

## Academic Context

This system was developed as a bachelor's thesis project:

**Title:** *Design and Implementation of an Information System Increasing the Security of Business Transactions Through the Use of Blockchain Smart Contracts*

The thesis explores how permissioned blockchain networks can provide transaction integrity, non-repudiation, and automated dispute resolution for B2B commerce, with a practical implementation demonstrating these concepts in a multi-organization supply chain scenario.

---

## License

Distributed under the Apache License 2.0. See [`LICENSE`](LICENSE) for more information.

---

## Author

**Filip Szemraj**
- GitHub: [@FilipSzemraj](https://github.com/FilipSzemraj)

---

## Acknowledgments

- [Hyperledger Fabric Documentation](https://hyperledger-fabric.readthedocs.io/)
- [Hyperledger Fabric Samples](https://github.com/hyperledger/fabric-samples)
- [OpenZeppelin ERC-20 Standard](https://docs.openzeppelin.com/contracts/erc20)