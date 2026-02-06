package com.blockchainbiz.app.network_api.dto.token;

import lombok.Data;

@Data
public class BurnInvocationRequest {
    private String channelName;
    private String chaincodeName;

    // Chaincode param
    private String amountStr;  // chaincode function: public String Burn(Context ctx, String amountStr)
}