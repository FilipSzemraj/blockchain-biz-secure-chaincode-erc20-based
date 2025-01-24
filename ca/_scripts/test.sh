source ./users_array.sh "$ORG_QUICK_NAME"
PEER_NAME="peer0"
PEER_ORG="Furnitures_Makers"
PEER_PORT=8051

ORG_NAME_WITHOUT_=$(echo "${PEER_ORG//_/}")

ORG_QUICK_NAME=$(echo "${PEER_ORG//_/}" | tr '[:upper:]' '[:lower:]')

echo $ORG_QUICK_NAME
ACTUAL_PEER_FULL_NAME="${PEER_NAME}.${ORG_QUICK_NAME}.com"

BOOTSTRAPUSERS=()

for ITEM in "${USERS[@]}"; do
    IFS=":" read -r NAME _ <<< "$ITEM"
    if [[ "$NAME" =~ ^peer[0-9]+$ && "$NAME" != "$PEER_NAME" ]]; then
        PEER_FULL_NAME="${NAME}.${ORG_QUICK_NAME}.com:${PEER_PORT:0:2}51"
        BOOTSTRAPUSERS+=("$PEER_FULL_NAME")
    fi
done

export BOOTSTRAP="${BOOTSTRAPUSERS[*]}"
echo $BOOTSTRAP