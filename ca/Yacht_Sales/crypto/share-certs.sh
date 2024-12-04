TLS_CA_CERT="/etc/hyperledger/client/tls_root_cert/tls-ca-cert.pem"

CA_CERT="/etc/hyperledger/server/ca/msp/cacerts/ca-cert.pem"

cp "$TLS_CA_CERT" "/etc/hyperledger/_shared_certs/tls-$HOSTNAME"
cp "$CA_CERT" "/etc/hyperledger/_shared_certs/ca-$HOSTNAME"

sleep 3

PATHS=$(find /etc/hyperledger/_shared_certs -type f ! -name "*$HOSTNAME*" | paste -sd ',' -)

export FABRIC_CA_SERVER_TLS_CLIENTAUTH_CERTFILES="$PATHS"
export FABRIC_CA_SERVER_HOME=/etc/hyperledger/server/ca
export FABRIC_CA_SERVER_TLS_CLIENTAUTH_TYPE=RequireAndVerifyClientCert

echo "FABRIC_CA_SERVER_TLS_CLIENTAUTH_CERTFILES=$PATHS" > /etc/hyperledger/_env/env
echo "FABRIC_CA_SERVER_HOME=/etc/hyperledger/server/ca" >> /etc/hyperledger/_env/env
echo "FABRIC_CA_SERVER_TLS_CLIENTAUTH_TYPE=RequireAndVerifyClientCert" >> /etc/hyperledger/_env/env

echo "Restarting existing Fabric CA server..."
pkill -f "fabric-ca-server" || echo "No running Fabric CA server found."

