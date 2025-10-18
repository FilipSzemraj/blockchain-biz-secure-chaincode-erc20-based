package com.blockchainbiz.app.network_api.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BankApiClient {

    private final RestTemplate restTemplate;

    private final String bankApiBaseUrl = "http://bankapi:8081/api/transactions";

    public BankApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> addTransaction(String transactionData) {
        return restTemplate.postForEntity(bankApiBaseUrl, transactionData, String.class);
    }

    public ResponseEntity<String> getAllTransactions() {
        return restTemplate.getForEntity(bankApiBaseUrl, String.class);
    }

    public ResponseEntity<String> getTransactionById(String id) {
        String url = bankApiBaseUrl + "/transactionsById?id=" + id;
        return restTemplate.getForEntity(url, String.class);
    }
}
