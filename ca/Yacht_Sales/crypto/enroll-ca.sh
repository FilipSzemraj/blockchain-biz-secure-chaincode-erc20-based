export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client
echo FABRIC_CA_CLIENT_HOME
echo $FABRIC_CA_CLIENT_HOME

#export FABRIC_CA_CLIENT_TLS_CERTFILES=/etc/hyperledger/client/admin/tls-msp/tlscacerts/tls-localhost-7054-tls-ca.pem
#export FABRIC_CA_CLIENT_TLS_CERTFILES=/etc/hyperledger/server/tls-ca/ca-cert.pem
#echo FABRIC_CA_CLIENT_TLS_CERTFILES
#echo $FABRIC_CA_CLIENT_TLS_CERTFILES

#export FABRIC_CA_CLIENT_MSPDIR=msp
#echo FABRIC_CA_CLIENT_MSPDIR
#echo $FABRIC_CA_CLIENT_MSPDIR

#export FABRIC_CA_CLIENT_TLS_CLIENTCERT_FILE=/etc/hyperledger/client/admin/tls-msp/signcerts/cert.pem
#echo FABRIC_CA_CLIENT_TLS_CLIENTCERT_FILE
#echo $FABRIC_CA_CLIENT_TLS_CLIENTCERT_FILE

#export FABRIC_CA_CLIENT_TLS_CLIENTKEY_FILE=/etc/hyperledger/client/admin/tls-msp/keystore/key.pem
#echo FABRIC_CA_CLIENT_TLS_CLIENTKEY_FILE
#echo $FABRIC_CA_CLIENT_TLS_CLIENTKEY_FILE

TLS_CA_CERT="/etc/hyperledger/client/tls_root_cert/tls-ca-cert.pem"

#export FABRIC_CA_CLIENT_TLS_CERTFILES=/etc/hyperledger/server/tls-ca/ca-cert.pem
#export FABRIC_CA_CLIENT_TLS_CLIENTCERT_FILE=/etc/hyperledger/client/tls-ca/admin/msp/signcerts/cert.pem
#export FABRIC_CA_CLIENT_TLS_CLIENTKEY_FILE=/etc/hyperledger/client/tls-ca/admin/msp/keystore/key.pem



mkdir -p /etc/hyperledger/client/ca

# Ustawienie katalogu roboczego klienta Fabric CA
export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/admin

# Ścieżki do certyfikatów TLS
export FABRIC_CA_CLIENT_TLS_CERTFILES=/etc/hyperledger/server/tls-ca/ca-cert.pem
export FABRIC_CA_CLIENT_TLS_CLIENTCERT_FILE=/etc/hyperledger/client/tls-ca/admin/msp/signcerts/cert.pem
export FABRIC_CA_CLIENT_TLS_CLIENTKEY_FILE=/etc/hyperledger/client/tls-ca/admin/msp/keystore/key.pem

# Konfiguracja CSR (Certificate Signing Request)
export FABRIC_CA_CLIENT_CSR_CN=admin
export FABRIC_CA_CLIENT_CSR_HOSTS="admin,localhost"
#export FABRIC_CA_CLIENT_CSR_NAMES='[{"C":"PL","ST":"Swietokrzyskie","L":"Kielce","O":"Furniture Makers","OU":"Fabric"}]'

# Nazwa CA
export FABRIC_CA_CLIENT_CANAME=furnituresmakers-ca

# Debugowanie (opcjonalnie)
export FABRIC_CA_CLIENT_DEBUG=true


#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/admin

#fabric-ca-client enroll -d -u https://admin:adminpw@localhost:7054 --csr.hosts "$FABRIC_CA_CLIENT_CSR_HOSTS"

