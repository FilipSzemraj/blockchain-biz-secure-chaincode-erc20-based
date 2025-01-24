source ./users_array.sh ""

PEER_LIST=()

for ITEM in "${USERS[@]}"; do
    IFS=":" read -r NAME _ <<< "$ITEM"

    if [[ "$NAME" =~ ^peer[1-9][0-9]*$ ]]; then
        PEER_LIST+=("$NAME")
    fi
done

echo "${PEER_LIST[@]}"

#ORGS_FOLDER=("Furnitures_Makers" "Wood_Supply" "Yacht_Sales")

for ORG in "${ORGS[@]}"; do
    IFS=":" read -r lowercase uppercase <<< "$ORG"
    echo "${ORG}"

    for PEER in "${PEER_LIST[@]}"; do
        CONTAINER_NAME="${PEER}.${lowercase}.com"
        echo "Stopping container: ${CONTAINER_NAME}"
        docker stop "${CONTAINER_NAME}" &  # Uruchomienie w tle, aby działać równolegle
    done
done

