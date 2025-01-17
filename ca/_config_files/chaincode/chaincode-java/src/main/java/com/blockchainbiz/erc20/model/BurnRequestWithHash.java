package com.blockchainbiz.erc20.model;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/**
 * Simple holder class that pairs a {@link BurnRequest} with a Base64-encoded hash.
 * This is useful for storing and verifying both the hash (e.g., signature)
 * and the data portion of a burn request in the blockchain world state.
 */
@DataType
public final class BurnRequestWithHash {

    @Property
    @JsonProperty("burnRequest")
    private final BurnRequest burnRequest;

    @Property
    @JsonProperty("hash")
    private final String hash;

    /**
     * Constructor for the wrapper.
     *
     * @param burnRequest The parsed BurnRequest object.
     * @param hash        The Base64-encoded hash associated with this burn request.
     */
    public BurnRequestWithHash(
            @JsonProperty("burnRequest") final BurnRequest burnRequest,
            @JsonProperty("hash") final String hash) {
        this.burnRequest = burnRequest;
        this.hash = hash;
    }

    /**
     * @return The {@link BurnRequest} object contained in this wrapper.
     */
    public BurnRequest getBurnRequest() {
        return burnRequest;
    }

    /**
     * @return The Base64-encoded hash associated with this burn request.
     */
    public String getHash() {
        return hash;
    }
}
