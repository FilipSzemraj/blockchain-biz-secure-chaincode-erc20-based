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

```
┌─────────────────────────────────────────────────────────────────┐
│                    FRONTEND (React + TypeScript + Vite)          │
│  Dashboard · Token Operations · Delivery Management · Auth      │
└───────────────────────────────┬─────────────────────────────────┘
                                │ REST API
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│              BACKEND (Spring Boot 3.4.1 · Java 21)              │
│  JWT Auth · Fabric Gateway SDK · SSE Events · PostgreSQL        │
│  Per-org instances: logistics / sales / production              │
└───────────────────────────────┬─────────────────────────────────┘
                                │ gRPC + mTLS
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│            HYPERLEDGER FABRIC NETWORK (v2.5 · Raft)             │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Yacht Sales   │  │ Furnitures   │  │ Wood Supply  │          │
│  │ Peer · CA     │  │ Makers       │  │ Peer · CA    │          │
│  │ Orderer       │  │ Peer · CA    │  │ Orderer      │          │
│  └──────────────┘  │ Orderer      │  └──────────────┘          │
│                     └──────────────┘                             │
│                                                                  │
│  Channel: yfw-channel          Containers: ~24                   │
│  Chaincode: ERC20TokenContract + IBANVoteContract                │
└──────────────────────────────────────┬──────────────────────────┘
                                       │
                          ┌────────────┘
                          ▼
                ┌─────────────────────┐
                │  Bank API           │
                │  RSA-2048 signing   │
                │  Transaction proof  │
                └─────────────────────┘
```

The system models a consortium of three organizations trading goods and settling payments via blockchain-backed tokens. Every transaction is cryptographically signed by an external bank service, verified on-chain, and recorded immutably.

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
│   └── bank-api/                 # External bank integration service
│       ├── src/                  #   RSA encryption, transaction validation
│       └── Dockerfile            #   Bank service container
│
├── frontend/                     # Web application (React + TypeScript + Vite)
│   ├── src/components/           #   Dashboard, token ops, delivery forms
│   ├── src/redux/                #   State management (Redux Toolkit)
│   └── src/config/               #   API client configuration
│
├── infrastructure/
│   └── ca/                       # Hyperledger Fabric network infrastructure
│       ├── docker-compose*.yaml  #   13 compose files (CAs, peers, orderers)
│       ├── _scripts/             #   Enrollment, channel creation, cert distribution
│       └── _config_files/        #   configtx.yaml, orderer.yaml, core.yaml
│
├── external-bank/                # Bank simulation utilities (Python)
│   ├── generating_key_pair.py    #   RSA key generation
│   └── encryption_decryption.py  #   Signing and verification
│
├── docs/                         # Documentation
│   ├── network-setup.md          #   Step-by-step network deployment guide
│   ├── architecture.md           #   Detailed system architecture
│   └── thesis-topic.md           #   Academic context
│
├── .env.example                  # Environment variable template
└── PROJECT_ANALYSIS.md           # Full project archaeology report
```

---

## Getting Started

### Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| Docker & Docker Compose | 20.10+ | Container orchestration for all services |
| Java JDK | 21 | Backend APIs build and runtime |
| Node.js & npm | 18+ | Frontend build |
| Python 3 | 3.8+ | Bank key generation utilities |

> **Note:** Java 11 and Gradle are only needed for [running chaincode tests locally](#testing). The chaincode itself is compiled inside Docker by Fabric's `fabric-javaenv` image during `peer lifecycle chaincode install`.

### 1. Clone and Set Up Environment

```bash
git clone <repository-url>
cd blockchain-biz-secure-chaincode-erc20-based

# Copy environment template and configure
cp .env.example .env
# Edit .env with your settings (JWT secret, database credentials, etc.)
```

### 2. Start the Blockchain Network

Follow the detailed guide in [`docs/network-setup.md`](docs/network-setup.md). Summary:

```bash
cd infrastructure/ca

# 1. Start Certificate Authorities
docker-compose up -d

# 2. Start orderers
docker-compose -f docker-compose.orderer.all.yaml up -d

# 3. Create channel and configure
docker-compose -f docker-compose.configtx.all.yaml up

# 4. Start anchor peers
cd _scripts && ./run-anchor-peers.sh

