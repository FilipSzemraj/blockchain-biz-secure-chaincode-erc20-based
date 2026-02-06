source ./users_array.sh ""

PEER_LIST=()

for ITEM in "${USERS[@]}"; do
    IFS=":" read -r NAME _ <<< "$ITEM"

    if [[ "$NAME" =~ ^peer[1-9][0-9]*$ ]]; then
        PEER_LIST+=("$NAME")
    fi
done

echo "${PEER_LIST[@]}"

ORGS_FOLDER=("Furnitures_Makers:70" "Wood_Supply:80" "Yacht_Sales:90")

for ORG_ITEM in "${ORGS_FOLDER[@]}"; do
    IFS=":" read -r ORG PORT <<< "$ORG_ITEM"

    echo "${ORG}"

    for PEER in "${PEER_LIST[@]}"; do
        echo "${PEER}.${ORG}.com"
        ./run-peer.sh "${PEER}" "${ORG}" "${PORT}51" &
        sleep 15
    done
done

