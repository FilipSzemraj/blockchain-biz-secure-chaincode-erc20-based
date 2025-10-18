package commonbank.bankapi.service;

import commonbank.bankapi.model.Confirmation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransactionService {
    private final Encrypter encrypter;
    private final Map<String, Confirmation> transactions = new ConcurrentHashMap<>();

    @Autowired
    public TransactionService(Encrypter encrypter){
        this.encrypter=encrypter;
    }

    public void addTransaction(String transaction) {
        Confirmation confirmation = SerializationHandler.loadConfirmationFromJson(transaction);
        System.out.println("Confirmation from addTransaction SERVICE " + confirmation.toString());
        System.out.println("Confirmation getRefNumber " + confirmation.getRefNumber());
        transactions.put(confirmation.getRefNumber(), confirmation);
    }
    public String getTransactionById(String id){
       Confirmation confirmation = transactions.get(id);
       if(confirmation == null) {
        throw new IllegalArgumentException("Transaction of id "+ id + " doesn't exists");
       }
       return encrypter.createJsonFileWithEncryptedTransactionData(confirmation);
    }

    public String getAllTransactionsSerialized() {
        List<String> serializedTransactions = new ArrayList<>();
        for(Confirmation confirmation : transactions.values()){
            String actualConfirmationWithHash = encrypter.createJsonFileWithEncryptedTransactionData(confirmation);
            serializedTransactions.add(actualConfirmationWithHash);
        }

        return SerializationHandler.marshalString(serializedTransactions);
    }
}
