$BASE_DIR = "C:\Users\filip\Documents\GitHub\blockchain-biz-secure\ca"

# Delete all `msp` folders inside `/client/<any_name>/`
Get-ChildItem -Path "$BASE_DIR" -Recurse -Directory | Where-Object { $_.Name -in @("msp", "tls-msp") } | Remove-Item -Recurse -Force


# Delete everything inside `/client/tls_ca/`
$TLS_CA_DIR = "$BASE_DIR\Furnitures_Makers\crypto\client\tls_ca"
if (Test-Path $TLS_CA_DIR) {
    Remove-Item "$TLS_CA_DIR\*" -Recurse -Force
}

# Delete everything inside `/client/tls_root_cert/`
$TLS_ROOT_CERT_DIR = "$BASE_DIR\Furnitures_Makers\crypto\client\tls_root_cert"
if (Test-Path $TLS_ROOT_CERT_DIR) {
    Remove-Item "$TLS_ROOT_CERT_DIR\*" -Recurse -Force
}

# Delete everything inside `/server/` except `fabric-ca-server-config.yaml`
$SERVER_DIR = "$BASE_DIR\Furnitures_Makers\crypto\server\ca"
Get-ChildItem -Path $SERVER_DIR -Recurse | Where-Object {
    $_.Name -ne "fabric-ca-server-config.yaml"
} | Remove-Item -Recurse -Force

$SERVER_DIR = "$BASE_DIR\Furnitures_Makers\crypto\server\tls-ca"
Get-ChildItem -Path $SERVER_DIR -Recurse | Where-Object {
    $_.Name -ne "fabric-ca-server-config.yaml"
} | Remove-Item -Recurse -Force
