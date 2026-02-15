package com.blockchainbiz.app.network_api.model;

import com.blockchainbiz.app.network_api.model.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String deliveryId;

    @Column(nullable = false)
    private String buyerId;

    @Column(nullable = false)
    private String sellerId;

    @Column(nullable = false)
    private String arbitratorId;

    @Column(nullable = false)
    private long tokenAmount;

    @Column(nullable = false)
    private String goodsType;

    @Column(nullable = false)
    private String goodsDetails;

    @Column(nullable = false)
    private long goodsQuantity;

    @Column(nullable = false)
    private long goodsDeliveredQuantity;

    @Column(nullable = false)
    private long goodsSentQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus currentStatus;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PartialDelivery> partialDeliveries;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTimestamp;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTimestamp;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryTimestamp;

    @Column
    private String disputeReason;

    @ElementCollection
    @CollectionTable(name = "delivery_raw_data", joinColumns = @JoinColumn(name = "delivery_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, Object> rawData;

    public static Delivery fromMap(Map<String, Object> data) {
        Delivery.DeliveryBuilder builder = Delivery.builder()
                .deliveryId((String) data.get("deliveryId"))
                .buyerId((String) data.get("buyerId"))
                .sellerId((String) data.get("sellerId"))
                .arbitratorId((String) data.get("arbitratorId"))
                .tokenAmount(Long.parseLong(data.get("tokenAmount").toString()))
                .goodsType((String) data.get("goodsType"))
                .goodsDetails((String) data.get("goodsDetails"))
                .goodsQuantity(Long.parseLong(data.get("goodsQuantity").toString()))
                .goodsDeliveredQuantity(Long.parseLong(data.get("goodsDeliveredQuantity").toString()))
                .goodsSentQuantity(Long.parseLong(data.get("goodsSentQuantity").toString()))
                .currentStatus(DeliveryStatus.valueOf((String) data.get("currentStatus")))
                .creationTimestamp(new Date(Long.parseLong(data.get("creationTimestamp").toString())))
                .updateTimestamp(new Date(Long.parseLong(data.get("updateTimestamp").toString())))
                .expiryTimestamp(new Date(Long.parseLong(data.get("expiryTimestamp").toString())))
                .disputeReason((String) data.get("disputeReason"))
                .rawData((Map<String, Object>) data.get("rawData"));

        Delivery delivery = builder.build();

        Object partialsObj = data.get("partialDeliveries");
        if (partialsObj instanceof List) {
            List<Map<String, Object>> partialsList = (List<Map<String, Object>>) partialsObj;
            List<PartialDelivery> partials = partialsList.stream()
                    .map(map -> PartialDelivery.fromMap(map, delivery))
                    .collect(Collectors.toList());
            delivery.setPartialDeliveries(partials);
        }
        return delivery;
    }
}
