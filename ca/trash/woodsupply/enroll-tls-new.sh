#!/bin/bash

# Deklaracja tablicy z użytkownikami i ich hasłami (login:hasło)
USERS=("woodsupply-ca:capw"
       "admin:adminpw"
       "peer0:peer0pw"
       "peer1:peer1pw"
       "peer2:peer2pw"
       "orderer0:orderer0pw"
       "production_api:productionapipw"
       "sales_api:salesapipw"
       "logistics_api:logisticsapipw")


mkdir -p /etc/hyperledger/client/tls_root_cert
cp -v /etc/hyperledger/server/tls-ca/msp/cacerts/ca-cert.pem /etc/hyperledger/client/tls_root_cert/tls-ca-cert.pem

export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client
echo FABRIC_CA_CLIENT_HOME
echo $FABRIC_CA_CLIENT_HOME

TLS_CA_CERT="/etc/hyperledger/client/tls_root_cert/tls-ca-cert.pem"

fabric-ca-client enroll -d -u https://tlsadmin:tlsadminpw@localhost:7055 --tls.certfiles $TLS_CA_CERT --mspdir tls-ca/tlsadmin/msp --csr.hosts "tlsadmin,localhost"


#fabric-ca-client enroll -d -u https://admin:adminpw@localhost:7055 --tls.certfiles $TLS_CA_CERT --enrollment.profile tls --csr.hosts "woodsupply-ca,localhost,127.0.0.1,admin" --mspdir tls-ca/admin/msp

# Iteracja przez użytkowników
for USER in "${USERS[@]}"; do
  # Rozdzielenie loginu i hasła
  IFS=":" read -r USERNAME PASSWORD <<< "$USER"

  # Ścieżka MSP dla użytkownika
  MSP_DIR="tls-ca/$USERNAME/msp"

  # CSR hosts
  HOSTS="$USERNAME,localhost"

  # Wywołanie komendy enroll
  fabric-ca-client enroll --enrollment.profile tls \
    -u https://$USERNAME:$PASSWORD@localhost:7055 \
    --caname tls-ca \
    --tls.certfiles "$TLS_CA_CERT" \
    --csr.hosts "$HOSTS" \
    --mspdir "$MSP_DIR"


  # Zmiana nazwy klucza
  KEYSTORE_DIR="$FABRIC_CA_CLIENT_HOME/$MSP_DIR/keystore"
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

mkdir -p /etc/hyperledger/server/ca/tls/

cp -r /etc/hyperledger/client/tls-ca/woodsupply-ca/msp/signcerts/cert.pem /etc/hyperledger/server/ca/tls/cert.pem
cp -r /etc/hyperledger/client/tls-ca/woodsupply-ca/msp/keystore/* /etc/hyperledger/server/ca/tls/key.pem

#mv /etc/hyperledger/client/tls-ca/admin/msp/keystore/* /etc/hyperledger/client/tls-ca/admin/msp/keystore/key.pem


echo "Stopping existing Fabric CA server..."
pkill -f "fabric-ca-server" || echo "No running Fabric CA server found."