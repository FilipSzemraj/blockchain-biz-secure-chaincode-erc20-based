package com.blockchainbiz.erc20.model;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/**
 * Simple holder class that pairs a {@link Confirmation} with a Base64-encoded hash.
 * This is often used when JSON files contain both the hash (e.g., signature)
 * and the data portion of a transaction confirmation.
 */
@DataType
public final class ConfirmationWithHash {

    @Property
    @JsonProperty("confirmation")
    private final Confirmation confirmation;

    @Property
    @JsonProperty("hash")
    private final String hash;

    /**
     * Constructor for the wrapper.
     *
     * @param confirmation The parsed confirmation object.
     * @param hash         The Base64-encoded hash/signature related to this confirmation.
     */
    public ConfirmationWithHash(
            @JsonProperty("confirmation") final Confirmation confirmation,
            @JsonProperty("hash") final String hash) {
        this.confirmation = confirmation;
        this.hash = hash;
    }

    /**
     * @return The {@link Confirmation} object contained in this wrapper.
     */
    public Confirmation getConfirmation() {
        return confirmation;
    }

    /**
     * @return The Base64-encoded hash/signature associated with this confirmation.
     */
    public String getHash() {
        return hash;
    }
}
