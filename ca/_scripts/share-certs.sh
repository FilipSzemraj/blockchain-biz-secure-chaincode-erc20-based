TLS_CA_CERT="/etc/hyperledger/client/tls_root_cert/tls-ca-cert.pem"

CA_CERT="/etc/hyperledger/server/ca/msp/cacerts/ca-cert.pem"

CONFIG_PATH="/etc/hyperledger/server/ca/msp/config.yaml"

PATHS_TO_ORDERER_MSP=($(find /etc/hyperledger/client -type d -path "*/ca/orderer*/msp"))
PATHS_TO_ADMIN_MSP=($(find /etc/hyperledger/client -type d -path "*/ca/admin*/msp"))

#ORDERER_MSP="/etc/hyperledger/client/ca/msp/cacerts/ca-cert.pem"

mkdir /etc/_shared_certs/"$HOSTNAME"-msp/
mkdir /etc/_shared_certs/"$HOSTNAME"-msp/cacerts/
mkdir /etc/_shared_certs/"$HOSTNAME"-msp/tlscacerts/
#mkdir /etc/_shared_certs/"$HOSTNAME"-msp/orderer/

cp "$TLS_CA_CERT" "/etc/_shared_certs/$HOSTNAME-msp/tlscacerts/tls-ca-cert.pem"
cp "$CA_CERT" "/etc/_shared_certs/$HOSTNAME-msp/cacerts/ca-cert.pem"
cp "$CONFIG_PATH" "/etc/_shared_certs/$HOSTNAME-msp/config.yaml"

if [ ${#PATHS_TO_ORDERER_MSP[@]} -eq 0 ]; then
  echo "No Orderer's MSP directories found. Exiting."
  exit 1
fi

if [ ${#PATHS_TO_ADMIN_MSP[@]} -eq 0 ]; then
  echo "No Admin's MSP directories found. Exiting."
  exit 1
fi

for path in "${PATHS_TO_ORDERER_MSP[@]}"; do
    orderer_name=$(echo "$path" | awk -F'/' '{print $(NF-1)}')
    mkdir -p "/etc/_shared_certs/$HOSTNAME-msp/orderer/${orderer_name}/msp"
    cp -r "${path}" "/etc/_shared_certs/"$HOSTNAME"-msp/orderer/${orderer_name}/"
done

for path in "${PATHS_TO_ADMIN_MSP[@]}"; do
    admin_name=$(echo "$path" | awk -F'/' '{print $(NF-1)}')
    mkdir -p "/etc/_shared_certs/$HOSTNAME-msp/admin/${admin_name}/msp"
    cp -r "${path}" "/etc/_shared_certs/"$HOSTNAME"-msp/admin/${admin_name}/"
done

sleep 3

PATHS=$(find /etc/_shared_certs -type f -path "*/tlscacerts/*" ! -path "*$HOSTNAME*" | paste -sd ',' -)

#export FABRIC_CA_SERVER_TLS_CLIENTAUTH_CERTFILES="$PATHS"
#export FABRIC_CA_SERVER_HOME=/etc/hyperledger/server/ca
#export FABRIC_CA_SERVER_TLS_CLIENTAUTH_TYPE=RequireAndVerifyClientCert

echo "FABRIC_CA_SERVER_TLS_CLIENTAUTH_CERTFILES=$PATHS" > /etc/_env/env
echo "FABRIC_CA_SERVER_HOME=/etc/hyperledger/server/ca" >> /etc/_env/env
echo "FABRIC_CA_SERVER_TLS_CLIENTAUTH_TYPE=RequireAndVerifyClientCert" >> /etc/_env/env





echo "Restarting existing Fabric CA server..."
pkill -f "fabric-ca-server" || echo "No running Fabric CA server found."
