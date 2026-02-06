package com.blockchainbiz.app.client_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CertificateDetails {
    private String issuer;
    private String validFrom;
    private String validTo;
    private String subject;
    private List<String> subjectAlternativeNames;
    private String hfIban;
}