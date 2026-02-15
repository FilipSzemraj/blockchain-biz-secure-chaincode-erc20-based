export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/_shared_certs/${ORG_QUICK_NAME}-msp/admin/admin/msp

echo "ZMIENNE"
echo $ORG_QUICK_NAME
echo $PREFIX_PORT
echo $CA_FILE
echo $CERT_FILE
echo $KEY_FILE

peer channel fetch 0 ./yfw-channel.block -o orderer0.${ORG_QUICK_NAME}.com:${PREFIX_PORT}50 -c yfw-channel --tls --cafile $CA_FILE --certfile $CERT_FILE --keyfile $KEY_FILE --clientauth
sleep 1
peer channel join -b yfw-channel.block -o orderer0.${ORG_QUICK_NAME}.com --tls --cafile $CA_FILE --certfile $CERT_FILE --keyfile $KEY_FILE --clientauth

sleep 1