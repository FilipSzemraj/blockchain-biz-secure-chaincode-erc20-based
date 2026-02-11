export FABRIC_CFG_PATH=/etc/hyperledger/config
echo $FABRIC_CFG_PATH

echo 'Start of script creating-channel.sh'

if [ ! -f ../output/genesis_block_YFW.pb ]; then
  echo 'genesis_block_YFW is not exisiting'
  configtxgen -profile YFW -outputBlock ../output/genesis_block_YFW.pb -channelID yfw-channel
  sleep 2
fi

#export OSN_TLS_CA_ROOT_CERT=/etc/hyperledger/_shared_certs/${ORG_NAME}-msp/orderer/orderer0/msp/tls/tlscacerts/tls-ca-cert.pem
#export ADMIN_TLS_SIGN_CERT=/etc/hyperledger/_shared_certs/${ORG_NAME}-msp/admin/admin/msp/tls/signcerts/cert.pem
#export ADMIN_TLS_PRIVATE_KEY=/etc/hyperledger/_shared_certs/${ORG_NAME}-msp/admin/admin/msp/tls/keystore/key.pem
echo 'osnadmin function is about to being executed'
osnadmin channel join --channelID yfw-channel --config-block ../output/genesis_block_YFW.pb -o orderer0.${ORG_NAME}.com:9443 --ca-file $OSN_TLS_CA_ROOT_CERT --client-cert $ADMIN_TLS_SIGN_CERT --client-key $ADMIN_TLS_PRIVATE_KEY

#osnadmin channel join --channelID yfw-channel --config-block ../output/genesis_block_YFW.pb -o orderer0.${ORG_NAME}.com:9443 --ca-file $OSN_TLS_CA_ROOT_CERT --client-cert $ADMIN_TLS_SIGN_CERT --client-key $ADMIN_TLS_PRIVATE_KEY
