package com.blockchainbiz.app.network_api.dto.deliveries;

import lombok.Data;

@Data
public class StartDeliveryRequest {
    private String channelName;
    private String chaincodeName;

    private String deliveryId;
}