fabric-ca-client enroll -u https://admin:adminpw@localhost:7054 \
  --tls.certfiles /etc/hyperledger/server/tls-ca/ca-cert.pem \
  --tls.client.certfile /etc/hyperledger/client/tls-ca/admin/msp/signcerts/cert.pem \
  --tls.client.keyfile /etc/hyperledger/client/tls-ca/admin/msp/keystore/key.pem \
  --mspdir /etc/hyperledger/client/ca/admin/msp \
  --csr.hosts "admin,localhost"

mv /etc/hyperledger/client/ca/admin/msp/keystore/* /etc/hyperledger/client/ca/admin/msp/keystore/furnituresmakers-key.pem

# Peer0
export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/peer0
fabric-ca-client enroll -u https://peer0:peer0pw@localhost:7054 \
  --tls.certfiles /etc/hyperledger/server/tls-ca/ca-cert.pem \
  --tls.client.certfile /etc/hyperledger/client/tls-ca/peer0/msp/signcerts/cert.pem \
  --tls.client.keyfile /etc/hyperledger/client/tls-ca/peer0/msp/keystore/key.pem \
  --csr.hosts "peer0,localhost"

# Peer1
export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/peer1
fabric-ca-client enroll -u https://peer1:peer1pw@localhost:7054 \
  --tls.certfiles /etc/hyperledger/server/tls-ca/ca-cert.pem \
  --tls.client.certfile /etc/hyperledger/client/tls-ca/peer1/msp/signcerts/cert.pem \
  --tls.client.keyfile /etc/hyperledger/client/tls-ca/peer1/msp/keystore/key.pem \
  --csr.hosts "peer1,localhost"

# Peer2
export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/peer2
fabric-ca-client enroll -u https://peer2:peer2pw@localhost:7054 \
  --tls.certfiles /etc/hyperledger/server/tls-ca/ca-cert.pem \
  --tls.client.certfile /etc/hyperledger/client/tls-ca/peer2/msp/signcerts/cert.pem \
  --tls.client.keyfile /etc/hyperledger/client/tls-ca/peer2/msp/keystore/key.pem \
  --csr.hosts "peer2,localhost"

# Orderer0
export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/orderer0
fabric-ca-client enroll -u https://orderer0:ordererpw@localhost:7054 \
  --tls.certfiles /etc/hyperledger/server/tls-ca/ca-cert.pem \
  --tls.client.certfile /etc/hyperledger/client/tls-ca/orderer0/msp/signcerts/cert.pem \
  --tls.client.keyfile /etc/hyperledger/client/tls-ca/orderer0/msp/keystore/key.pem \
  --csr.hosts "orderer0,localhost"

# Production API
export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/production_api
fabric-ca-client enroll -u https://production_api:productionapipw@localhost:7054 \
  --tls.certfiles /etc/hyperledger/server/tls-ca/ca-cert.pem \
  --tls.client.certfile /etc/hyperledger/client/tls-ca/production_api/msp/signcerts/cert.pem \
  --tls.client.keyfile /etc/hyperledger/client/tls-ca/production_api/msp/keystore/key.pem \
  --csr.hosts "production_api,localhost"

# Logistics API
export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/logistics_api
fabric-ca-client enroll -u https://logistics_api:logisticsapipw@localhost:7054 \
  --tls.certfiles /etc/hyperledger/server/tls-ca/ca-cert.pem \
  --tls.client.certfile /etc/hyperledger/client/tls-ca/logistics_api/msp/signcerts/cert.pem \
  --tls.client.keyfile /etc/hyperledger/client/tls-ca/logistics_api/msp/keystore/key.pem \
  --csr.hosts "logistics_api,localhost"

# Sales API
export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/sales_api
fabric-ca-client enroll -u https://sales_api:salesapipw@localhost:7054 \
  --tls.certfiles /etc/hyperledger/server/tls-ca/ca-cert.pem \
  --tls.client.certfile /etc/hyperledger/client/tls-ca/sales_api/msp/signcerts/cert.pem \
  --tls.client.keyfile /etc/hyperledger/client/tls-ca/sales_api/msp/keystore/key.pem \
  --csr.hosts "sales_api,localhost"


