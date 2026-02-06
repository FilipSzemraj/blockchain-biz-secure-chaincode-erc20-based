package com.blockchainbiz.app.network_api.dto.token;

import lombok.Data;

@Data
public class TransferRequest {
    private String channelName;
    private String chaincodeName;

    // Chaincode params
    private String to;
    private long value;         // public void Transfer(Context ctx, String to, long value)
}