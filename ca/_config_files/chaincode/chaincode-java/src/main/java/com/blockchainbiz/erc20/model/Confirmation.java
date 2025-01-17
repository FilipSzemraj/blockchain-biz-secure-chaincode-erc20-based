package com.blockchainbiz.erc20.model;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.Date;
import java.util.Map;

@DataType()
public final class Confirmation {

    @Property()
    @JsonProperty("refNumber")
    private String refNumber;

    @Property()
    @JsonProperty("amount")
    private long amount;

    @Property()
    @JsonProperty("transferDate")
    private Date transferDate;

    @Property()
    @JsonProperty("fromIBAN")
    private String fromIBAN;

    @Property()
    @JsonProperty("toIBAN")
    private String toIBAN;

    @Property()
    @JsonProperty("rawData")
    private Map<String, Object> rawData;

    /** Default constructor */
    public Confirmation() {
        super();
    }

    /**
     * Constructor of the class
     *
     * @param refNumber unique identifier of the burn transaction
     * @param amount the amount of tokens being burned
     * @param transferDate the amount of tokens being burned
     * @param fromIBAN the amount of tokens being burned
     * @param toIBAN the amount of tokens being burned
     * @param amount the amount of tokens being burned
     * @param rawData raw transaction data provided by the bank
     */
    public Confirmation(
            @JsonProperty("refNumber") final String refNumber,
            @JsonProperty("amount") final long amount,
            @JsonProperty("transferDate") final Date transferDate,
            @JsonProperty("fromIBAN") final String fromIBAN,
            @JsonProperty("toIBAN") final String toIBAN,
            @JsonProperty("rawData") final Map<String, Object> rawData) {
        super();
        this.refNumber = refNumber;
        this.amount = amount;
        this.transferDate = transferDate;
        this.fromIBAN = fromIBAN;
        this.toIBAN = toIBAN;
        this.rawData = rawData;
    }

    public String getRefNumber() {
        return refNumber;
    }

    public long getAmount() {
        return amount;
    }
    public Date getTransferDate() { return transferDate; }
    public String getFromIBAN() { return fromIBAN; }
    public String getToIBAN() { return toIBAN; }


    public Map<String, Object> getRawData() {
        return rawData;
    }
    public void setRawData(Map<String, Object> rawData) {
        this.rawData = rawData;
    }
}