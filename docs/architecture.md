# System Architecture

## Overview

The system is a decentralized application (dApp) for a 3-organization consortium that secures B2B transactions using Hyperledger Fabric smart contracts, ERC-20 token management, and cryptographic bank verification.

```
┌─────────────────────────────────────────────────────────────────┐
│                    FRONTEND (React + TypeScript + Vite)          │
│  Dashboard · Token Operations · Delivery Management · Auth      │
└───────────────────────────────┬─────────────────────────────────┘
                                │ REST API (HTTP/JSON)
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

- **client-api** — Main REST API. JWT authentication, Fabric Gateway SDK integration, Server-Sent Events for real-time blockchain event streaming. Runs per-organization instances (logistics, sales, production) each with their own Fabric identity.
- **bank-api** — External bank simulation. RSA-2048 key pair for signing transaction confirmations. Runs on port 8081.

### Frontend (React 18, TypeScript, Vite)

Located in `frontend/`. Single-page application with:
- Redux Toolkit for state management
- Dashboard, token operations, delivery management views
- JWT-based authentication flow (access + refresh tokens)

### Infrastructure (Docker)

Located in `infrastructure/ca/`. Docker Compose files for:
- Certificate Authorities (identity CA + TLS CA per organization)
- Peer nodes (3 per organization)
- Orderer nodes (Raft consensus)
- Channel creation and configuration

### External Bank (Python)

Located in `external-bank/`. Utilities for:
- RSA key pair generation
- Transaction confirmation signing/verification

## Data Flow

1. User interacts via React frontend
2. Frontend calls REST API on Spring Boot backend
3. Backend uses Fabric Gateway SDK to submit transactions via gRPC
4. Peers execute chaincode, orderers sequence via Raft consensus
5. Events propagated back to frontend via Server-Sent Events (SSE)
6. Bank API handles external transaction confirmations with RSA encryption

## Network Topology

~24 Docker containers across three organizations:

| Organization | Peer | Orderer | CA (Identity) | CA (TLS) | API Instances |
|-------------|------|---------|---------------|----------|---------------|
| Yacht Sales | peer0:9051 | orderer0 | ca-yachtsales | tls-ca-yachtsales | sales, logistics, production |
| Furnitures Makers | peer0:7051 | orderer0 | ca-furnituresmakers | tls-ca-furnituresmakers | sales, logistics, production |
| Wood Supply | peer0:8051 | orderer0 | ca-woodsupply | tls-ca-woodsupply | sales, logistics, production |

**Channel:** `yfw-channel` (Yacht-Furnitures-Wood)
**Consensus:** Raft ordering service
**Security:** Mutual TLS between all nodes, X.509 certificate-based identity

## Security Model

- **Identity:** X.509 certificates issued by per-organization Fabric CAs
- **Transport:** Mutual TLS (mTLS) for all gRPC communication
- **Authentication:** JWT tokens (access: 5 min, refresh: 7 days)
- **Transaction Integrity:** RSA-2048 signed bank confirmations verified in chaincode
- **Replay Prevention:** Transaction ID deduplication stored on the ledger
- **Governance:** 3-of-3 organizational consensus for IBAN changes
