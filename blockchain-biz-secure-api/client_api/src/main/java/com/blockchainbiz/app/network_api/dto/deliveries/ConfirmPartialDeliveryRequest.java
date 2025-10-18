package com.blockchainbiz.app.network_api.dto.deliveries;

import lombok.Data;

@Data
public class ConfirmPartialDeliveryRequest {
    private String channelName;
    private String chaincodeName;

    private String deliveryId;
    private String partialDeliveryId;
    private long acceptedQuantity;
}