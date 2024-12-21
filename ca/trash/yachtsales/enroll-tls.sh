
mkdir -p /etc/hyperledger/client/tls_root_cert
cp -v /etc/hyperledger/server/tls-ca/ca-cert.pem /etc/hyperledger/client/tls_root_cert/tls-ca-cert.pem


echo FABRIC_CA_SERVER_HOME
echo $FABRIC_CA_SERVER_HOME

export FABRIC_CA_CLIENT_TLS_CERTFILES=/etc/hyperledger/server/tls-ca/ca-cert.pem
echo FABRIC_CA_CLIENT_TLS_CERTFILES
echo $FABRIC_CA_CLIENT_TLS_CERTFILES

export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client
echo FABRIC_CA_CLIENT_HOME
echo $FABRIC_CA_CLIENT_HOME

#export FABRIC_CA_CLIENT_MSPDIR=msp
#echo FABRIC_CA_CLIENT_MSPDIR
#echo $FABRIC_CA_CLIENT_MSPDIR


TLS_CA_CERT="/etc/hyperledger/client/tls_root_cert/tls-ca-cert.pem"

fabric-ca-client enroll -d -u https://tlsadmin:tlsadminpw@localhost:7054 --tls.certfiles $TLS_CA_CERT --mspdir tls-ca/tlsadmin/msp --csr.hosts "tlsadmin,localhost"


fabric-ca-client enroll -d -u https://admin:adminpw@localhost:7054 --tls.certfiles $TLS_CA_CERT --enrollment.profile tls --csr.hosts "furnituresmakers-ca,localhost,127.0.0.1,admin" --mspdir tls-ca/admin/msp


#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/admin
#set HOSTNAME=admin

#fabric-ca-client enroll --enrollment.profile tls -u https://admin:adminpw@localhost:7054 --caname tls-ca --tls.certfiles $TLS_CA_CERT --enrollment.profile tls --mspdir tls-ca/msp

#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/peer0


fabric-ca-client enroll --enrollment.profile tls -u https://peer0:peer0pw@localhost:7054 --caname tls-ca --tls.certfiles $TLS_CA_CERT --csr.hosts "peer0,localhost" --enrollment.profile tls --mspdir tls-ca/peer0/msp

#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/peer1


fabric-ca-client enroll --enrollment.profile tls -u https://peer1:peer1pw@localhost:7054 --caname tls-ca --tls.certfiles $TLS_CA_CERT --csr.hosts "peer1,localhost" --enrollment.profile tls --mspdir tls-ca/peer1/msp

#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/peer2


fabric-ca-client enroll --enrollment.profile tls -u https://peer2:peer2pw@localhost:7054 --caname tls-ca --tls.certfiles $TLS_CA_CERT --csr.hosts "peer2,localhost" --enrollment.profile tls --mspdir tls-ca/peer2/msp

#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/orderer0


fabric-ca-client enroll --enrollment.profile tls -u https://orderer0:orderer0pw@localhost:7054 --caname tls-ca --tls.certfiles $TLS_CA_CERT --csr.hosts "orderer0,localhost" --enrollment.profile tls --mspdir tls-ca/orderer0/msp

#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/production_api


fabric-ca-client enroll --enrollment.profile tls -u https://production_api:productionapipw@localhost:7054 --caname tls-ca --tls.certfiles $TLS_CA_CERT --csr.hosts "production_api,localhost" --enrollment.profile tls --mspdir tls-ca/production_api/msp

#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/sales_api


fabric-ca-client enroll --enrollment.profile tls -u https://sales_api:salesapipw@localhost:7054 --caname tls-ca --tls.certfiles $TLS_CA_CERT --csr.hosts "sales_api,localhost" --enrollment.profile tls --mspdir tls-ca/sales_api/msp

#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/logistics_api


fabric-ca-client enroll --enrollment.profile tls -u https://logistics_api:logisticsapipw@localhost:7054 --caname tls-ca --tls.certfiles $TLS_CA_CERT --csr.hosts "logistics_api,localhost" --enrollment.profile tls --mspdir tls-ca/logistics_api/msp

#mv /etc/hyperledger/client/tls-ca/admin/msp/keystore/* /etc/hyperledger/server/ca/tls/key.pem

mkdir -p /etc/hyperledger/server/ca/tls/

cp -r /etc/hyperledger/client/tls-ca/admin/msp/signcerts/cert.pem /etc/hyperledger/server/ca/tls/cert.pem
cp -r /etc/hyperledger/client/tls-ca/admin/msp/keystore/* /etc/hyperledger/server/ca/tls/key.pem

TLS_CA_DIR="/etc/hyperledger/client/tls-ca"
for USER_DIR in "$TLS_CA_DIR"/*; do
  if [ -d "$USER_DIR" ]; then
    KEYSTORE_DIR="$USER_DIR/msp/keystore"
    # Sprawdź, czy katalog keystore istnieje
    if [ -d "$KEYSTORE_DIR" ]; then
      # Znajdź pierwszy plik w katalogu keystore i zmień jego nazwę na key.pem
      for KEY_FILE in "$KEYSTORE_DIR"/*; do
        if [ -f "$KEY_FILE" ]; then
          mv "$KEY_FILE" "$KEYSTORE_DIR/key.pem"
          echo "Renamed key in $USER_DIR to key.pem"
          break
        fi
      done
    else
      echo "Keystore directory not found in $USER_DIR"
    fi
  fi
done
#mv /etc/hyperledger/client/tls-ca/admin/msp/keystore/* /etc/hyperledger/client/tls-ca/admin/msp/keystore/key.pem


echo "Stopping existing Fabric CA server..."
pkill -f "fabric-ca-server" || echo "No running Fabric CA server found."

#echo "Setting environment variables for the new Fabric CA server..."
#export FABRIC_CA_SERVER_HOME=/etc/hyperledger/server
#export FABRIC_CA_SERVER_TLS_CLIENTAUTH_TYPE=RequireAndVerifyClientCert

#echo "Starting new Fabric CA server with updated configuration..."
#fabric-ca-server start -b admin:adminpw --cafiles /etc/hyperledger/server/ca/fabric-ca-server-config.yaml
