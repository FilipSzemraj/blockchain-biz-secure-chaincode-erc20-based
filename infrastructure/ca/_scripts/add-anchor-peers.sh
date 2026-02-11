#!/bin/bash

# Ustawienie ścieżki do MSP
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/_shared_certs/${ORG_QUICK_NAME}-msp/admin/admin/msp

# Pobranie i dołączenie do kanału
echo "Fetching channel block..."
peer channel fetch 0 ./yfw-channel.block -o orderer0.${ORG_QUICK_NAME}.com:${PREFIX_PORT}50 -c yfw-channel --tls --cafile $CA_FILE --certfile $CERT_FILE --keyfile $KEY_FILE --clientauth
if [ $? -ne 0 ]; then
  echo "Failed to fetch channel block."
  exit 1
fi

echo "Joining channel..."
peer channel join -b yfw-channel.block -o orderer0.${ORG_QUICK_NAME}.com --tls --cafile $CA_FILE --certfile $CERT_FILE --keyfile $KEY_FILE --clientauth
if [ $? -ne 0 ]; then
  echo "Failed to join channel."
  exit 1
fi

sleep 10

# Tworzenie paczki chaincode
echo "Packaging chaincode..."
peer lifecycle chaincode package basic.tar.gz --path ../chaincode/chaincode-java/ --lang java --label basic_1.0
if [ $? -ne 0 ]; then
  echo "Failed to package chaincode."
  exit 1
fi

# Instalacja chaincode
echo "Installing chaincode..."
OUTPUT=$(peer lifecycle chaincode install ./basic.tar.gz 2>&1)
if [ $? -ne 0 ]; then
  echo "Failed to install chaincode."
  echo "$OUTPUT"
  exit 1
fi

# Wyodrębnianie package ID
echo "Extracting package ID..."
CC_PACKAGE_ID=$(echo "$OUTPUT" | grep -oP '(?<=identifier: ).*')
if [ -n "$CC_PACKAGE_ID" ]; then
  echo "Chaincode package ID: $CC_PACKAGE_ID"
else
  echo "Failed to extract the chaincode package ID."
  echo "$OUTPUT"
  exit 1
fi

# Sprawdzenie, czy paczka została zainstalowana
echo "Verifying chaincode installation..."
while true; do
  INSTALL_OUTPUT=$(peer lifecycle chaincode queryinstalled 2>&1)
  if echo "$INSTALL_OUTPUT" | grep -q "$CC_PACKAGE_ID"; then
    echo "Chaincode installed successfully."
    break
  fi
  echo "Waiting for chaincode to be installed..."
  sleep 2
done

# Zatwierdzenie chaincode przez organizację
echo "Approving chaincode for organization..."
peer lifecycle chaincode approveformyorg \
  -o orderer0.${ORG_QUICK_NAME}.com:${PREFIX_PORT}50 \
  --channelID yfw-channel \
  --name basic \
  --version 1.0 \
  --package-id "$CC_PACKAGE_ID" \
  --sequence 1 \
  --tls \
  --cafile $CA_FILE \
  --clientauth \
  --certfile $CERT_FILE \
  --keyfile $KEY_FILE \
  --init-required

if [ $? -ne 0 ]; then
  echo "Failed to approve chaincode for organization."
  exit 1
fi

echo "Chaincode successfully installed and approved."

sleep 3

if [ "${ORG_QUICK_NAME}" == "furnituresmakers" ]; then
peer lifecycle chaincode commit -o orderer0.${ORG_QUICK_NAME}.com:${PREFIX_PORT}50 \
  --channelID yfw-channel --name basic --version 1.0 --sequence 1 --tls --cafile $CA_FILE \
  --clientauth --certfile $CERT_FILE --keyfile $KEY_FILE \
  --peerAddresses peer0.yachtsales.com:9051 --tlsRootCertFiles /etc/hyperledger/_shared_certs/yachtsales-msp/tlscacerts/tls-ca-cert.pem \
  --peerAddresses peer0.woodsupply.com:8051 --tlsRootCertFiles /etc/hyperledger/_shared_certs/woodsupply-msp/tlscacerts/tls-ca-cert.pem --init-required
sleep 3
peer chaincode invoke -o orderer0.${ORG_QUICK_NAME}.com:${PREFIX_PORT}50 \
  --channelID yfw-channel \
  --name basic \
  --isInit \
  --peerAddresses peer0.yachtsales.com:9051 \
  --tlsRootCertFiles /etc/hyperledger/_shared_certs/yachtsales-msp/tlscacerts/tls-ca-cert.pem \
  --peerAddresses peer0.woodsupply.com:8051 \
  --tlsRootCertFiles /etc/hyperledger/_shared_certs/woodsupply-msp/tlscacerts/tls-ca-cert.pem \
  --tls \
  --clientauth \
  --cafile $CA_FILE \
  --certfile $CERT_FILE \
  --keyfile $KEY_FILE \
  -c '{"function":"Initialize","Args":["IntrinsicCoin", "IC", "18"]}'

fi
