package commonbank.bankapi.controller;

import commonbank.bankapi.model.Confirmation;
import commonbank.bankapi.service.TransactionService;
import commonbank.bankapi.service.ValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransferController {
    private final TransactionService transactionService;
    private final ValidationService validationService;

    public TransferController(TransactionService transactionService, ValidationService validationService){
        this.transactionService = transactionService;
        this.validationService = validationService;
    }

    @PostMapping("")
    public ResponseEntity<String> addTransaction(@RequestBody String serializedConfirmation){
        System.out.println("Received request for addTransaction");
        try{
            //System.out.println("Received JSON: " + serializedConfirmation);
            transactionService.addTransaction(serializedConfirmation);
            return ResponseEntity.status(HttpStatus.CREATED).body("Transaction added succesfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add transaction: " + e.getMessage());
        }
    }

    /**
     * Endpoint do pobierania wszystkich transakcji
     * @return lista transakcji w formacie JSON
     */
    @GetMapping
    public ResponseEntity<String> getAllTransactions() {
        try {
            String transactionsSerialized = transactionService.getAllTransactionsSerialized();
            return ResponseEntity.ok(transactionsSerialized);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve transactions: " + e.getMessage());
        }
    }
    @GetMapping("/transactionsById")
    public ResponseEntity<String> getTransactionById(@RequestParam(required = false) String id) {
        System.out.println("Received request for getTransactionById");
        if (id == null || id.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Query parameter 'id' is required. Use the format: /api/transactionsById?id=123ABC");
        }
        try {
            String transactionSerialized = transactionService.getTransactionById(id);
            return ResponseEntity.ok(transactionSerialized);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve transaction " + id + " - " + e.getMessage());
        }
    }

    @PostMapping("/burnRequest")
    public ResponseEntity<String> submitBurnRequest(@RequestBody String serializedConfirmation){
        System.out.println("Received request for paycheck");
        try{
            //System.out.println("Received JSON: " + serializedConfirmation);
            transactionService.addTransaction(serializedConfirmation);
            return ResponseEntity.status(HttpStatus.CREATED).body("Transaction added succesfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add transaction: " + e.getMessage());
        }
    }





}
