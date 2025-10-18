#USERS=("peer0:peer0pw"
#       "peer1:peer1pw"
#       "peer2:peer2pw"
#       "orderer0:ordererpw"
#       "production_api:productionapipw"
#       "sales_api:salesapipw"
#       "logistics_api:logisticsapipw")

source ./users_array.sh "$HOSTNAME"

export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client
echo FABRIC_CA_CLIENT_HOME
echo $FABRIC_CA_CLIENT_HOME

TLS_CA_CERT="/etc/hyperledger/client/tls_root_cert/tls-ca-cert.pem"

mkdir -p /etc/hyperledger/client/ca

export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/admin

export FABRIC_CA_CLIENT_CSR_CN=admin
export FABRIC_CA_CLIENT_CSR_HOSTS="admin,localhost"

export FABRIC_CA_CLIENT_CANAME=$HOSTNAME-ca


fabric-ca-client enroll -u https://admin:adminpw@localhost:$FABRIC_CA_SERVER_PORT \
  --caname $FABRIC_CA_CLIENT_CANAME \
  --tls.certfiles /etc/hyperledger/server/tls-ca/msp/cacerts/ca-cert.pem \
  --tls.client.certfile /etc/hyperledger/client/tls-ca/admin/msp/signcerts/cert.pem \
  --tls.client.keyfile /etc/hyperledger/client/tls-ca/admin/msp/keystore/key.pem \
  --mspdir /etc/hyperledger/client/ca/admin/msp \
  --csr.hosts "admin.$HOSTNAME.com,localhost,admin-$HOSTNAME-com" \
  --csr.cn admin \
  --csr.names "C=PL,ST=Swietokrzyskie,L=Kielce,O=${ORG_NAME},OU=admin" \
  ----enrollment.attrs "hf.Type"
  #--csr.names "C=PL,ST=Swietokrzyskie,L=Kielce,O=${ORG_NAME},OU=admin"


mv /etc/hyperledger/client/ca/admin/msp/keystore/* /etc/hyperledger/client/ca/admin/msp/keystore/key.pem

for ((i=1; i<${#USERS[@]}; i++)); do
    USER="${USERS[$i]}"
 # Rozdzielenie loginu i hasła
   IFS=":" read -r USERNAME PASSWORD ROLE <<< "$USER"

   # CSR hosts
   HOSTS="$USERNAME.$HOSTNAME.com,localhost,$USERNAME-$HOSTNAME-com"
   export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/$USERNAME

   CLIENT_CERT=/etc/hyperledger/client/tls-ca/$USERNAME/msp/signcerts/cert.pem
   CLIENT_KEY=/etc/hyperledger/client/tls-ca/$USERNAME/msp/keystore/key.pem
   CLIENT_TLS_CA_CERT_DIR=/etc/hyperledger/client/tls-ca/$USERNAME/msp/tlscacerts
   CLIENT_TLS_CA_CERT=$(ls -1 "$CLIENT_TLS_CA_CERT_DIR" | head -n 1)

   CLIENT_TLS_CA_CERT="$CLIENT_TLS_CA_CERT_DIR/$CLIENT_TLS_CA_CERT"


   echo CLIENT_CERT
   echo $CLIENT_CERT

   echo CLIENT_KEY
   echo $CLIENT_KEY

   # Wywołanie komendy enroll
   CMD="fabric-ca-client enroll -u https://$USERNAME:$PASSWORD@localhost:$FABRIC_CA_SERVER_PORT \
        --caname $FABRIC_CA_CLIENT_CANAME \
        --tls.certfiles \"$TLS_CA_CERT\" \
        --csr.hosts \"$HOSTS\" \
        --csr.cn \"$USERNAME\" \
        --csr.names C=PL,ST=Swietokrzyskie,L=Kielce,O=$ORG_NAME "
        #--csr.names C=PL,ST=Swietokrzyskie,L=Kielce,O=$ORG_NAME,OU=$ROLE"

  # Sprawdzenie, czy nazwa użytkownika zawiera postfix "_api"
  if [[ "$USERNAME" == *_api ]]; then
      CMD="$CMD --enrollment.attrs \"hf.iban,hf.Type\""
  fi

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

  mkdir -p "$FABRIC_CA_CLIENT_HOME/msp/tls/keystore"
  mkdir -p "$FABRIC_CA_CLIENT_HOME/msp/tls/signcerts"
  mkdir -p "$FABRIC_CA_CLIENT_HOME/msp/tls/tlscacerts"

  cp "${CLIENT_CERT}" "$FABRIC_CA_CLIENT_HOME/msp/tls/signcerts/cert.pem"
  cp "${CLIENT_KEY}" "$FABRIC_CA_CLIENT_HOME/msp/tls/keystore/key.pem"
  cp "${CLIENT_TLS_CA_CERT}" "$FABRIC_CA_CLIENT_HOME/msp/tls/tlscacerts/tls-ca-cert.pem"


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

  CACERTS_DIR="$FABRIC_CA_CLIENT_HOME/msp/cacerts"
  if [ -d "$CACERTS_DIR" ]; then
        # Znajdź pierwszy plik w katalogu
        CERT_FILE=$(find "$CACERTS_DIR" -type f -print -quit)

        if [ -n "$CERT_FILE" ]; then # Sprawdź, czy plik istnieje
          mv "$CERT_FILE" "$CACERTS_DIR/ca-cert.pem"
          echo "Renamed key in $CACERTS_DIR to ca-cert.pem"
        else
          echo "No key file found in $CACERTS_DIR"
        fi
      else
        echo "Keystore directory does not exist: $CACERTS_DIR"
      fi
done



./share-certs.sh
