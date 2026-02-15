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