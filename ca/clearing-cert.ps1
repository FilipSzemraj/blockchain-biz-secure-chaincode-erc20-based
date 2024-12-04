$BASE_DIR = "C:\Users\filip\Documents\GitHub\blockchain-biz-secure\ca"

$ORGANIZATIONS = @(
    "Furnitures_Makers",
    "Wood_Supply",
    "Yacht_Sales"
)

# Delete all `msp` folders inside `/client/<any_name>/`
#Get-ChildItem -Path "$BASE_DIR" -Recurse -Directory | Where-Object { $_.Name -in @("msp", "tls-msp") } | Remove-Item -Recurse -Force
$ENV_GLOBAL="$BASE_DIR\_env"
$SHARED_CERTS_GLOBAL="$BASE_DIR\_shared_certs"
if (Test-Path $ENV_GLOBAL){
    Remove-Item "$ENV_GLOBAL\*" -Recurse -Force
}
if(Test-Path $SHARED_CERTS_GLOBAL){
    Remove-Item "$SHARED_CERTS_GLOBAL\*" -Recurse -Force
}


foreach ($ORG in $ORGANIZATIONS) {
    Write-Host "Cleaning up directories for organization: $ORG`n" -ForegroundColor Green

    # Delete everything inside `/client/`
    $CLIENT_DIR = "$BASE_DIR\$ORG\crypto\client"
    if (Test-Path $CLIENT_DIR) {
        Write-Host "Cleaning $CLIENT_DIR"
        Get-ChildItem -Path $CLIENT_DIR -Recurse | Where-Object {
            $_.Name -ne "ca" -and $_.Name -ne "tls-ca" -and $_.Name -ne "tls_root_cert" -and $_.Name -ne "tls-ca"
        } | Remove-Item -Recurse -Force
    }

    $ENV="$BASE_DIR\$ORG\crypto\_env"
    $SHARED_CERTS="$BASE_DIR\$ORG\crypto\_shared_certs"
    if (Test-Path $ENV){
        Remove-Item "$ENV\*" -Recurse -Force
    }
    if(Test-Path $SHARED_CERTS){
        Remove-Item "$SHARED_CERTS\*" -Recurse -Force
    }
    # Delete everything inside `/client/tls_root_cert/`
    #$TLS_ROOT_CERT_DIR = "$BASE_DIR\$ORG\crypto\client\tls_root_cert"
    #if (Test-Path $TLS_ROOT_CERT_DIR) {
    #    Write-Host "Cleaning $TLS_ROOT_CERT_DIR"
    #    Remove-Item "$TLS_ROOT_CERT_DIR\*" -Recurse -Force
    #}

    # Delete everything inside `/server/ca` except `fabric-ca-server-config.yaml`
    $SERVER_DIR = "$BASE_DIR\$ORG\crypto\server"
    if (Test-Path $SERVER_DIR) {
        Write-Host "Cleaning $SERVER_DIR (preserving fabric-ca-server-config.yaml)"
        Get-ChildItem -Path $SERVER_DIR -Recurse | Where-Object {
            $_.Name -ne "fabric-ca-server-config.yaml" -and $_.Name -ne "msp" -and $_.Name -ne "config.yaml" -and $_.Name -ne "ca" -and $_.Name -ne "tls-ca"
        } | Remove-Item -Recurse -Force
    }

    # Delete everything inside `/server/tls-ca` except `fabric-ca-server-config.yaml`
    #$SERVER_TLS_DIR = "$BASE_DIR\$ORG\crypto\server\tls-ca"
    #if (Test-Path $SERVER_TLS_DIR) {
    #    Write-Host "Cleaning $SERVER_TLS_DIR (preserving fabric-ca-server-config.yaml)"
    #    Get-ChildItem -Path $SERVER_TLS_DIR -Recurse | Where-Object {
    #        $_.Name -ne "fabric-ca-server-config.yaml" -and $_.Name -ne "msp" -and $_.Name -ne "config.yaml"
    #    } | Remove-Item -Recurse -Force
    #}

    Write-Host "`nCompleted cleanup for $ORG" -ForegroundColor Green
    Write-Host "----------------------------------------" -ForegroundColor Green
}

Write-Host "All organizations have been processed" -ForegroundColor Red

