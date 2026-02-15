cp -f creating-channel-with-retry.sh ../_config_files/configtx/creating-channel-with-retry.sh
cp -f healthcheck-admin-smart.sh ../_config_files/configtx/healthcheck-admin-smart.sh

echo "Copied scripts to ../_config_files/configtx/"


docker-compose -f "../docker-compose.configtx.all.yaml" down

docker-compose -f "../docker-compose.configtx.all.yaml" config

docker-compose -f "../docker-compose.configtx.all.yaml" build


docker-compose -f "../docker-compose.configtx.all.yaml" up
#winpty docker exec -it $(docker-compose --project-name "admin-furnituresmakers" -f "../docker-compose.configtx.yaml" ps -q configtx) bash -c "echo 'Checking output...'; ls ../output; bash"
#winpty docker exec -it ${CONTAINER_NAME} bash -c "echo 'Checking output...'; ls ../output; bash"