# 5. Start remaining peers
./run-all-inner-peers.sh
```

### 3. Chaincode Installation

No manual build step is required. The peer Docker containers mount `chaincode/` directly and Fabric's `fabric-javaenv` image compiles the Java source automatically during `peer lifecycle chaincode install`.

The anchor peer startup script (`add-anchor-peers.sh`) handles the full lifecycle — packaging, installing, approving, and committing — automatically when anchor peers start. See [`docs/network-setup.md`](docs/network-setup.md) for the full sequence.

### 4. Start Backend Services

```bash
cd backend/client-api
docker-compose up -d   # Starts PostgreSQL + API instances for all 3 orgs
```

### 5. Start Frontend

```bash
cd frontend
npm install
npm run dev            # Development server at http://localhost:5173
```

### 6. Verify

```bash
# Query the token symbol from chaincode
peer chaincode query -C yfw-channel -n basic -c '{"Args":["TokenSymbol"]}' \
  --tls --cafile $CA_FILE

# Expected: status:200
```

---

## Smart Contract API Reference

### ERC20TokenContract

| Function | Parameters | Access | Description |
|----------|-----------|--------|-------------|
| `Mint` | `to`, `amount`, `confirmationHash` | Admin | Create new tokens with bank confirmation verification |
| `Burn` | `from`, `amount` | Owner | Destroy tokens (two-phase: request → finalize) |
| `Transfer` | `from`, `to`, `amount` | Owner | Transfer tokens between accounts |
| `Approve` | `spender`, `amount` | Owner | Authorize third-party spending |
| `TransferFrom` | `from`, `to`, `amount` | Approved | Execute approved transfer |
| `BalanceOf` | `account` | Any | Query token balance |
| `TotalSupply` | — | Any | Query total token supply |
| `TokenName` | — | Any | Returns token name |
| `TokenSymbol` | — | Any | Returns token symbol |

### IBANVoteContract

| Function | Parameters | Access | Description |
|----------|-----------|--------|-------------|
| `ProposeIBAN` | `iban` | Any org member | Propose a new IBAN for the consortium |
| `VoteOnIBAN` | `proposalId` | Any org member | Cast organization's vote (3-of-3 required) |
| `GetCurrentIBAN` | — | Any | Query the current active IBAN |

### Transaction Verification Flow

```
1. Bank API receives transfer request
2. Bank signs confirmation with RSA-2048 private key
3. Client API submits signed confirmation to chaincode
4. Chaincode verifies RSA signature against stored public key
5. Chaincode checks transaction ID not already used (replay prevention)
6. Tokens minted/transferred on successful verification
7. Event emitted → SSE → Frontend updates in real time
```

---

## Network Topology

The system deploys **~24 Docker containers** across three organizations:

| Organization | Peer | Orderer | CA (Identity) | CA (TLS) | API Instances |
|-------------|------|---------|---------------|----------|---------------|
| **Yacht Sales** | peer0:9051 | orderer0 | ca-yachtsales | tls-ca-yachtsales | sales, logistics, production |
| **Furnitures Makers** | peer0:7051 | orderer0 | ca-furnituresmakers | tls-ca-furnituresmakers | sales, logistics, production |
| **Wood Supply** | peer0:8051 | orderer0 | ca-woodsupply | tls-ca-woodsupply | sales, logistics, production |

**Channel:** `yfw-channel` (Yacht-Furnitures-Wood)
**Consensus:** Raft ordering service
**Security:** Mutual TLS between all nodes, X.509 certificate-based identity

---

## Testing

### Chaincode Tests (Java 11 + Gradle required)

The chaincode requires JDK 11 and Gradle for local testing. If you don't have them installed:

```bash
# Download JDK 11 (Adoptium Temurin)
curl -sL "https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.25%2B9/OpenJDK11U-jdk_x64_linux_hotspot_11.0.25_9.tar.gz" \
  -o /tmp/jdk11.tar.gz
tar xzf /tmp/jdk11.tar.gz -C /tmp/

# Download Gradle
curl -sL "https://services.gradle.org/distributions/gradle-8.5-bin.zip" -o /tmp/gradle.zip
unzip -qo /tmp/gradle.zip -d /tmp/
```

Run tests (34 tests, 80% coverage enforced by JaCoCo):

```bash
export JAVA_HOME=/tmp/jdk-11.0.25+9
export PATH=$JAVA_HOME/bin:/tmp/gradle-8.5/bin:$PATH

cd chaincode
gradle test                    # Run unit tests
gradle jacocoTestReport        # Generate coverage report (build/reports/jacoco/)
gradle shadowJar               # Build chaincode.jar (build/libs/chaincode.jar)
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
| **RSA-2048 bank verification** | External signing | Separates financial verification from blockchain logic — the bank remains an independent trust anchor |
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
