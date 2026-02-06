package com.blockchainbiz.app.client_api.service;

import com.blockchainbiz.app.client_api.exception.CustomException;
import com.blockchainbiz.app.client_api.model.Certificate;
import com.blockchainbiz.app.client_api.model.CertificateDetails;
import com.blockchainbiz.app.client_api.model.User;
import com.blockchainbiz.app.client_api.repository.CertificateRepository;
import com.blockchainbiz.app.client_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;

    public Certificate addCertificate(Long userId, String receivedCertificate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("The user with the given ID does not exist."));

        if (certificateRepository.findByUserId(userId).isPresent()) {
            throw new CustomException("A certificate for this user already exists.");
        }

        Certificate certificate = Certificate.builder()
                .user(user)
                .certificate(receivedCertificate)
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Adding certificate for user: {}", userId);
        log.debug("Certificate content: {}", certificate.getCertificate());

        return certificateRepository.save(certificate);
    }

    public Certificate getCertificateByUserId(Long userId) {
        return certificateRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("A certificate does not exist for this user."));
    }

    private PublicKey extractPublicKeyFromCertificate(String certificatePem) {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream certInputStream = new ByteArrayInputStream(certificatePem.getBytes(StandardCharsets.UTF_8));
            java.security.cert.Certificate cert = certFactory.generateCertificate(certInputStream);
            return cert.getPublicKey();
        } catch (Exception e) {
            throw new CustomException("Error processing certificate.", e);
        }
    }

    public boolean verifyMessage(Long userId, String message, String signature) {
        Certificate certificate = getCertificateByUserId(userId);

        // Ekstrahuj klucz publiczny z certyfikatu PEM
        String certificatePem = certificate.getCertificate();
        PublicKey publicKey = extractPublicKeyFromCertificate(certificatePem);

        // Weryfikuj podpis
        return verifySignature(publicKey, message, signature);
    }

    private boolean verifySignature(PublicKey publicKey, String message, String signature) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA"); // Algorytm zależny od użytego klucza
            sig.initVerify(publicKey);
            sig.update(message.getBytes(StandardCharsets.UTF_8));
            return sig.verify(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            throw new CustomException("Error during signature verification.", e);
        }
    }

    public void deleteCertificate(Long userId) {
        Certificate certificate = certificateRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("A certificate does not exist for this user."));

        certificateRepository.delete(certificate);

        log.info("Certificate for user {} has been deleted.", userId);
    }

    public CertificateDetails extractCertificateDetails(String certificatePem) {
        try {
            log.info("Raw certificate: " + certificatePem);

            // Reformat the PEM string
            certificatePem = formatPemCertificate(certificatePem);
            log.info("Formatted certificate: " + certificatePem);

            // Convert PEM to X509Certificate
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream certInputStream = new ByteArrayInputStream(certificatePem.getBytes(StandardCharsets.UTF_8));
            X509Certificate x509Certificate = (X509Certificate) certFactory.generateCertificate(certInputStream);

            // Extract Issuer
            String issuer = x509Certificate.getIssuerX500Principal().getName();

            // Extract Validity
            String validFrom = x509Certificate.getNotBefore().toString();
            String validTo = x509Certificate.getNotAfter().toString();

            // Extract Subject
            String subject = x509Certificate.getSubjectX500Principal().getName();

            // Extract SAN (Subject Alternative Name)
            List<String> sanList = extractSAN(x509Certificate);

            // Extract Custom Extension (hfIban)
            String hfIban = extractHfIban(x509Certificate);

            // Return details
            return new CertificateDetails(issuer, validFrom, validTo, subject, sanList, hfIban);
        } catch (Exception e) {
            throw new CustomException("Error extracting certificate details", e);
        }
    }

    private List<String> extractSAN(X509Certificate x509Certificate) {
        try {
            Collection<List<?>> sanCollection = x509Certificate.getSubjectAlternativeNames();
            if (sanCollection == null) {
                return List.of();
            }
            return sanCollection.stream()
                    .filter(san -> san.size() > 1) // Ensure correct structure
                    .map(san -> san.get(1).toString()) // Extract SAN values
                    .toList();
        } catch (Exception e) {
            throw new CustomException("Error extracting Subject Alternative Names (SAN)", e);
        }
    }

    private String extractJsonFromBraces(String input) {
        int start = input.indexOf('{');
        int end = input.lastIndexOf('}');
        if (start != -1 && end != -1 && start < end) {
            return input.substring(start, end + 1).trim(); // Wytnij zawartość pomiędzy klamrami
        }
        throw new CustomException("Invalid JSON structure: missing braces");
    }

    private String extractHfIban(X509Certificate x509Certificate) {
        try {
            byte[] extensionValue = x509Certificate.getExtensionValue("1.2.3.4.5.6.7.8.1");
            if (extensionValue != null) {
                // Dekoduj wartość ASN.1
                String decodedValue = new String(extensionValue, StandardCharsets.UTF_8);
                // Usuń znaki spoza klamr {}
                String cleanedValue = extractJsonFromBraces(decodedValue);
                return cleanedValue;
            }
            return null;
        } catch (Exception e) {
            throw new CustomException("Error extracting hfIban", e);
        }
    }

    private String formatPemCertificate(String pem) {
        String header = "-----BEGIN CERTIFICATE-----";
        String footer = "-----END CERTIFICATE-----";

        // Remove existing line breaks and ensure the header/footer are intact
        pem = pem.replace("\n", "").replace("\r", "").replace(header, "").replace(footer, "");

        // Insert line breaks every 64 characters
        StringBuilder formatted = new StringBuilder(header + "\n");
        int index = 0;
        while (index < pem.length()) {
            formatted.append(pem, index, Math.min(index + 64, pem.length())).append("\n");
            index += 64;
        }
        formatted.append(footer);
        return formatted.toString();
    }
}