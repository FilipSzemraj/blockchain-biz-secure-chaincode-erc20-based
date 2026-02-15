package com.blockchainbiz.app.network_api.model;

import com.blockchainbiz.app.network_api.model.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "partial_deliveries")
public class PartialDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String deliveryId;

    @Column(nullable = false)
    private String goodsType;

    @Column(nullable = false)
    private String goodsDetails;

    @Column(nullable = false)
    private long goodsQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus currentStatus;

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
    @CollectionTable(name = "partial_delivery_raw_data", joinColumns = @JoinColumn(name = "partial_delivery_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, Object> rawData;

    @ManyToOne
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;


    public static PartialDelivery fromMap(Map<String, Object> data) {
        return PartialDelivery.builder()
                .deliveryId((String) data.get("deliveryId"))
                .goodsType((String) data.get("goodsType"))
                .goodsDetails((String) data.get("goodsDetails"))
                .goodsQuantity(Long.parseLong(data.get("goodsQuantity").toString()))
                .currentStatus(DeliveryStatus.valueOf((String) data.get("currentStatus")))
                .creationTimestamp(new Date(Long.parseLong(data.get("creationTimestamp").toString())))
                .updateTimestamp(new Date(Long.parseLong(data.get("updateTimestamp").toString())))
                .expiryTimestamp(new Date(Long.parseLong(data.get("expiryTimestamp").toString())))
                .disputeReason((String) data.get("disputeReason"))
                .rawData((Map<String, Object>) data.get("rawData"))
                // Pole 'delivery' ustawiamy na null; można je ustawić później, jeśli to konieczne.
                .delivery(null)
                .build();
    }

    public static PartialDelivery fromMap(Map<String, Object> data, Delivery delivery) {
        return PartialDelivery.builder()
                .deliveryId((String) data.get("deliveryId"))
                .goodsType((String) data.get("goodsType"))
                .goodsDetails((String) data.get("goodsDetails"))
                .goodsQuantity(Long.parseLong(data.get("goodsQuantity").toString()))
                .currentStatus(DeliveryStatus.valueOf((String) data.get("currentStatus")))
                .creationTimestamp(new Date(Long.parseLong(data.get("creationTimestamp").toString())))
                .updateTimestamp(new Date(Long.parseLong(data.get("updateTimestamp").toString())))
                .expiryTimestamp(new Date(Long.parseLong(data.get("expiryTimestamp").toString())))
                .disputeReason((String) data.get("disputeReason"))
                .rawData((Map<String, Object>) data.get("rawData"))
                .delivery(delivery)
                .build();
    }
}
