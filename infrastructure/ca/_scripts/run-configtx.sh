CONTAINER_HOST=${1:-admin}
CONTAINER_ORG=${2:-Furnitures_Makers}

ORG_QUICK_NAME=$(echo "${CONTAINER_ORG//_/}" | tr '[:upper:]' '[:lower:]')

cp -f creating-channel.sh ../_config_files/configtx/creating-channel.sh
cp -f healthcheck-admin.sh ../_config_files/configtx/healthcheck-admin.sh

echo "Copied creating-channel.sh to ../_config_files/configtx/creating-channel.sh"

CONTAINER_NAME=${CONTAINER_HOST}.${ORG_QUICK_NAME}.com
ORG_NAME=${ORG_QUICK_NAME}
OSN_TLS_CA_ROOT_CERT=/etc/hyperledger/_shared_certs/${ORG_NAME}-msp/orderer/orderer0/msp/tls/tlscacerts/tls-ca-cert.pem
ADMIN_TLS_SIGN_CERT=/etc/hyperledger/_shared_certs/${ORG_NAME}-msp/admin/admin/msp/tls/signcerts/cert.pem
ADMIN_TLS_PRIVATE_KEY=/etc/hyperledger/_shared_certs/${ORG_NAME}-msp/admin/admin/msp/tls/keystore/key.pem

cat > "../_config_files/configtx/.${CONTAINER_NAME}.env" <<EOF
CONTAINER_NAME=$CONTAINER_NAME
ORG_NAME=$ORG_NAME
OSN_TLS_CA_ROOT_CERT=$OSN_TLS_CA_ROOT_CERT
ADMIN_TLS_SIGN_CERT=$ADMIN_TLS_SIGN_CERT
ADMIN_TLS_PRIVATE_KEY=$ADMIN_TLS_PRIVATE_KEY
EOF

docker-compose --project-name "admin-${ORG_NAME}" --env-file ../_config_files/configtx/.${CONTAINER_NAME}.env -f "../docker-compose.configtx.yaml" down

docker-compose --project-name "admin-${ORG_NAME}" --env-file ../_config_files/configtx/.${CONTAINER_NAME}.env -f "../docker-compose.configtx.yaml" config

docker-compose --project-name "admin-${ORG_NAME}" --env-file ../_config_files/configtx/.${CONTAINER_NAME}.env -f "../docker-compose.configtx.yaml" build


docker-compose --project-name "admin-${ORG_NAME}" --env-file ../_config_files/configtx/.${CONTAINER_NAME}.env -f "../docker-compose.configtx.yaml" up -d
#winpty docker exec -it $(docker-compose --project-name "admin-furnituresmakers" -f "../docker-compose.configtx.yaml" ps -q configtx) bash -c "echo 'Checking output...'; ls ../output; bash"
winpty docker exec -it ${CONTAINER_NAME} bash -c "echo 'Checking output...'; ls ../output; bash"
#winpty docker exec -it ${ORDERER_GENERAL_LISTENADDRESS} bash -c "echo 'Taking control of orderer...'; bash"
