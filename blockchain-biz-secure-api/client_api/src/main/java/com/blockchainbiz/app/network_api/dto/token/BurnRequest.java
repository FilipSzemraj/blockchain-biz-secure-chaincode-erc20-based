package com.blockchainbiz.app.network_api.dto.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BurnRequest {
    private int amount;
    private String burnWallet;
    private String id;
    private String recipientAccount;
    private long timestamp;

    /**
     * Converts a map (obtained from deserializing JSON) into a BurnRequest object.
     *
     * @param data the map representing the JSON object for burnRequest
     * @return a BurnRequest instance
     */
    public static BurnRequest fromMap(Map<String, Object> data) {
        // Note: Depending on how numbers are deserialized, you may need to adjust these casts.
        int amount = (data.get("amount") instanceof Number)
                ? ((Number) data.get("amount")).intValue()
                : Integer.parseInt((String) data.get("amount"));
        String burnWallet = (String) data.get("burnWallet");
        String id = (String) data.get("id");
        String recipientAccount = (String) data.get("recipientAccount");
        long timestamp = (data.get("timestamp") instanceof Number)
                ? ((Number) data.get("timestamp")).longValue()
                : Long.parseLong((String) data.get("timestamp"));

        return BurnRequest.builder()
                .amount(amount)
                .burnWallet(burnWallet)
                .id(id)
                .recipientAccount(recipientAccount)
                .timestamp(timestamp)
                .build();
    }
}
