#!/bin/bash
HOSTNAME=${1:-hostname}
ORG_NAME=""
# Array with user credentials
USERS=("$HOSTNAME-ca:capw:Organization CA"
       "admin:adminpw:admin"
       "peer0:peer0pw:peer"
       "peer1:peer1pw:peer"
       "peer2:peer2pw:peer"
       "orderer0:orderer0pw:orderer"
       "production_api:productionapipw:client"
       "sales_api:salesapipw:client"
       "logistics_api:logisticsapipw:client")

ORGS=("furnituresmakers:FurnituresMakers"
     "woodsupply:WoodSupply"
     "yachtsales:YachtSales")

# Iterate through the array and rebuild it, excluding the match
NEW_ORG=()
for ITEM in "${ORGS[@]}"; do
    IFS=":" read -r lowercase uppercase <<< "$ITEM"

    if [[ "$lowercase" != "$HOSTNAME" ]]; then
        NEW_ORG+=("$ITEM")
    else
        ORG_NAME=$uppercase
    fi
done

# Replace the original array with the new one
ORGS=("${NEW_ORG[@]}")
