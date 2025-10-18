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
public class BurnWithHash {
    private BurnRequest burnRequest;
    private String hash;

    /**
     * Converts a map (obtained from deserializing JSON) into a BurnResponse object.
     *
     * @param data the map representing the JSON object
     * @return a BurnResponse instance
     */
    public static BurnWithHash fromMap(Map<String, Object> data) {
        Map<String, Object> burnRequestData = (Map<String, Object>) data.getOrDefault("burnRequest", "");
        BurnRequest burnInvocationRequest = BurnRequest.fromMap(burnRequestData);
        String hash = (String) data.get("hash");
        return BurnWithHash.builder()
                .burnRequest(burnInvocationRequest)
                .hash(hash)
                .build();
    }
}
