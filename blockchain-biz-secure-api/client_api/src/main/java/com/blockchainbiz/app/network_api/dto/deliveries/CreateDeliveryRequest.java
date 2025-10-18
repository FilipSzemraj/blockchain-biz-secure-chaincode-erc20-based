package com.blockchainbiz.app.network_api.dto.deliveries;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class CreateDeliveryRequest {

    private String channelName;
    private String chaincodeName;

    private String deliveryId;
    private String buyerId;
    private String sellerId;
    private String arbitratorId;
    private long tokenAmount;
    private String goodsType;
    private String goodsDetails;
    private long goodsQuantity;

    private String expiryTimestamp;

    private Map<String, Object> rawData;

}