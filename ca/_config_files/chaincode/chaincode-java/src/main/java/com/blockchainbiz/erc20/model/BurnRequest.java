package com.blockchainbiz.erc20.model;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.Date;

/**
 * The BurnRequest class represents a request for burning tokens in the system.
 *
 * This class encapsulates all necessary details for processing a burn request, including:
 * - A unique identifier (`id`) to track the request.
 * - The temporary account holding the tokens to prevent double spending.
 * - The recipient account that could be involved in related operations.
 * - The amount of tokens to be burned.
 * - A timestamp indicating when the request was created.
 *
 * Instances of this class are serialized into JSON for storage in the blockchain's world state,
 * ensuring compatibility with Hyperledger Fabric and allowing for auditable, tamper-resistant records.
 *
 * Usage:
 * - Create an instance with all required details.
 * - Serialize to JSON for storage or transmission.
 * - Deserialize to reconstruct the object for processing or validation.
 *
 * Example:
 * <pre>
 * {@code
 * BurnRequest burnRequest = new BurnRequest("unique-id", "temp-account", "recipient-account", 100, new Date());
 * String json = MarshalUtils.marshalString(burnRequest);
 * System.out.println("Serialized BurnRequest: " + json);
 * }
 * </pre>
 */
@DataType()
public final class BurnRequest {

    @Property()
    @JsonProperty("id")
    private String id;

    //
    @Property()
    @JsonProperty("burnWallet")
    private String burnWallet;

    @Property()
    @JsonProperty("recipientAccount")
    private String recipientAccount;

    @Property()
    @JsonProperty("amount")
    private Long amount;

    @Property()
    @JsonProperty("timestamp")
    private Date timestamp;

    /** Default constructor */
    public BurnRequest() {
        super();
    }

    /**
     * Constructor for BurnRequest
     *
     * @param id Unique txId from the burn request transaction.
     * @param tempAccount Temporary account holding the tokens.
     * @param recipientAccount Recipient account for withdrawal.
     * @param amount Number of tokens being burned.
     * @param timestamp Time of request creation.
     */
    public BurnRequest(
            @JsonProperty("id") final String id,
            @JsonProperty("burnWallet") final String burnWallet,
            @JsonProperty("recipientAccount") final String recipientAccount,
            @JsonProperty("amount") final Long amount,
            @JsonProperty("timestamp") final Date timestamp) {
        super();
        this.id = id;
        this.burnWallet = burnWallet;
        this.recipientAccount = recipientAccount;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getBurnWallet() {
        return burnWallet;
    }

    public String getRecipientAccount() {
        return recipientAccount;
    }

    public Long getAmount() {
        return amount;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}

/*
if(!result.isMatches()){
      throw new ChaincodeException("Unauthorized: Hash of transaction data isn't equal to the encrypted one", UNAUTHORIZED_SENDER.toString());
    }
 */