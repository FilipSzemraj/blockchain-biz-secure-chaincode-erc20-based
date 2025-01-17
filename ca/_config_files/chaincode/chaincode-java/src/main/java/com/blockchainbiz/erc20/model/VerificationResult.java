package com.blockchainbiz.erc20.model;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/**
 * Represents the result of a hash verification process.
 * <p>
 * This class encapsulates the outcome of the verification, including whether the hashes match,
 * the decrypted hash, the locally computed hash, and a reference to the {@link Confirmation}.
 * </p>
 */
@DataType()
public final class VerificationResult {

    /**
     * Indicates whether the decrypted hash matches the locally computed hash.
     */
    @Property()
    @JsonProperty("matches")
    private boolean matches;

    /**
     * The decrypted hash as a string.
     */
    @Property()
    @JsonProperty("decryptedHash")
    private String decryptedHash;

    /**
     * The locally computed hash as a string.
     */
    @Property()
    @JsonProperty("localHash")
    private String localHash;

    /**
     * The {@link Confirmation} object associated with this verification result.
     */
    @Property()
    @JsonProperty("confirmation")
    private Confirmation confirmation;

    /**
     * Default constructor required for serialization/deserialization.
     */
    public VerificationResult() {
        super();
    }

    /**
     * Constructs a new {@link VerificationResult} with the specified details.
     *
     * @param matches       {@code true} if the hashes match; {@code false} otherwise.
     * @param decryptedHash The decrypted hash as a string.
     * @param localHash     The locally computed hash as a string.
     * @param confirmation  The associated {@link Confirmation} object.
     */
    public VerificationResult(
            @JsonProperty("matches") final boolean matches,
            @JsonProperty("decryptedHash") final String decryptedHash,
            @JsonProperty("localHash") final String localHash,
            @JsonProperty("confirmation") final Confirmation confirmation) {
        super();
        this.matches = matches;
        this.decryptedHash = decryptedHash;
        this.localHash = localHash;
        this.confirmation = confirmation;
    }

    /**
     * Indicates whether the decrypted hash matches the locally computed hash.
     *
     * @return {@code true} if the hashes match; {@code false} otherwise.
     */
    public boolean isMatches() {
        return matches;
    }

    /**
     * Returns the decrypted hash as a string.
     *
     * @return The decrypted hash as a string.
     */
    public String getDecryptedHash() {
        return decryptedHash;
    }

    /**
     * Returns the locally computed hash as a string.
     *
     * @return The locally computed hash as a string.
     */
    public String getLocalHash() {
        return localHash;
    }

    /**
     * Returns the {@link Confirmation} object associated with this verification result.
     *
     * @return The associated {@link Confirmation} object.
     */
    public Confirmation getConfirmation() {
        return confirmation;
    }

    /**
     * Returns a string representation of this {@link VerificationResult}.
     *
     * @return A string representation of the result, including whether the hashes match,
     *         the decrypted hash, and the locally computed hash.
     */
    @Override
    public String toString() {
        return "VerificationResult{" +
                "matches=" + matches +
                ", decryptedHash='" + decryptedHash + '\'' +
                ", localHash='" + localHash + '\'' +
                ", confirmation=" + (confirmation != null ? confirmation.toString() : "null") +
                '}';
    }
}
