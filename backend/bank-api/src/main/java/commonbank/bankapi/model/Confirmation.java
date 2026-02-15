package commonbank.bankapi.model;

import com.owlike.genson.annotation.JsonProperty;

import java.util.Date;
import java.util.Map;

public final class Confirmation {

    @JsonProperty("refNumber")
    private String refNumber;

    @JsonProperty("amount")
    private long amount;

    @JsonProperty("transferDate")
    private Date transferDate;

    @JsonProperty("fromIBAN")
    private String fromIBAN;

    @JsonProperty("toIBAN")
    private String toIBAN;

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
}