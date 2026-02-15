package com.blockchainbiz.app.network_api.controller;

import com.blockchainbiz.app.network_api.client.BankApiClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bank")
public class BankController {

    private final BankApiClient bankApiClient;

    public BankController(BankApiClient bankApiClient) {
        this.bankApiClient = bankApiClient;
    }

    @PostMapping("/addTransaction")
    public ResponseEntity<String> addTransaction(@RequestBody String transactionData) {
        try {
            return bankApiClient.addTransaction(transactionData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error while adding transaction: " + e.getMessage());
        }
    }

    @GetMapping("/getAllTransactions")
    public ResponseEntity<String> getAllTransactions() {
        try {
            return bankApiClient.getAllTransactions();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error while retrieving transactions: " + e.getMessage());
        }
    }

    @GetMapping("/getTransactionById")
    public ResponseEntity<String> getTransactionById(@RequestParam String id) {
        try {
            return bankApiClient.getTransactionById(id);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error while retrieving transaction by ID: " + e.getMessage());
        }
    }
}
