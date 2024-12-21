MODE=${1:-easy}

ORDERER_FOLDER="../orderer/"
PEER_FOLDER="../peer/"
ADMIN_FOLDER="../configtx/"

MSP_FOLDERS=$(find ../.. -type d -name '*msp*')

for CURRENT_PATH in $MSP_FOLDERS; do
  if [ $MODE = "easy" ]; then
    # Copy only if the file doesn't exist
    if [ ! -f "$CURRENT_PATH/config.yaml" ]; then
      cp ./config.yaml "$CURRENT_PATH"
      echo "Copied to $CURRENT_PATH (easy mode)"
    else
      echo "Skipped $CURRENT_PATH, file already exists (easy mode)"
    fi
  elif [ $MODE = "all" ]; then
    # Force copy with verbose output
    cp -v ./config.yaml "$CURRENT_PATH"
  else
    echo "Inappropriate parameter, choose between easy and all."
    exit 1
  fi
done

cp ./config.yaml "$ORDERER_FOLDER"
cp ./config.yaml "$PEER_FOLDER"
cp ./config.yaml "$ADMIN_FOLDER"

echo "Completed copying to $ORDERER_FOLDER, $PEER_FOLDER, and $ADMIN_FOLDER"
