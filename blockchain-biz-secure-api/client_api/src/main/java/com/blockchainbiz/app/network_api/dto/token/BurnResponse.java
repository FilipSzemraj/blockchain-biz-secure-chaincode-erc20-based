package com.blockchainbiz.app.network_api.dto.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BurnResponse extends RepresentationModel<BurnResponse> {
    // List of burn operations along with their associated hash
    private List<BurnWithHash> burns;

    // Optional error message
    private String error;

    // Convenience constructor when there is no error
    public BurnResponse(List<BurnWithHash> burns) {
        this.burns = burns;
        this.error = null;
    }

    public static BurnResponse fromError(String errorMessage) {
        return BurnResponse.builder()
                .burns(List.of())
                .error(errorMessage)
                .build();
    }
}
