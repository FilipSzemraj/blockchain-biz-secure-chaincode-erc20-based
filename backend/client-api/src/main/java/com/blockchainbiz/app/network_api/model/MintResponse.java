package com.blockchainbiz.app.network_api.model;

import com.blockchainbiz.app.network_api.dto.token.ConfirmationWithHashDTO;
import com.owlike.genson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@Relation(collectionRelation = "confirmations") // For JSON representation
public class MintResponse extends RepresentationModel<MintResponse> {

    private List<ConfirmationWithHashDTO> mints;
    private String error;

    public static MintResponse fromMapList(List<Map<String, Object>> mintsMap) {
        List<ConfirmationWithHashDTO> mints = mintsMap.stream()
                .map(ConfirmationWithHashDTO::fromMap)
                .collect(Collectors.toList());
        return new MintResponse(mints);
    }
    public MintResponse(List<ConfirmationWithHashDTO> mints) {
        this.mints = mints;
        this.error = null;
    }

    public static MintResponse fromError(String errorMessage){
    return MintResponse.builder()
            .mints(List.of())
            .error(errorMessage)
            .build();
    }
}