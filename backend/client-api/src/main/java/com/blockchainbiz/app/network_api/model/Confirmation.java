package com.blockchainbiz.app.network_api.model;

import com.owlike.genson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "confirmations")
public class Confirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String refNumber;

    @Column(nullable=false)
    private long amount;

    @Column(nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date transferDate;

    @Column(nullable=false)
    private String fromIBAN;

    @Column(nullable=false)
    private String toIBAN;

    @ElementCollection
    @CollectionTable(name = "mint_raw_data", joinColumns = @JoinColumn(name = "mint_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, Object> rawData;

    public static Confirmation fromMap(Map<String, Object> data){
        return Confirmation.builder()
                .refNumber((String) data.get("refNumber"))
                .amount(Long.parseLong(data.get("amount").toString()))
                .transferDate(new Date(Long.parseLong(data.get("transferDate").toString())))
                .fromIBAN((String) data.get("fromIBAN"))
                .toIBAN((String) data.get("toIBAN"))
                .rawData((Map<String, Object>) data.get("rawData"))
                .build();
    }

}


