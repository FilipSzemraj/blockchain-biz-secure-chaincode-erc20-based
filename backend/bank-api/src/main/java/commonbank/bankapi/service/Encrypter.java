package commonbank.bankapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import commonbank.bankapi.model.Confirmation;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;


@Service
public class Encrypter {

    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public Encrypter(
            @Value("${key.public.path}") String publicKeyPath,
            @Value("${key.private.path}") String privateKeyPath
    ){
        PublicKey publicKey = null;
        PrivateKey privateKey = null;
        try {
            publicKey = loadPublicKey(publicKeyPath);
            privateKey = loadPrivateKey(privateKeyPath);
        } catch (Exception e) {
            System.err.println("Klucze nie istnieją lub są uszkodzone. Tworzona nowa para kluczy.");
            KeyPair keyPair = generateKeyPairAndSave(publicKeyPath, privateKeyPath);
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
        } finally {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }
    }
    /**
     * Tworzenie gotowego do wysłania pliku zawierającego dane transakcji jak i ich podpis.
     *
     */
    public String createJsonFileWithSignedTransactionData(Confirmation cnf){
        byte[] serializedData = SerializationHandler.marshal(cnf);
        String hash = generateSHA256Hash(serializedData);

        System.out.println("SHA-256 Hash: " + hash);

        String signature = signTransaction(hash);
        System.out.println("Digital Signature: " + signature);

        return SerializationHandler.returnSerializedString(cnf, signature, hash);
    }
    public String createJsonFileWithEncryptedTransactionData(Confirmation cnf){
        byte[] serializedData = SerializationHandler.marshal(cnf);
        String hash = generateSHA256Hash(serializedData);

        System.out.println("SHA-256 Hash: " + hash);

        String encryptedHash = encrypt(hash);
        System.out.println("Encrypted hash: " + encryptedHash);

        return SerializationHandler.returnSerializedString(cnf, encryptedHash, hash);
    }
    public static KeyPair generateKeyPairAndSave(String publicKeyPath, String privateKeyPath) {
        try{
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048); // Rozmiar klucza
            KeyPair keyPair = keyGen.generateKeyPair();
            File publicKey = new File(publicKeyPath);
            File privateKey = new File(privateKeyPath);
            writeKeyToPem(keyPair, publicKey, privateKey);
            return keyPair;
        }catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    public static PublicKey loadPublicKey(String filePath) throws Exception {
        String key = new String(Files.readAllBytes(Paths.get(filePath)))
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }
    public static PrivateKey loadPrivateKey(String filePath) throws Exception {
        String key = new String(Files.readAllBytes(Paths.get(filePath)))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    public static void writeKeyToPem(KeyPair kp, File publicKey, File privateKey){
        byte[] privateKeyBytes = kp.getPrivate().getEncoded();
        byte[] publicKeyBytes = kp.getPublic().getEncoded();

        String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(privateKeyBytes) +
                "\n-----END PRIVATE KEY-----\n";

        String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(publicKeyBytes) +
                "\n-----END PUBLIC KEY-----\n";

        try(FileWriter writerPr = new FileWriter(privateKey);
            FileWriter writerPu = new FileWriter(publicKey))
        {
            writerPr.write(privateKeyPem);
            writerPu.write(publicKeyPem);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    //----------------------------------- Sekcja hashowania

    /**
     * Konwersja bajtów do zapisu heksadecymalnego
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
     * Metoda pomocnicza: obliczanie "gołego" hash SHA-256 dla podanych bajtów
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

    public String signTransaction(String transactionData) {
        try {
            byte[] transactionBytes = transactionData.getBytes();

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(transactionBytes);
            byte[] digitalSignature = signature.sign();

            return Base64.getEncoder().encodeToString(digitalSignature);
        } catch (Exception e) {
            throw new RuntimeException("Error signing transaction", e);
        }
    }

    public boolean verifyTransaction(String transactionData, String signatureBase64) {
        try {
            byte[] digitalSignature = Base64.getDecoder().decode(signatureBase64);

            byte[] transactionBytes = transactionData.getBytes();

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(transactionBytes);

            return signature.verify(digitalSignature);
        } catch (Exception e) {
            throw new RuntimeException("Error verifying transaction", e);
        }
    }

    public String encrypt(String message) {
        try {
            byte[] messageToBytes = message.getBytes();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] encryptedBytes = cipher.doFinal(messageToBytes);
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred during encryption", e);
        }
    }

    public String decrypt(String encryptedMessage) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMessage);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
            return new String(decryptedMessage, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred during decryption", e);
        }
    }

}
