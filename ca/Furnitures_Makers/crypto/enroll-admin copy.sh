cp -v /etc/hyperledger/server/tls-cert.pem /etc/hyperledger/client/tls_root_cert/tls-ca-cert.pem

if [ $? -eq 0 ]; then
    echo "CA certificate copied successfully."
else
    echo "Failed to copy CA certificate."
    exit 1
fi

echo FABRIC_CA_SERVER_HOME
echo $FABRIC_CA_SERVER_HOME

export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/admin

echo FABRIC_CA_CLIENT_HOME
echo $FABRIC_CA_CLIENT_HOME

TLS_CA_CERT="/etc/hyperledger/client/tls_root_cert/tls-ca-cert.pem"

fabric-ca-client enroll -d -u https://admin:adminpw@localhost:7054 --tls.certfiles "$TLS_CA_CERT" --enrollment.profile tls --mspdir msp

#############
fabric-ca-client register -d --id.name admin --id.secret adminpw -u https://localhost:7054 --mspdir tls-ca/tlsadmin/msp --id.type client --tls.certfiles $TLS_CA_CERT  --id.affiliation Furnitures_Makers --id.attrs "hf.Registrar.Roles=*,hf.Registrar.Attributes=*,hf.Revoker=true,hf.GenCRL=true" --caname tls-ca

fabric-ca-client enroll -d -u https://admin:adminpw@localhost:7054 --tls.certfiles $TLS_CA_CERT --enrollment.profile tls --csr.hosts 'furnituresmakers-ca,localhost,127.0.0.1,admin' --mspdir tls-ca/admin/msp



#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/admin
#set HOSTNAME=admin

#fabric-ca-client enroll --enrollment.profile tls -u https://admin:adminpw@localhost:7054 --caname tls-ca --tls.certfiles $TLS_CA_CERT --enrollment.profile tls --mspdir tls-ca/msp

#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/peer0
set HOSTNAME=peer0


fabric-ca-client enroll --enrollment.profile tls -u https://peer0:peer0pw@localhost:7054 --caname tls-ca --tls.certfiles $TLS_CA_CERT --enrollment.profile tls --mspdir tls-ca/peer0/msp

#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/peer1
set HOSTNAME=peer1


fabric-ca-client enroll --enrollment.profile tls -u https://peer1:peer1pw@localhost:7054 --caname tls-ca --tls.certfiles $TLS_CA_CERT --enrollment.profile tls --mspdir tls-ca/peer1/msp

#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/peer2
set HOSTNAME=peer2


fabric-ca-client enroll --enrollment.profile tls -u https://peer2:peer2pw@localhost:7054 --caname tls-ca --tls.certfiles $TLS_CA_CERT --enrollment.profile tls --mspdir tls-ca/peer2/msp

#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/orderer0
set HOSTNAME=orderer0


fabric-ca-client enroll --enrollment.profile tls -u https://orderer0:orderer0pw@localhost:7054 --caname tls-ca --tls.certfiles $TLS_CA_CERT --enrollment.profile tls --mspdir tls-ca/orderer0/msp

#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/production_api
set HOSTNAME=production_api


fabric-ca-client enroll --enrollment.profile tls -u https://production_api:productionapipw@localhost:7054 --caname tls-ca --tls.certfiles $TLS_CA_CERT --enrollment.profile tls --mspdir tls-ca/production_api/msp

#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/sales_api
set HOSTNAME=sales_api


fabric-ca-client enroll --enrollment.profile tls -u https://sales_api:salesapipw@localhost:7054 --caname tls-ca --tls.certfiles $TLS_CA_CERT --enrollment.profile tls --mspdir tls-ca/sales_api/msp

#export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/logistics_api
set HOSTNAME=logistics_api


fabric-ca-client enroll --enrollment.profile tls -u https://logistics_api:logisticsapipw@localhost:7054 --caname tls-ca --tls.certfiles $TLS_CA_CERT --enrollment.profile tls --mspdir tls-ca/logistics_api/msp


cp -r /etc/hyperledger/client/tls-ca/admin/msp/signcerts/cert.pem /etc/hyperledger/server/ca/tls/cert.pem
cp -r /etc/hyperledger/client/tls-ca/admin/msp/keystore/*.pem /etc/hyperledger/server/ca/tls/key.pem


echo "Stopping existing Fabric CA server..."
pkill -f "fabric-ca-server" || echo "No running Fabric CA server found."

#echo "Setting environment variables for the new Fabric CA server..."
#export FABRIC_CA_SERVER_HOME=/etc/hyperledger/server
#export FABRIC_CA_SERVER_TLS_CLIENTAUTH_TYPE=RequireAndVerifyClientCert

#echo "Starting new Fabric CA server with updated configuration..."
#fabric-ca-server start -b admin:adminpw --cafiles /etc/hyperledger/server/ca/fabric-ca-server-config.yaml
 #################

# Peer1
export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/peer1
fabric-ca-client enroll -u https://peer1:peer1pw@localhost:7054 --tls.certfiles $TLS_CA_CERT --csr.hosts "peer1,localhost"

# Peer2
export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/peer2
fabric-ca-client enroll -u https://peer2:peer2pw@localhost:7054 --tls.certfiles $TLS_CA_CERT --csr.hosts "peer2,localhost"

# Orderer0
export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/orderer0
fabric-ca-client enroll -u https://orderer0:ordererpw@localhost:7054 --tls.certfiles $TLS_CA_CERT --csr.hosts "orderer0,localhost"

# Production API
export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/production_api
fabric-ca-client enroll -u https://production_api:productionapipw@localhost:7054 --tls.certfiles $TLS_CA_CERT --csr.hosts "production_api,localhost"

# Logistics API
export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/logistics_api
fabric-ca-client enroll -u https://logistics_api:logisticsapipw@localhost:7054 --tls.certfiles $TLS_CA_CERT --csr.hosts "logistics_api,localhost"

# Sales API
export FABRIC_CA_CLIENT_HOME=/etc/hyperledger/client/ca/sales_api
fabric-ca-client enroll -u https://sales_api:salesapipw@localhost:7054 --tls.certfiles $TLS_CA_CERT --csr.hosts "sales_api,localhost"

