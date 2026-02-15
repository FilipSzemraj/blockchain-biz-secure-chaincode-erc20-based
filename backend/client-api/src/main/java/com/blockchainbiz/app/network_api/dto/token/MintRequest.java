package com.blockchainbiz.app.network_api.dto.token;

import lombok.Data;

@Data
public class MintRequest {
    private String channelName;
    private String chaincodeName;

    // Chaincode param
    private String jsonContent;
}