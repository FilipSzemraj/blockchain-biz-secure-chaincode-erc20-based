PEER_NAME=${1:-peer0}
PEER_ORG=${2:-Furnitures_Makers}
PEER_PORT=${3:-7051}
NEXT_PORT=$((PEER_PORT + 1))

ORG_NAME_WITHOUT_=$(echo "${PEER_ORG//_/}")

# Replace underscores with lowercase and no underscores
ORG_QUICK_NAME=$(echo "${PEER_ORG//_/}" | tr '[:upper:]' '[:lower:]')


source ./users_array.sh "$ORG_QUICK_NAME"

BOOTSTRAPUSERS=""

for ORG in "${ORGS[@]}"; do
  for USER in "${USERS[@]}"; do
      # Split the USER into USERNAME and PASSWORD
      IFS=":" read -r USERNAME PASSWORD <<< "$USER"

      # Check if USERNAME contains "peer"
      if [[ "$USERNAME" == peer* ]]; then
          # Append to BOOTSTRAPUSERS
          BOOTSTRAPUSERS+="${USERNAME}.${ORG}.com:7051,"
      fi
  done
done


export CORE_PEER_ID="${PEER_NAME}.${ORG_QUICK_NAME}.com"
export CORE_PEER_NETWORKID="${PEER_NAME}.${ORG_QUICK_NAME}.com"
export CORE_PEER_LISTENADDRESS=0.0.0.0:$PEER_PORT
export CORE_PEER_ADDRESS="${PEER_NAME}.${ORG_QUICK_NAME}.com:${PEER_PORT}"
export CORE_PEER_CHAINCODELISTENADDRESS=0.0.0.0:${NEXT_PORT}
export CORE_PEER_LOCALMSPID=$ORG_NAME_WITHOUT_

export CORE_GOSSIP_ENDPOINT="${PEER_NAME}.${ORG_QUICK_NAME}.com"
export CORE_GOSSIP_EXTERNALENDPOINT="${PEER_NAME}.${ORG_QUICK_NAME}.com:$PEER_PORT"

export CORE_OPERATIONS_LISTENADDRESS="${PEER_NAME}.${ORG_QUICK_NAME}.com:9443"

#BASE_DIR=$(dirname "$(realpath "${BASH_SOURCE[0]}")")

#cd "$BASE_DIR/.."

# Write variables to .env file
cat > "../_config_files/peer/.env" <<EOF
PEER_NAME=${PEER_NAME}
PEER_ORG=${PEER_ORG}
PEER_PORT=${PEER_PORT}
NEXT_PORT=${NEXT_PORT}
ORG_QUICK_NAME=${ORG_QUICK_NAME}

CORE_PEER_ID=${PEER_NAME}.${ORG_QUICK_NAME}.com
CORE_PEER_NETWORKID=${PEER_NAME}.${ORG_QUICK_NAME}.com
CORE_PEER_LISTENADDRESS=0.0.0.0:${PEER_PORT}
CORE_PEER_ADDRESS=${PEER_NAME}.${ORG_QUICK_NAME}.com:${PEER_PORT}
CORE_PEER_CHAINCODELISTENADDRESS=0.0.0.0:${NEXT_PORT}
CORE_PEER_LOCALMSPID=${CORE_PEER_LOCALMSPID}

CORE_PEER_GOSSIP_ENDPOINT=${PEER_NAME}.${ORG_QUICK_NAME}.com:${PEER_PORT}
CORE_PEER_GOSSIP_EXTERNALENDPOINT=${PEER_NAME}.${ORG_QUICK_NAME}.com:${PEER_PORT}

CORE_OPERATIONS_LISTENADDRESS=${PEER_NAME}.${ORG_QUICK_NAME}.com:9443
EOF

cp -f start-peer.sh ../_config_files/peer/start-peer.sh

echo "Copied start-peer.sh to ../_config_files/peer/start-peer.sh"


docker-compose --project-name "admin-${ORG_NAME}" --env-file ../_config_files/peer/.env -f "../docker-compose.peer.yaml" down

docker-compose --env-file ../_config_files/peer/.env -f "../docker-compose.peer.yaml" config

docker-compose --env-file ../_config_files/peer/.env -f "../docker-compose.peer.yaml" build


docker-compose --env-file ../_config_files/peer/.env -f "../docker-compose.peer.yaml" up






