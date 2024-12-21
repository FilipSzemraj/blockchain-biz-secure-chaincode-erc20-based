export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/_shared_certs/${ORG_QUICK_NAME}-msp/admin/admin/msp

peer channel fetch 0 ./yfw-channel.block -o orderer0.${ORG_QUICK_NAME}.com:${PREFIX_PORT}50 -c yfw-channel --tls --cafile $CA_FILE --certfile $CERT_FILE --keyfile $KEY_FILE --clientauth
sleep 1
peer channel join -b yfw-channel.block -o orderer0.${ORG_QUICK_NAME}.com --tls --cafile $CA_FILE --certfile $CERT_FILE --keyfile $KEY_FILE --clientauth

sleep 1

peer lifecycle chaincode package basic.tar.gz --path ../chaincode/chaincode-typescript/ --lang node --label basic_1.0

#sleep 1

#peer lifecycle chaincode install ./basic.tar.gz
#OUTPUT=$(peer lifecycle chaincode install ./basic.tar.gz 2>&1)


#CC_PACKAGE_ID=$(echo "$OUTPUT" | grep -oP '(?<=identifier: ).*')

#if [ -n "$CC_PACKAGE_ID" ]; then
#  echo "Chaincode package ID: $CC_PACKAGE_ID"
#else
#  echo "Failed to extract the chaincode package ID."
#  exit 1
#fi

#sleep 1

#peer lifecycle chaincode approveformyorg -o orderer0.furnituresmakers.com:7050 --channelID yfw-channel --name basic --version 1.0 --package-id $CC_PACKAGE_ID --sequence 1 --tls --cafile $CA_FILE --clientauth --certfile $CERT_FILE --keyfile $KEY_FILE
