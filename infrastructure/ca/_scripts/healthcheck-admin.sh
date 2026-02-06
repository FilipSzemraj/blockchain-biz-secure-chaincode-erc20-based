#!/bin/bash

# Sprawdź, czy plik genesis_block_YFW.pb istnieje
if [ ! -f /etc/hyperledger/output/genesis_block_YFW.pb ]; then
  echo 'File genesis_block_YFW not existing'
  exit 1
fi

#export OSN_TLS_CA_ROOT_CERT=/etc/hyperledger/_shared_certs/${ORG_NAME}-msp/orderer/orderer0/msp/tls/tlscacerts/tls-ca-cert.pem
#export ADMIN_TLS_SIGN_CERT=/etc/hyperledger/_shared_certs/${ORG_NAME}-msp/admin/admin/msp/tls/signcerts/cert.pem
#export ADMIN_TLS_PRIVATE_KEY=/etc/hyperledger/_shared_certs/${ORG_NAME}-msp/admin/admin/msp/tls/keystore/key.pem


# Spróbuj dołączyć do kanału (lub wykonaj testowe zapytanie)
osnadmin channel list -o orderer0.${ORG_NAME}.com:9443 --ca-file $OSN_TLS_CA_ROOT_CERT --client-cert $ADMIN_TLS_SIGN_CERT --client-key $ADMIN_TLS_PRIVATE_KEY

if [ $? -eq 0 ]; then
  echo 'osnadmin channel list executed sucessfully'
  exit 0
else
  echo 'osnadmin channel list executed unsucessfully'
  exit 1
fi