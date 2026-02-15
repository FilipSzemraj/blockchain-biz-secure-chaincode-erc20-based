package com.blockchainbiz.app.network_api.dto.token;

import lombok.Data;

@Data
public class FinalizeBurnRequest {
    private String channelName;
    private String chaincodeName;

    // Chaincode param
    private String jsonContent;  // public void FinalizeBurn(Context ctx, String jsonContent)
}