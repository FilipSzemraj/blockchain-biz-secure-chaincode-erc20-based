#CLIENT_ROOT_CAs=$(find /etc/hyperledger/_shared_certs -type f -path "*/tlscacerts/*.pem" ! -path "*{$ORG_QUICK_NAME}*" | paste -sd ',' -)

#export CORE_TLS_CLIENTROOTCAS_FILE="$CLIENT_ROOT_CAs"

cp -v /etc/hyperledger/config/config.yaml /etc/hyperledger/msp/
cp -v /etc/hyperledger/config/config.yaml /etc/hyperledger/_shared_certs/${ORG_QUICK_NAME}-msp/admin/admin/msp

cp -v "${FIRST_ORG_SOURCE}" "${FIRST_ORG_DEST}"
cp -v "${SECOND_ORG_SOURCE}" "${SECOND_ORG_DEST}"
#for ORG in "${ORGS[@]}"; do
#  IFS=":" read -r lowercase uppercase <<< "$ORG"
#  cp -v /etc/hyperledger/_shared_certs/${lowercase}-msp/admin/admin/msp /etc/hyperledger/msp/cacerts/${lowercase}-cert.pem
#done;

#peer channel fetch 0 ./yfw-channel.block -o orderer0.furnituresmakers.com:7050 -c yfw-channel --tls --cafile $CA_FILE --certfile $CERT_FILE --keyfile $KEY_FILE --clientauth

#execute peer node start