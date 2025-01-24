package com.blockchainbiz.erc20;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.HashMap;
import java.util.Map;

import static com.blockchainbiz.erc20.ContractConstants.IBAN_KEY;
import static com.blockchainbiz.erc20.ContractConstants.VOTES_KEY_PREFIX;
import static java.nio.charset.StandardCharsets.UTF_8;

@Contract(name = "IBANVoteContract")
public class IBANVoteContract {

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getCurrentIBAN(Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        String currentIBAN = stub.getStringState(IBAN_KEY.getValue());
        if (currentIBAN == null || currentIBAN.isEmpty()) {
            throw new ChaincodeException("Current IBAN not set.");
        }
        return currentIBAN;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void proposeIBAN(Context ctx, String proposedIBAN) {
        ChaincodeStub stub = ctx.getStub();
        String identity = ctx.getClientIdentity().getId();
        String voteKey = VOTES_KEY_PREFIX.getValue() + proposedIBAN;

        // Sprawdź, czy użytkownik już głosował na ten IBAN
        if (stub.getStringState(voteKey + "_" + identity) != null) {
            throw new ChaincodeException("User has already voted for this IBAN.");
        }

        // Zarejestruj głos
        stub.putStringState(voteKey + "_" + identity, "voted");

        // Zlicz głosy
        String currentVotes = stub.getStringState(voteKey);
        int voteCount = currentVotes == null ? 0 : Integer.parseInt(currentVotes);
        voteCount++;
        stub.putStringState(voteKey, Integer.toString(voteCount));

        // Jeśli osiągnięto większość, zmień IBAN
        int requiredVotes = 3; // Przykładowa liczba głosów potrzebnych do zmiany
        if (voteCount >= requiredVotes) {
            stub.putStringState(IBAN_KEY.getValue(), proposedIBAN);
            clearVotes(stub, proposedIBAN);
        }
    }
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void Init(Context ctx, String initialIBAN) {
        ChaincodeStub stub = ctx.getStub();

        // Sprawdź, czy IBAN już istnieje
        if (stub.getStringState(IBAN_KEY.getValue()) != null && !stub.getStringState(IBAN_KEY.getValue()).isEmpty()) {
            throw new ChaincodeException("IBAN is already initialized.");
        }

        // Ustaw początkowy IBAN
        stub.putStringState(IBAN_KEY.getValue(), initialIBAN);

        String policyIBAN = "AND('YachtSales.member', 'FurnituresMakers.member', 'WoodSupply.member')";
        stub.setStateValidationParameter(IBAN_KEY.getValue(), policyIBAN.getBytes(UTF_8));
    }

    private void clearVotes(ChaincodeStub stub, String proposedIBAN) {
        String voteKey = VOTES_KEY_PREFIX.getValue() + proposedIBAN;
        stub.delState(voteKey);
    }
}
