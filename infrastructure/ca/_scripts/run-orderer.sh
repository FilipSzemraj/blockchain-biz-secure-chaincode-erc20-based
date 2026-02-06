ORDERER_NAME=${1:-orderer0}
ORDERER_ORG=${2:-Furnitures_Makers}
ORDERER_PORT=${3:-7050}
NEXT_PORT=$((PEER_PORT + 1))

ORG_QUICK_NAME=$(echo "${ORDERER_ORG//_/}" | tr '[:upper:]' '[:lower:]')

source ./users_array.sh "$ORG_QUICK_NAME"

BOOTSTRAPUSERS=""

for ORG in "${ORGS[@]}"; do
  for USER in "${USERS[@]}"; do
      # Split the USER into USERNAME and PASSWORD
      IFS=":" read -r USERNAME PASSWORD <<< "$USER"

      # Check if USERNAME contains "peer"
      if [[ "$USERNAME" == orderer* ]]; then
          # Append to BOOTSTRAPUSERS
          BOOTSTRAPUSERS+="${USERNAME}.${ORG}.com:7050,"

      fi
  done
done
#ORDERER_CONSENSUS_ETCDRAFT_CONSENTERS_

export ORDERER_GENERAL_LISTENADDRESS="${ORDERER_NAME}.${ORG_QUICK_NAME}.com"
export ORDERER_GENERAL_LISTENPORT=$ORDERER_PORT
export ORDERER_GENERAL_LOCALMSPID=$ORDERER_ORG
#ORDERER_TLS_CLIENTROOTCAS_FILES w docker-compose
#ORDERER_CONSENSUS... w start-orderer.sh

#SERIALIZATION OF ORGS ARRAY
export ORGS_STR=$(IFS=:; echo "${ORGS[*]}")


cat > "../_config_files/orderer/.${ORDERER_GENERAL_LISTENADDRESS}.env" <<EOF
ORDERER_GENERAL_LISTENADDRESS=${ORDERER_GENERAL_LISTENADDRESS}
ORDERER_GENERAL_LISTENPORT=$ORDERER_GENERAL_LISTENPORT
ORDERER_GENERAL_LOCALMSPID=$ORDERER_GENERAL_LOCALMSPID
ORDERER_ORG=$ORDERER_ORG
ORDERER_NAME=$ORDERER_NAME
ORG_QUICK_NAME=$ORG_QUICK_NAME
ORGS_STR=$ORGS_STR
EOF

cp -f start-orderer.sh ../_config_files/orderer/start-orderer.sh

echo "Copied start-orderer.sh to ../_config_files/orderer/start-orderer.sh"

docker-compose --project-name "${ORDERER_NAME}-${ORG_QUICK_NAME}" --env-file ../_config_files/orderer/.env -f "../docker-compose.orderer.yaml" down

docker-compose --project-name "${ORDERER_NAME}-${ORG_QUICK_NAME}" --env-file ../_config_files/orderer/.env -f "../docker-compose.orderer.yaml" config

docker-compose --project-name "${ORDERER_NAME}-${ORG_QUICK_NAME}" --env-file ../_config_files/orderer/.env -f "../docker-compose.orderer.yaml" build


docker-compose --project-name "${ORDERER_NAME}-${ORG_QUICK_NAME}" --env-file ../_config_files/orderer/.${ORDERER_GENERAL_LISTENADDRESS}.env -f "../docker-compose.orderer.yaml" up

#winpty docker exec -it ${ORDERER_GENERAL_LISTENADDRESS} bash -c "echo 'Taking control of orderer...'; bash"
