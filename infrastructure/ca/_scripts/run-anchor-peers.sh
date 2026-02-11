source ./users_array.sh "$ORG_QUICK_NAME"

cp -f start-anchor-peer.sh ../_config_files/peer/start-anchor-peer.sh
cp -f add-anchor-peers.sh ../_config_files/peer/add-anchor-peers.sh

echo "Copied start-peer.sh, add-anchor-peers.sh to ../_config_files/peer/"


docker-compose -f "../docker-compose.peer.all.yaml" down

docker-compose -f "../docker-compose.peer.all.yaml" config

docker-compose -f "../docker-compose.peer.all.yaml" build


docker-compose -f "../docker-compose.peer.all.yaml" up