USERS=("peer0:peer0pw"
       "peer1:peer1pw"
       "peer2:peer2pw"
       "orderer0:ordererpw"
       "production_api:productionapipw"
       "sales_api:salesapipw"
       "logistics_api:logisticsapipw")

export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client
echo FABRIC_CA_CLIENT_HOME
echo $FABRIC_CA_CLIENT_HOME

TLS_CA_CERT="/etc/hyperledger/client/tls_root_cert/tls-ca-cert.pem"

mkdir -p /etc/hyperledger/client/ca

export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/admin

export FABRIC_CA_CLIENT_CSR_CN=admin
export FABRIC_CA_CLIENT_CSR_HOSTS="admin,localhost"

export FABRIC_CA_CLIENT_CANAME=woodsupply-ca


fabric-ca-client enroll -u https://admin:adminpw@localhost:7055 \
  --tls.certfiles /etc/hyperledger/server/tls-ca/msp/cacertsca-cert.pem \
  --tls.client.certfile /etc/hyperledger/client/tls-ca/admin/msp/signcerts/cert.pem \
  --tls.client.keyfile /etc/hyperledger/client/tls-ca/admin/msp/keystore/key.pem \
  --mspdir /etc/hyperledger/client/ca/admin/msp \
  --csr.hosts "admin,localhost"

mv /etc/hyperledger/client/ca/admin/msp/keystore/* /etc/hyperledger/client/ca/admin/msp/keystore/woodsupply-key.pem

for USER in "${USERS[@]}"; do
 # Rozdzielenie loginu i hasła
   IFS=":" read -r USERNAME PASSWORD <<< "$USER"

   # CSR hosts
   HOSTS="$USERNAME,localhost"
   export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/$USERNAME

   CLIENT_CERT=/etc/hyperledger/client/tls-ca/$USERNAME/msp/signcerts/cert.pem
   CLIENT_KEY=/etc/hyperledger/client/tls-ca/$USERNAME/msp/keystore/key.pem
   echo CLIENT_CERT
   echo $CLIENT_CERT

   echo CLIENT_KEY
   echo $CLIENT_KEY

   # Wywołanie komendy enroll
   CMD="fabric-ca-client enroll -u https://$USERNAME:$PASSWORD@localhost:7055 \
        --tls.certfiles \"$TLS_CA_CERT\" \
        --csr.hosts \"$HOSTS\""

  if [ -f "$CLIENT_CERT" ]; then
    CMD="$CMD --tls.client.certfile \"$CLIENT_CERT\""
  else
    echo "Client certificate not found: $CLIENT_CERT"
  fi

  if [ -f "$CLIENT_KEY" ]; then
    CMD="$CMD --tls.client.keyfile \"$CLIENT_KEY\""
  else
    echo "Client key not found: $CLIENT_KEY"
  fi

  eval $CMD

  KEYSTORE_DIR="$FABRIC_CA_CLIENT_HOME/msp/keystore"
      if [ -d "$KEYSTORE_DIR" ]; then
        # Znajdź pierwszy plik w katalogu
        KEY_FILE=$(find "$KEYSTORE_DIR" -type f -print -quit)

        if [ -n "$KEY_FILE" ]; then # Sprawdź, czy plik istnieje
          mv "$KEY_FILE" "$KEYSTORE_DIR/key.pem"
          echo "Renamed key in $KEYSTORE_DIR to key.pem"
        else
          echo "No key file found in $KEYSTORE_DIR"
        fi
      else
        echo "Keystore directory does not exist: $KEYSTORE_DIR"
      fi
done

./share-certs.sh

