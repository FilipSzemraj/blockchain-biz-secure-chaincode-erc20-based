# System Architecture

## Overview

The system is a decentralized application (dApp) for a 3-organization consortium that secures B2B transactions using Hyperledger Fabric smart contracts, ERC-20 token management, and cryptographic bank verification.

![System Architecture](../diagrams/architecture.svg)

The **Client API** is the central integration point — it proxies signing requests to the **Bank API** (a standalone RSA-2048 signing oracle) and submits signed transactions to the **Fabric network** via gRPC+mTLS. The Bank API never communicates with the blockchain directly; the chaincode verifies RSA signatures against a `public_key.pem` hardcoded in the chaincode JAR.

## Organizations (Consortium)

| Organization | Peer Address | Role |
|-------------|-------------|------|
| **Yacht Sales** | peer0.yachtsales.com:9051 | Yacht manufacturing and sales |
| **Furnitures Makers** | peer0.furnituresmakers.com:7051 | Furniture production |
| **Wood Supply** | peer0.woodsupply.com:8051 | Raw material supply |

## Components

### Chaincode (Java 11)

Located in `chaincode/`. Two smart contracts deployed to `yfw-channel`:

- **ERC20TokenContract** — Token lifecycle: mint (with bank verification), burn (two-phase), transfer, approve/transferFrom. Implements replay attack prevention via transaction ID deduplication.
- **IBANVoteContract** — Governance: propose IBAN changes, vote with 3-of-3 consensus required.

### Backend (Spring Boot 3.4.1, Java 21)

Located in `backend/`. Two services:

- **client-api** — Main REST API. JWT authentication, Fabric Gateway SDK integration, Server-Sent Events for real-time blockchain event streaming. Runs per-organization instances (logistics, sales, production) each with their own Fabric identity. Proxies Bank API requests via `/api/bank/*` endpoints.
- **bank-api** — RSA-2048 signing oracle (port 8081). Receives transaction confirmations, serializes them to JSON, computes SHA-256 hash, and signs the hash with an RSA private key. Stateless — no database, no Fabric connection. The matching public key is bundled in the chaincode JAR for on-chain verification.

### Frontend (React 18, TypeScript, Vite)

Located in `frontend/`. Single-page application with:
- Redux Toolkit for state management
- Dashboard, token operations, delivery management views
- JWT-based authentication flow (access + refresh tokens)
- Connects directly to both Client API (`:8080`) and Bank API (`:8081`)

### Infrastructure (Docker)

Located in `infrastructure/ca/`. Docker Compose files for:
- Certificate Authorities (identity CA + TLS CA per organization)
- Peer nodes (up to 3 per organization: 1 anchor + 2 inner)
- Orderer nodes (Raft consensus)
- Channel creation and configuration

See [`infrastructure/ca/INFRASTRUCTURE_STEPS.md`](../infrastructure/ca/INFRASTRUCTURE_STEPS.md) for the comprehensive deployment guide.

### External Bank (Python)

Located in `external-bank/`. Utilities for:
- RSA key pair generation
- Transaction confirmation signing/verification

## Bank Verification Flow

![Mint Verification Flow](../diagrams/mint-verification-flow.svg)

The mint transaction is the only operation that requires bank verification. The flow:

1. Client API proxies a `Confirmation` object to Bank API (`POST /api/transactions`)
2. Bank API serializes the confirmation → computes SHA-256 hash → signs hash with RSA-2048 private key (SHA256withRSA)
3. Bank API returns `{encryptedHash, hash, data}` — signed confirmation JSON
4. Client API submits the signed JSON to chaincode `Mint(jsonContent)` via Fabric Gateway (gRPC+mTLS, endorsed by all 3 orgs)
5. Chaincode (`ConfirmationVerifier`):
   - Parses JSON, extracts `encryptedHash` and `data` (Confirmation)
   - Re-serializes Confirmation → computes local SHA-256 hash
   - Decrypts `encryptedHash` using hardcoded `public_key.pem` (RSA/ECB/PKCS1Padding)
   - Compares decrypted hash == local hash
6. Chaincode validates: caller MSP, admin role, replay prevention (`USED_TRANSACTIONS_{hash}`), IBAN matching (fromIBAN vs caller's `hf.iban` certificate attribute, toIBAN vs consortium IBAN)
7. Tokens minted, `Transfer` event emitted → SSE → Frontend

**Key constraint:** The bank's public key is a static resource in the chaincode JAR (`chaincode/src/main/resources/public_key.pem`). Key rotation requires a full chaincode upgrade cycle (package → install → approve → commit across all 3 orgs). No runtime key update mechanism exists.

## Data Flow

1. User interacts via React frontend
2. Frontend calls REST API on Client API backend
3. For minting: Client API proxies to Bank API for RSA signing, then submits signed JSON to chaincode
4. Client API uses Fabric Gateway SDK to submit/evaluate transactions via gRPC+mTLS
5. Peers execute chaincode, orderers sequence via Raft consensus
6. Events propagated back to frontend via Server-Sent Events (SSE)

## Network Topology

~24 Docker containers across three organizations:

| Organization | Peer | Orderer | CA (Identity) | CA (TLS) |
|-------------|------|---------|---------------|----------|
| Yacht Sales | peer0:9051 | orderer0:9050 | ca-yachtsales:9054 | tls-ca-yachtsales |
| Furnitures Makers | peer0:7051 | orderer0:7050 | ca-furnituresmakers:7054 | tls-ca-furnituresmakers |
| Wood Supply | peer0:8051 | orderer0:8050 | ca-woodsupply:8054 | tls-ca-woodsupply |

**Channel:** `yfw-channel` (Yacht-Furnitures-Wood)
**Consensus:** Raft ordering service
**Security:** Mutual TLS between all nodes, X.509 certificate-based identity
**Docker network:** `fabric-network` (shared across all components)

## Security Model

- **Identity:** X.509 certificates issued by per-organization Fabric CAs
- **Transport:** Mutual TLS (mTLS) for all gRPC communication
- **Authentication:** JWT tokens (access: 5 min, refresh: 7 days)
- **Transaction Integrity:** RSA-2048 signed bank confirmations verified in chaincode against hardcoded public key
- **Replay Prevention:** Transaction ID (SHA-256 hash) deduplication stored on the ledger
- **Governance:** 3-of-3 organizational consensus for IBAN changes
- **Bank Key Trust:** Static — public key bundled in chaincode JAR, no runtime rotation