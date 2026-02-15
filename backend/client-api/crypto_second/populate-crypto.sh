#!/bin/bash

MAIN_FOLDER_OF_CERTIFICATIONS="C:/Users/filip/Documents/GitHub/blockchain-biz-secure/ca"
DESTINATION_FOLDER="C:/Users/filip/Documents/GitHub/blockchain-biz-secure-api/client_api/crypto"


ORG_FOLDERS=("Furnitures_Makers" "Wood_Supply" "Yacht_Sales")

for ORG in "${ORG_FOLDERS[@]}"; do
  SOURCE="${MAIN_FOLDER_OF_CERTIFICATIONS}/${ORG}/crypto/client/ca/*_api/msp"
  
  echo "Pliki dla organizacji: $ORG"
  
  if compgen -G "${SOURCE}" > /dev/null; then
    for file in ${SOURCE}; do
       LAST_FOLDER=$(basename "$(dirname "$file")")  # folder kończący się na _api
       LAST_SUBFOLDER=$(basename "$file")           # ostatni folder (msp)

       TARGET="${DESTINATION_FOLDER}/${ORG}/${LAST_FOLDER}/${LAST_SUBFOLDER}"

       mkdir -p "$TARGET"

       cp -r "$file/"* "$TARGET/"

       echo "Skopiowano $file do $TARGET"

      echo "$file"
    done
  else
    echo "Brak plików w ${SOURCE}"
  fi
done