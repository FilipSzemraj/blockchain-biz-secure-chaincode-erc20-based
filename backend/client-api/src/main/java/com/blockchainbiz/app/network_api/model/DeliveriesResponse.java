package com.blockchainbiz.app.network_api.model;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Relation(collectionRelation = "deliveries")
public class DeliveriesResponse extends RepresentationModel<DeliveriesResponse> {

    private List<Delivery> deliveries;
    private String error;

    public DeliveriesResponse(List<Delivery> deliveries) {
        this.deliveries = deliveries;
        this.error = null;
    }


    public static DeliveriesResponse fromError(String errorMessage) {
        return DeliveriesResponse.builder()
                .deliveries(List.of())
                .error(errorMessage)
                .build();
    }

}
