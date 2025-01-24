package com.blockchainbiz.erc20.utils;

import com.blockchainbiz.erc20.model.Confirmation;
import com.blockchainbiz.erc20.model.ConfirmationWithHash;
import com.blockchainbiz.erc20.model.VerificationResult;
import com.owlike.genson.Genson;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class ConfirmationVerifier {
    private static final PublicKey publicKey;

    static{
        publicKey = loadPublicKey("public_key.pem");
    }
    private ConfirmationVerifier() {
    }
    /**
     * Loads an RSA public key from a resource file (e.g., "public_key.pem") packaged
     * within the application's resources.
     *
     * @param resourcePath The path to the resource containing the PEM-encoded public key.
     *                     For example: "public_key.pem".
     * @return The loaded {@link PublicKey} object.
     * @throws RuntimeException If the key file is not found or can't be parsed.
     */
    private static PublicKey loadPublicKey(String resourcePath) {
        try (InputStream inputStream = ConfirmationVerifier.class.getResourceAsStream("/" + resourcePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Public key resource not found: " + resourcePath);
            }
            byte[] keyBytes = inputStream.readAllBytes();
            String publicKeyPEM = new String(keyBytes, StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decodedKey = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
            throw new RuntimeException("Failed to load organization public key: " + e.getMessage(), e);
        }
    }


    /**
     * Serializes the given object to JSON using Genson, and returns the resulting
     * JSON bytes. Suitable for hashing or other processing.
     *
     * @param obj The object to serialize.
     * @return A byte array containing the JSON representation of the object.
     */
    public static byte[] marshalBytes(final Object obj) {
        String json = new Genson().serialize(obj);
        System.out.println("Serialized JSON (Java): " + json);
        return json.getBytes(UTF_8);
    }
    public static String marshalString(final Object obj) {
        String json = new Genson().serialize(obj);
        System.out.println("Serialized JSON (Java): " + json);
        return json;
    }
    public static <T> T unmarshalString(String json, Class<T> clazz) {
        return new Genson().deserialize(json, clazz);
    }

    /**
     * Generates a raw SHA-256 hash (in hex-encoded form) for the given data.
     *
     * @param data The data to be hashed.
     * @return A hex string representing the SHA-256 hash.
     * @throws RuntimeException If the SHA-256 algorithm is not available.
     */
    public static String generateSHA256Hash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating SHA-256 hash", e);
        }
    }

    /**
     * Converts a byte array into a lowercase hexadecimal string.
     *
     * @param bytes The byte array to convert.
     * @return A hex-encoded string (lowercase).
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }


    /**
     * Reads a JSON file from disk, parses it into a generic Map, extracts the "data" portion
     * as a {@link Confirmation} object, and returns this confirmation.
     *
     * @param filePath The path to the JSON file on disk.
     * @return The constructed {@link Confirmation} object from the "data" field within the file.
     * @throws RuntimeException If the JSON file cannot be read or parsed.
     */
    public static Confirmation loadConfirmationFromJson(String filePath) {
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)), UTF_8);
            System.out.println("Loaded JSON: " + jsonContent);

            Genson genson = new Genson();
            Map<String, Object> jsonMap = genson.deserialize(jsonContent, Map.class);

            Map<String, Object> data = (Map<String, Object>) jsonMap.get("data");

            String serializedData = genson.serialize(data);
            Confirmation confirmation = genson.deserialize(serializedData, Confirmation.class);

            System.out.println("Confirmation loaded: " + confirmation);
            return confirmation;
        } catch (IOException e) {
            throw new RuntimeException("Error reading JSON file: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies a signature using the "SHA256withRSA" method (RSASSA-PKCS1-v1_5).
     *
     * @param signedHashBase64 the Base64-encoded signature generated in Python
     * @param confirmation     the Confirmation object whose data was signed
     * @return {@code true} if the signature is valid; {@code false} otherwise
     * @throws RuntimeException if an error occurs during signature verification
     */
    public static boolean verifySignature(String signedHashBase64, Confirmation confirmation) {
        try {
            byte[] signatureBytes = Base64.getDecoder().decode(signedHashBase64);

            byte[] dataToVerify = marshalBytes(confirmation);
            String localHash = generateSHA256Hash(dataToVerify);
            byte [] transactionHashBytes = localHash.getBytes();

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(transactionHashBytes);
            boolean isValid = signature.verify(signatureBytes);

            System.out.println("Signature verification result: " + isValid);
            return isValid;
        } catch (Exception e) {
            throw new RuntimeException("Error verifying signature: " + e.getMessage(), e);
        }
    }
    /**
     * Verifies the signature from a JSON file using the "SHA256withRSA" method (RSASSA-PKCS1-v1_5).
     * <p>
     * A wrapper for {@link #verifySignature(String, Confirmation)}, which loads data from a JSON file
     * using the {@link #loadConfirmationWithHash(String)} method.
     * </p>
     *
     * @param jsonContent JSON content containing the signature and data for verification
     * @return {@code true} if the signature is valid; {@code false} otherwise
     * @throws IllegalArgumentException if the JSON file does not exist or contains invalid data
     * @see #verifySignature(String, Confirmation)
     * @see #loadConfirmationWithHash(String)
     */
    public boolean verifySignatureFromJson(String jsonContent){
        ConfirmationWithHash tuple = loadConfirmationWithHash(jsonContent);
        return verifySignature(tuple.getHash(), tuple.getConfirmation());
    }

    /**
     * Decrypts (using RSA/ECB/PKCS1Padding) the given Base64-encoded hash and compares it
     * to the locally computed SHA-256 hash of the {@link Confirmation} data. Often used
     * as an additional check to ensure the integrity of the confirmation.
     *
     * @param signedHashBase64 The Base64-encoded, RSA-encrypted hash.
     * @param confirmation     The {@link Confirmation} object whose data should match
     *                         the decrypted hash.
     * @return {@code true} if the decrypted hash equals the locally computed SHA-256 hash;
     *         {@code false} otherwise.
     * @throws RuntimeException If decryption or hash comparison fails.
     */
    public static VerificationResult verifyHash(String signedHashBase64, Confirmation confirmation) {
        try {
            byte[] encryptedHash = Base64.getDecoder().decode(signedHashBase64);

            byte[] dataToVerify = marshalBytes(confirmation);
            String localHash = generateSHA256Hash(dataToVerify);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] decryptedHash = cipher.doFinal(encryptedHash);
            String decryptedHashString = new String(decryptedHash, StandardCharsets.UTF_8);
            System.out.println("localHash (hex): " + localHash);
            System.out.println("Decrypted hash (hex): " + decryptedHashString);

            boolean hashesMatch = decryptedHashString.equals(localHash);
            System.out.println("Hashes match: " + hashesMatch);
            VerificationResult result = new VerificationResult(hashesMatch, signedHashBase64, localHash, confirmation);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error verifying hash: " + e.getMessage(), e);
        }
    }
    /**
     * Verifies the hash from a JSON file.
     * <p>
     * A wrapper for {@link ConfirmationVerifier#verifyHash(String, Confirmation)}, it calls {@link #loadConfirmationWithHash(String)}
     * to retrieve the required parameters for hash verification.
     * </p>
     *
     * @param jsonContent the path to the JSON file containing the data for verification
     * @return {@code true} if the hash is valid; {@code false} otherwise
     * @throws IllegalArgumentException if the JSON file does not exist or contains invalid data
     * @see ConfirmationVerifier#verifyHash(String, Confirmation)
     * @see #loadConfirmationWithHash(String)
     */

    public static VerificationResult verifyHashFromJson(String jsonContent) {
        ConfirmationWithHash tuple = loadConfirmationWithHash(jsonContent);
        // Zweryfikuj hash
        return verifyHash(tuple.getHash(), tuple.getConfirmation());
    }

    /**
     * Reads a JSON file from disk, parses it to find the "hash" (Base64-encoded) and the "data"
     * portion (serialized into a {@link Confirmation}), then returns both as a
     * {@link ConfirmationWithHash} object.
     *
     * @param jsonContent Json file content.
     * @return A {@link ConfirmationWithHash} containing both the parsed confirmation data
     *         and the extracted Base64-encoded hash.
     * @throws RuntimeException If any error occurs while reading or parsing the file.
     */
    public static ConfirmationWithHash loadConfirmationWithHash(String jsonContent) {
        try {
            Genson genson = new Genson();

            Map<String, Object> jsonMap = genson.deserialize(jsonContent, Map.class);

            String signedHashBase64 = (String) jsonMap.get("hash");
            Map<String, Object> data = (Map<String, Object>) jsonMap.get("data");

            String serializedData = genson.serialize(data);
            Confirmation confirmation = genson.deserialize(serializedData, Confirmation.class);

            return new ConfirmationWithHash(confirmation, signedHashBase64);

        } catch (Exception e) {
            throw new RuntimeException("Error loading Confirmation and hash from file", e);
        }
    }



}
