package com.blockchainbiz.app.network_api.model.enums;

public enum DeliveryStatus {
    CREATED,          // Delivery has been created, waiting for seller confirmation
    IN_TRANSIT,       // Goods are being delivered
    DELIVERED,        // Goods arrived, waiting for buyer confirmation
    COMPLETED,        // Buyer confirmed delivery & tokens released
    DISPUTE,          // Buyer disputed the delivery
    DISPUTE_RESOLVED, // Dispute has been resolved
    CANCELLED         // Delivery was cancelled before completion
}
