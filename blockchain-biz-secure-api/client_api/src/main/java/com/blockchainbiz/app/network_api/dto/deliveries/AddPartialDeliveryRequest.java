package com.blockchainbiz.app.network_api.dto.deliveries;

import lombok.Data;

import java.util.Map;

@Data
public class AddPartialDeliveryRequest {
    private String channelName;
    private String chaincodeName;

    private String deliveryId;
    private String goodsType;
    private String goodsDetails;
    private long goodsQuantity;


    private String expiryTimestamp;

    private Map<String, Object> rawData;
}