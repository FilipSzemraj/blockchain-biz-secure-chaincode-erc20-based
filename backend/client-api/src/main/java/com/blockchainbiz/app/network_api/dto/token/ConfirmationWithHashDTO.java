package com.blockchainbiz.app.network_api.dto.token;


import com.blockchainbiz.app.network_api.model.Confirmation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmationWithHashDTO {
    private Confirmation confirmation;
    private String hash;

    public static ConfirmationWithHashDTO fromMap(Map<String, Object> data){
        Map<String, Object> confirmationData = (Map<String, Object>) data.getOrDefault("confirmation", Map.of());
        Confirmation confirmation = Confirmation.fromMap(confirmationData);
        String hash = (String) data.get("hash");
        return ConfirmationWithHashDTO.builder()
                .confirmation(confirmation)
                .hash(hash)
                .build();
    }
}
