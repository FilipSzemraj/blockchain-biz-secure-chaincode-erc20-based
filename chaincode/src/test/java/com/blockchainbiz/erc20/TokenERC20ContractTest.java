/*
 * SPDX-License-Identifier: Apache-2.0
 */
package com.blockchainbiz.erc20;

import com.blockchainbiz.erc20.model.*;
import com.blockchainbiz.erc20.utils.ConfirmationVerifier;
import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

import static com.blockchainbiz.erc20.ContractConstants.*;
import static com.blockchainbiz.erc20.ContractErrors.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.protobuf.Timestamp;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TokenERC20ContractTest {

  private final String org1UserId =
      "x509::CN=admin, L=Kielce, ST=Swietokrzyskie,"
          + " C=PL::CN=yachtsales-ca, O=YachtSales, L=Kielce, ST=Swietokrzyskie, C=PL";

  String hash="19b096bddb45d9aa08fa3c2b883598e4f64476351e4c112a222c70b45ff0993f";

  private String jsonContent = "{\"data\":{\"amount\":1000,\"fromIBAN\":\"PL0000000000000000000000000\",\"rawData\":{\"additionalInfo\":\"some info\",\"currency\":\"EUR\"},\"refNumber\":\"123ABC\",\"toIBAN\":\"PL9999999999999999999999999\",\"transferDate\":1737469632152},\"hash\":\"k49jWhVj6+QMf6AGAxmNV986o5l2efzahJCZq3vrqGTndeCUiQSKac3tjWfPbIWvUs8EQINIvnU/XNXRYsF0GtgTcaUUj+eASB4qZDo6ImE2dQIXegL5QUFDFi3nX9FY0mneRlin52JVSVguzRyBXSCrftBMhWwWFWumZoglN1RdxkEhMoUo9ZkviMrQU1Q6qseeDnhlMxevbGozbUB8hJ+hxw6iHNe6WJ+VwZW59wyDCxKojACXQVlzJaiykbgHMy5vkZrY2ilMRoakh+v7iYcuGRPwxI1Zajj6dBD53GIpxG9kBcnodVAZ/CdHUG6eUCwtYDVX+EcEtIPAIIoc8A==\"}";
  private final String falseJsonContent = "{\"data\":{\"amount\":1100,\"fromIBAN\":\"PL0000000000000000000000000\",\"rawData\":{\"additionalInfo\":\"some extra info\",\"currency\":\"EUR\"},\"refNumber\":\"123ABC\",\"toIBAN\":\"PL9999999999999999999999999\",\"transferDate\":1737469632152},\"hash\":\"k49jWhVj6+QMf6AGAxmNV986o5l2efzahJCZq3vrqGTndeCUiQSKac3tjWfPbIWvUs8EQINIvnU/XNXRYsF0GtgTcaUUj+eASB4qZDo6ImE2dQIXegL5QUFDFi3nX9FY0mneRlin52JVSVguzRyBXSCrftBMhWwWFWumZoglN1RdxkEhMoUo9ZkviMrQU1Q6qseeDnhlMxevbGozbUB8hJ+hxw6iHNe6WJ+VwZW59wyDCxKojACXQVlzJaiykbgHMy5vkZrY2ilMRoakh+v7iYcuGRPwxI1Zajj6dBD53GIpxG9kBcnodVAZ/CdHUG6eUCwtYDVX+EcEtIPAIIoc8A==\"}";

  private final String MSP_ID_ORG1 = ORG1.getValue();

    ERC20TokenContract contract;
    Context ctx;
    ChaincodeStub stub;
    ClientIdentity ci;

  @Nested
  class serializationTest{
      @Test
      public void testJsonDeserialization() {
          ConfirmationWithHash result = ConfirmationVerifier.loadConfirmationWithHash(jsonContent);

          assertNotNull(result);
          assertNotNull(result.getConfirmation());
          assertEquals(1000L, result.getConfirmation().getAmount());
          assertEquals("PL0000000000000000000000000", result.getConfirmation().getFromIBAN());
          assertEquals("EUR", result.getConfirmation().getRawData().get("currency"));
          assertEquals("123ABC", result.getConfirmation().getRefNumber());
          assertEquals("PL9999999999999999999999999", result.getConfirmation().getToIBAN());
          assertEquals("k49jWhVj6+QMf6AGAxmNV986o5l2efzahJCZq3vrqGTndeCUiQSKac3tjWfPbIWvUs8EQINIvnU/XNXRYsF0GtgTcaUUj+eASB4qZDo6ImE2dQIXegL5QUFDFi3nX9FY0mneRlin52JVSVguzRyBXSCrftBMhWwWFWumZoglN1RdxkEhMoUo9ZkviMrQU1Q6qseeDnhlMxevbGozbUB8hJ+hxw6iHNe6WJ+VwZW59wyDCxKojACXQVlzJaiykbgHMy5vkZrY2ilMRoakh+v7iYcuGRPwxI1Zajj6dBD53GIpxG9kBcnodVAZ/CdHUG6eUCwtYDVX+EcEtIPAIIoc8A==", result.getHash());
      }
  }

    @Nested
    class MintFunctionTest {

        Confirmation confirmation;
        VerificationResult verificationResult;

        @BeforeEach
        public void setupMintTest() {
            // 1) Instantiate contract & create mocks
            contract = new ERC20TokenContract();
            ctx = mock(Context.class);
            stub = mock(ChaincodeStub.class);
            ci = mock(ClientIdentity.class);

            // Wire them
            when(ctx.getClientIdentity()).thenReturn(ci);
            when(ctx.getStub()).thenReturn(stub);

            // 2) Set up "testMint_setOrgAndUser"
            when(ci.getMSPID()).thenReturn(MSP_ID_ORG1);
            when(ci.getAttributeValue("role")).thenReturn("admin");
            when(ci.getId()).thenReturn(org1UserId);
            // Default client IBAN from certificate
            when(ci.getAttributeValue("hf.iban")).thenReturn("PL0000000000000000000000000");

            // 3) Set up "testMint_setTokenDetails"
            when(stub.getStringState(NAME_KEY.getValue())).thenReturn("IntrinsicCoin");
            when(stub.getStringState(IBAN_KEY.getValue())).thenReturn("PL9999999999999999999999999");

            // 4) Set up "testMint_setVerificationDetails"
            verificationResult = mock(VerificationResult.class);
            confirmation = mock(Confirmation.class);
            when(verificationResult.isMatches()).thenReturn(true);           // Default = valid hash
            when(verificationResult.getConfirmation()).thenReturn(confirmation);
            when(verificationResult.getLocalHash()).thenReturn(hash);
            // By default, minted amount = 1000
            when(confirmation.getAmount()).thenReturn(1000L);

            // 5) Set up "testMint_setBurnKeyDetails"
            @SuppressWarnings("unchecked")
            QueryResultsIterator<KeyModification> historyMock = mock(QueryResultsIterator.class);
            when(historyMock.iterator()).thenReturn(Collections.emptyIterator());
            when(stub.getHistoryForKey(USED_TRANSACTIONS_PREFIX.getValue() + hash)).thenReturn(historyMock);
            // The chaincode checks if null => not used
            when(stub.getStringState(USED_TRANSACTIONS_PREFIX.getValue() + hash)).thenReturn(null);

            // 6) Set up "testMint_setIBANs"
            // The default from/to IBAN match the certificate’s IBAN & consortium IBAN
            when(confirmation.getFromIBAN()).thenReturn("PL0000000000000000000000000");
            when(confirmation.getToIBAN()).thenReturn("PL9999999999999999999999999");

            // 7) Set up "testMint_setOrgBalance"
            // Example: Current org balance = 5000
            CompositeKey balanceKeyMock = new CompositeKey(BALANCE_PREFIX.getValue(), MSP_ID_ORG1);
            when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), MSP_ID_ORG1)).thenReturn(balanceKeyMock);
            when(stub.getStringState(balanceKeyMock.toString())).thenReturn("5000");

            // 8) Default total supply
            when(stub.getStringState(TOTAL_SUPPLY_KEY.getValue())).thenReturn("10000");
        }

        @Test
        public void testMint_Success() {
            // No extra setup needed; everything is in @BeforeEach

            // when
            contract.Mint(ctx, jsonContent);

            // then - verify ledger updates
            // 1. Organization balance 5000 -> 6000
            verify(stub).putStringState(
                    new CompositeKey(BALANCE_PREFIX.getValue(), MSP_ID_ORG1).toString(),
                    "6000"
            );

            // 2. TotalSupply 10000 -> 11000
            verify(stub).putStringState(TOTAL_SUPPLY_KEY.getValue(), "11000");

            // 3. Mark transaction as used
            verify(stub).putStringState(USED_TRANSACTIONS_PREFIX.getValue() + hash, "used");

            // 4. Fire "Transfer" event
            verify(stub).setEvent(eq(TRANSFER_EVENT.getValue()), any(byte[].class));
        }

        @Test
        public void testMint_UnauthorizedMSP_ThrowsException() {
            // Overwrite the MSP to be something invalid
            when(ci.getMSPID()).thenReturn("UnknownMSP");

            ChaincodeException exception = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Mint(ctx, "someJsonContent")
            );
            assertTrue(exception.getMessage().contains("Client is not authorized to mint new tokens"));
        }

        @Test
        public void testMint_RoleNotAdmin_ThrowsException() {
            when(ci.getMSPID()).thenReturn(MSP_ID_ORG1);
            when(ci.getAttributeValue("role")).thenReturn("user");

            ChaincodeException exception = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Mint(ctx, "someJsonContent")
            );

            assertTrue(exception.getMessage().contains("Unauthorized: Only admin can mint tokens"));
        }

        @Test
        public void testMint_HashNotMatches_ThrowsException() {
            // Let the real chaincode logic detect mismatch for falseJsonContent,
            // or manually override if your chaincode is mocking the verification internally.
            when(verificationResult.isMatches()).thenReturn(false);

            ChaincodeException exception = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Mint(ctx, falseJsonContent)
            );
            assertTrue(exception.getMessage().contains("Hash of transaction data isn't equal"));
        }

        @Test
        public void testMint_TransactionAlreadyUsed_ThrowsException() {
            // Mark it as "used"
            when(stub.getStringState(USED_TRANSACTIONS_PREFIX.getValue() + hash)).thenReturn("used");

            ChaincodeException exception = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Mint(ctx, jsonContent)
            );
            assertTrue(exception.getMessage().contains("Transaction ID %s was already used"));
        }

        @Test
        public void testMint_BurnKeyAlreadyUsed_ThrowsException() {
            // Provide a non-empty history
            @SuppressWarnings("unchecked")
            QueryResultsIterator<KeyModification> historyMock = mock(QueryResultsIterator.class);
            KeyModification mockModification = mock(KeyModification.class);
            when(historyMock.iterator()).thenReturn(Collections.singletonList(mockModification).iterator());
            when(stub.getHistoryForKey(USED_TRANSACTIONS_PREFIX.getValue() + hash)).thenReturn(historyMock);

            ChaincodeException exception = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Mint(ctx, jsonContent)
            );
            assertTrue(exception.getMessage().contains("Burn ID already used in the past"));
        }

        @Test
        public void testMint_ConsortiumIBANNotSet_ThrowsException() {
            // Clear IBAN key => chaincode complains
            when(stub.getStringState(IBAN_KEY.getValue())).thenReturn("");

            ChaincodeException exception = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Mint(ctx, jsonContent)
            );
            assertTrue(exception.getMessage().contains("No IBAN number found for the consortium"));
        }

        @Test
        public void testMint_InvalidDestinationIBAN_ThrowsException() {
            // Mismatch between toIBAN and consortium IBAN
            when(confirmation.getToIBAN()).thenReturn("PL1111111111111111111111111");
            when(stub.getStringState(IBAN_KEY.getValue())).thenReturn("PL0000000000000000000000000");

            ChaincodeException exception = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Mint(ctx, jsonContent)
            );
            assertTrue(exception.getMessage().contains("The provided recipient IBAN does not match the consortium's IBAN"));
        }

        @Test
        public void testMint_InvalidAmount_ThrowsException() {
            final String wrongAmountHash="d0a2c693793dab4193ae8f0cbcf60d7564e4d27c6453930daf48b4d5710b0162";
            final String wrongAmountJsonContent = "{\"data\":{\"amount\":0,\"fromIBAN\":\"PL0000000000000000000000000\",\"rawData\":{\"additionalInfo\":\"some info\",\"currency\":\"EUR\"},\"refNumber\":\"123ABC\",\"toIBAN\":\"PL9999999999999999999999999\",\"transferDate\":1737469632152},\"hash\":\"QaElM0wBlPToOFKuc1DtZJ/wBBJp9xw6NRb/iWujDvb9C34w1fWvcBWgzPUtDJau/LPve1Vl/KZQFflTCI/vvVhan1rrOO2QRHlhK+NSt1QRThwUAOv/wvGBlFOY/EDIPI0WbuivncQ63RkbJrS6WrHbhMW9fW/c7oVKW/L+AyVvFmLK0dR152ABe8sFQaA9gL/TDjVc/Gd2ebOByJhKehKfZspdGlHm6i3HvB1uYDA4RhWG+zsx7jdGY7OIrSqJxs7tLox3MzY6GQ83aHNPdycDumnRmnOiQqOYoZlfZUGFgN0QEJCBjViBdniyK0W3TvMTE2ovgmR0HZsmb/rV3Q==\"}";


            String originalHash = hash;
            String originalJson = jsonContent;
            try {
                // Override the default
                hash = wrongAmountHash;
                jsonContent = wrongAmountJsonContent;

                setupMintTest();

                ChaincodeException exception = assertThrows(
                        ChaincodeException.class,
                        () -> contract.Mint(ctx, jsonContent)
                );
                assertTrue(exception.getMessage().contains("Mint amount must be a positive integer"));
            } finally {
                // Restore
                hash = originalHash;
                jsonContent = originalJson;
            }
        }

        @Test
        public void testMint_ClientIBANMismatch_ThrowsRuntimeException() {
            // Suppose the chaincode checks the certificate IBAN against the fromIBAN
            when(ci.getAttributeValue("hf.iban")).thenReturn("PL1234");

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> contract.Mint(ctx, jsonContent)
            );
            assertTrue(exception.getMessage().contains("Client IBAN not found in certificate or isn't the same"));
        }

        @Test
        public void testMint_NotInitialized_ThrowsException() {
            // Force empty name => checkInitialized(ctx) fails
            when(stub.getStringState(NAME_KEY.getValue())).thenReturn("");

            ChaincodeException exception = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Mint(ctx, jsonContent)
            );
            assertTrue(exception.getMessage().contains("Contract options need to be set before calling any function"));
        }

        @Test
        public void testMint_Overflow_ThrowsArithmeticException() {
            // Very high balance / total supply
            CompositeKey balanceKey = new CompositeKey(BALANCE_PREFIX.getValue(), MSP_ID_ORG1);
            when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), MSP_ID_ORG1)).thenReturn(balanceKey);

            when(stub.getStringState(balanceKey.toString())).thenReturn(String.valueOf(Long.MAX_VALUE));
            when(stub.getStringState(TOTAL_SUPPLY_KEY.getValue())).thenReturn(String.valueOf(Long.MAX_VALUE));

            // Attempt to add 1000 => overflow
            assertThrows(
                    ArithmeticException.class,
                    () -> contract.Mint(ctx, jsonContent)
            );
        }
    }

    @Nested
    class BurnFunctionTest {

        private final String burnAmount = "100";
        private final String orgMSP = MSP_ID_ORG1;
        private final String ibanAttribute = "PL0000000000000000000000000";
        private final String txId = "tx123";

        // Common fields reused by tests
        ERC20TokenContract contract;
        Context ctx;
        ChaincodeStub stub;
        ClientIdentity ci;

        CompositeKey burnBalanceKey;
        CompositeKey burnKey;
        CompositeKey balanceKey;
        String burnRequestHash;
        BurnRequestWithHash combinedRequest;

        @BeforeEach
        public void setupBurnTest() {
            // 1) Create contract & mock objects
            contract = new ERC20TokenContract();
            ctx = mock(Context.class);
            stub = mock(ChaincodeStub.class);
            ci = mock(ClientIdentity.class);

            // 2) Wire them together
            when(ctx.getClientIdentity()).thenReturn(ci);
            when(ctx.getStub()).thenReturn(stub);

            // 3) Basic identity info
            when(ci.getMSPID()).thenReturn(orgMSP);      // Must be an allowed MSP
            when(ci.getId()).thenReturn(org1UserId);     // Some user ID
            when(ci.getAttributeValue("hf.iban")).thenReturn(ibanAttribute);
            when(ci.getAttributeValue("role")).thenReturn("admin");
            when(stub.getStringState(NAME_KEY.getValue())).thenReturn("IntrinsicCoin");
            when(stub.getTxId()).thenReturn(txId);

            // 4) Mock timestamp (use a normal Instant so no overflow in Date.from(...))
            Instant mockInstant = Instant.ofEpochSecond(1735689600L); // 2025-10-01T00:00:00Z (example)
            when(stub.getTxTimestamp()).thenReturn(mockInstant);

            // 5) Setup CompositeKey for burn-balance wallet
            burnBalanceKey = new CompositeKey(BURN_BALANCE_PREFIX.getValue(), orgMSP);
            when(stub.createCompositeKey(BURN_BALANCE_PREFIX.getValue(), orgMSP))
                    .thenReturn(burnBalanceKey);

            // 6) Construct a BurnRequest (using the default `burnAmount`, but in real tests
            //    you can parameterize or override it in your @Test).
            Date txTimestamp = Date.from(mockInstant);
            BurnRequest burnRequest = new BurnRequest(
                    txId,
                    burnBalanceKey.toString(),
                    ibanAttribute,
                    Long.parseLong(burnAmount),
                    txTimestamp
            );

            // 7) Serialize, hash, and wrap. This is effectively what chaincode does.
            byte[] serializedBurnRequest = ConfirmationVerifier.marshalBytes(burnRequest);
            burnRequestHash = ConfirmationVerifier.generateSHA256Hash(serializedBurnRequest);
            combinedRequest = new BurnRequestWithHash(burnRequest, burnRequestHash);

            // 8) Prevent "duplicate transaction" logic from failing
            //    e.g. wasAlreadyUsed(stub, burnRequestHash) => stub.getStringState("used_" + hash)
            String usedTxKey = USED_TRANSACTIONS_PREFIX.getValue() + burnRequestHash;
            when(stub.getStringState(usedTxKey)).thenReturn("");

            // 9) Setup CompositeKey for the final burn transaction
            burnKey = new CompositeKey(BURN_TRANSACTIONS_PREFIX.getValue(), burnRequestHash);
            when(stub.createCompositeKey(BURN_TRANSACTIONS_PREFIX.getValue(), burnRequestHash))
                    .thenReturn(burnKey);
            when(stub.getStringState(burnKey.toString())).thenReturn("");

            // 10) Mock the org’s current balance, typically "1000"
            balanceKey = new CompositeKey("balance", orgMSP);
            when(stub.createCompositeKey("balance", orgMSP)).thenReturn(balanceKey);
            when(stub.getStringState(balanceKey.toString())).thenReturn("1000");

            // 11) Ensure no ledger history for burnKey
            @SuppressWarnings("unchecked")
            QueryResultsIterator<KeyModification> mockHistory = mock(QueryResultsIterator.class);
            Iterator<KeyModification> emptyIterator = java.util.Collections.emptyIterator();
            when(mockHistory.iterator()).thenReturn(emptyIterator);
            when(stub.getHistoryForKey(burnKey.toString())).thenReturn(mockHistory);
        }

        @Test
        public void testBurn_Success() {
            // 1) Invoke the burn with the default burnAmount ("100")
            String burnResult = contract.Burn(ctx, burnAmount);

            // 2) Assert the returned key string
            assertNotNull(burnResult);
            assertTrue(burnResult.contains(BURN_TRANSACTIONS_PREFIX.getValue()));

            // 3) Verify ledger updates:
            //    - The balance goes from 1000 -> 900
            //    - The burnBalance goes from 0 -> 100
            verify(stub).putStringState(balanceKey.toString(), "900");
            verify(stub).putStringState(burnBalanceKey.toString(), "100");

            // 4) Confirm an event was emitted
            verify(stub).setEvent(eq("BurnPendingEvent"), any(byte[].class));
        }

        /**
         * 2) Invalid amount format -> throws ChaincodeException(INVALID_AMOUNT)
         */
        @Test
        public void testBurn_InvalidAmountFormat() {
            // Try a non-numeric
            String invalidAmount = "abc";

            ChaincodeException thrown = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Burn(ctx, invalidAmount)
            );

            String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);
            assertEquals(INVALID_AMOUNT.toString(), errorCode);
            assertTrue(thrown.getMessage().contains("Invalid amount format"));
        }

        /**
         * 3) Negative or zero amount -> throws INVALID_AMOUNT
         */
        @Test
        public void testBurn_ZeroAmount() {
            ChaincodeException thrown = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Burn(ctx, "0")
            );

            String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);
            assertEquals(INVALID_AMOUNT.toString(), errorCode);
            assertTrue(thrown.getMessage().contains("Burn amount must be a positive integer"));
        }

        @Test
        public void testBurn_NegativeAmount() {
            ChaincodeException thrown = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Burn(ctx, "-50")
            );

            String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);
            assertEquals(INVALID_AMOUNT.toString(), errorCode);
            assertTrue(thrown.getMessage().contains("Burn amount must be a positive integer"));
        }

        /**
         * 4) Unauthorized sender -> MSP not in ORG1, ORG2, ORG3
         */
        @Test
        public void testBurn_UnauthorizedMSP() {
            // Setup some unapproved MSP
            when(ci.getMSPID()).thenReturn("BadOrgMSP");

            ChaincodeException thrown = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Burn(ctx, burnAmount)
            );

            String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);
            assertEquals(UNAUTHORIZED_SENDER.toString(), errorCode);
            assertTrue(thrown.getMessage().contains("Client is not authorized to burn tokens"));
        }

        /**
         * 5) Contract not initialized -> checkInitialized(ctx) fails
         *    Suppose checkInitialized calls: stub.getStringState(NAME_KEY.getValue())
         *    and expects a non-null or non-empty result. We'll mock it empty to force failure.
         */
        @Test
        public void testBurn_ContractNotInitialized() {
            when(stub.getStringState(NAME_KEY.getValue())).thenReturn("");

            ChaincodeException thrown = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Burn(ctx, burnAmount)
            );

            // The chaincode might throw a custom initialization error or reuse a known code
            // If you have a constant, e.g. NOT_INITIALIZED, test that. Otherwise check message.
            assertTrue(thrown.getMessage().contains("Contract options need to be set before calling any function"));
        }

        /**
         * 6) Missing or invalid IBAN
         */
        @Test
        public void testBurn_MissingIBAN() {
            // Mock IBAN to be null
            when(ci.getAttributeValue("hf.iban")).thenReturn(null);

            ChaincodeException thrown = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Burn(ctx, burnAmount)
            );

            String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);

            assertEquals("INVALID_IBAN", errorCode);
            assertTrue(thrown.getMessage().contains("Client IBAN is missing or invalid"));
        }

        /**
         * 7) Insufficient funds -> currentBalance < amount
         */
        @Test
        public void testBurn_InsufficientFunds() {
            // Mock the ledger to have only 50 tokens
            when(stub.getStringState(balanceKey.toString())).thenReturn("50");

            ChaincodeException thrown = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Burn(ctx, burnAmount)
            );

            String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);

            assertEquals(INSUFFICIENT_FUND.toString(), errorCode);
            assertTrue(thrown.getMessage().contains("insufficient funds"));
        }

        /**
         * 8) The balance does not exist in the ledger
         */
        @Test
        public void testBurn_BalanceNotFound() {
            when(stub.getStringState(balanceKey.toString())).thenReturn(null);

            ChaincodeException thrown = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Burn(ctx, burnAmount)
            );

            String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);
            assertEquals(BALANCE_NOT_FOUND.toString(), errorCode);
            assertTrue(thrown.getMessage().contains("The balance does not exist"));
        }

        /**
         * 9) The burnRequestHash was already used -> wasAlreadyUsed(...) returns true
         */
        @Test
        public void testBurn_HashAlreadyUsed() {
            // Suppose wasAlreadyUsed(stub, burnRequestHash) => checks
            //  "USED_TRANSACTIONS_PREFIX + burnRequestHash"
            // Return something non-empty to simulate it's used.
            String usedTxKey = USED_TRANSACTIONS_PREFIX.getValue() + burnRequestHash;
            when(stub.getStringState(usedTxKey)).thenReturn("used");

            ChaincodeException thrown = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Burn(ctx, burnAmount)
            );

            String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);
            assertEquals(DUPLICATE_TRANSACTION_ID.toString(), errorCode);
            assertTrue(thrown.getMessage().contains("Hash of transaction data was already used"));
        }

        /**
         * 10) Burn ID (composite key) already exists in world state
         */
        @Test
        public void testBurn_BurnIDAlreadyExists() {
            // The code checks stub.getStringState(burnKey.toString())
            // If it's non-empty => "Burn ID already exists"
            when(stub.getStringState(burnKey.toString())).thenReturn("existing burn data");

            ChaincodeException thrown = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Burn(ctx, burnAmount)
            );

            String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);
            assertEquals(DUPLICATE_TRANSACTION_ID.toString(), errorCode);
            assertTrue(thrown.getMessage().contains("Burn ID already exists"));
        }

        /**
         * 11) Burn ID found in ledger history -> stub.getHistoryForKey(burnKey.toString())
         *     => return an iterator with at least one entry
         */
        @Test
        public void testBurn_BurnIDFoundInHistory() {
            // We’ll mock an iterator with a single KeyModification
            @SuppressWarnings("unchecked")
            QueryResultsIterator<KeyModification> mockHistory = mock(QueryResultsIterator.class);
            KeyModification mockKeyModification = mock(KeyModification.class);

            // Create an iterator with one item
            Iterator<KeyModification> nonEmptyIterator = Arrays.asList(mockKeyModification).iterator();
            when(mockHistory.iterator()).thenReturn(nonEmptyIterator);

            // Return that from the stub
            when(stub.getHistoryForKey(burnKey.toString())).thenReturn(mockHistory);

            ChaincodeException thrown = assertThrows(
                    ChaincodeException.class,
                    () -> contract.Burn(ctx, burnAmount)
            );

            String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);
            assertEquals(DUPLICATE_TRANSACTION_ID.toString(), errorCode);
            assertTrue(thrown.getMessage().contains("Burn ID already used in the past"));
        }
  }

  @Nested
    class FinalizeBurnTest{
      private String existingBurnJson;
      private final String finalizeBurnJsonContent = "{\"data\":{\"amount\":100,\"fromIBAN\":\"PL9999999999999999999999999\",\"rawData\":{\"BurnRequestHash\":\"5388d615120526d8e28440c9fcf1c59b50d4cbfedf081d081ff7aefd6d0b66f7\",\"currency\":\"EUR\"},\"refNumber\":\"123ABC\",\"toIBAN\":\"PL0000000000000000000000000\",\"transferDate\":1737469632152},\"hash\":\"Suh3NffFV6sKeqFLCbdBh+CJzOWUw/RVt8HWgHTWX2guWoD1BTItLjqsOGQnGQhMw1VaiMU7XJrarKpURpHpNqZK0zZd7T87QOVkmbiSkARvrGDRYrwZSBo9/plx+6cKipQZr5i2qkURz9wngaxkzCiFVR72zMSjVJu/YCVUzOK9jCHwUJbCJrVrG3EdQZKqEGgcMJSaPdwCNAr9ilbHfzkqJ5RpBEH9OsdlBpev1VlgKKXoW5i65R86hc68NGYAQznKuca8Wc6jFs4zfvi5sUqC0pOXMoxMxntIgUD7OaS5F+7gelI647nLKHYNwZxtUuH8DhVC2y0DyfhaZTXdDw==\"}";

      private final String decryptedHash = "d165c54c5fef358b400ca5c78ba6fe2b0e074c066402ab60a182f045a61e471f";
      private final String BURN_REQUEST_HASH="5388d615120526d8e28440c9fcf1c59b50d4cbfedf081d081ff7aefd6d0b66f7";
      @BeforeEach
      public void setupFinalizeBurnTest() {
          // 1) Create contract & mocks
          contract = new ERC20TokenContract();
          ctx = mock(Context.class);
          stub = mock(ChaincodeStub.class);
          ci = mock(ClientIdentity.class);

          // 2) Wire them together
          when(ctx.getClientIdentity()).thenReturn(ci);
          when(ctx.getStub()).thenReturn(stub);

          // 3) Basic identity info
          when(ci.getMSPID()).thenReturn(MSP_ID_ORG1);            // Must be allowed (ORG1/ORG2/ORG3)
          when(ci.getAttributeValue("role")).thenReturn("admin"); // Must be "admin" to finalize burn
          // The chaincode checks that toIBAN == client's IBAN
          when(ci.getAttributeValue("hf.iban")).thenReturn("PL0000000000000000000000000");

          // 4) Ledger states needed
          //    - The chaincode checks the consortium IBAN in world state
          when(stub.getStringState(IBAN_KEY.getValue())).thenReturn("PL9999999999999999999999999");
          //    - The chaincode checks total supply in world state
          when(stub.getStringState(TOTAL_SUPPLY_KEY.getValue())).thenReturn("1000000"); // e.g., 1 million

          // 5) We must have an existing BurnRequest in the ledger:
          //    The chaincode uses: burnTx + BURN_REQUEST_HASH as the key
          CompositeKey burnKeyComposite = new CompositeKey(BURN_TRANSACTIONS_PREFIX.getValue(), BURN_REQUEST_HASH);
          String burnKey = burnKeyComposite.toString();

          when(stub.createCompositeKey(BURN_TRANSACTIONS_PREFIX.getValue(), BURN_REQUEST_HASH)).thenReturn(burnKeyComposite);
          // For a valid record, we store a JSON object that looks like:
          //   {
          //      "burnRequest": {...},
          //      "hash": "5388d6..."
          //   }
          CompositeKey orgBurnBalanceKeyComposite = new CompositeKey(BURN_BALANCE_PREFIX.getValue(), MSP_ID_ORG1);
          String orgBurnBalanceKey = orgBurnBalanceKeyComposite.toString();
          when(stub.createCompositeKey(BURN_BALANCE_PREFIX.getValue(), MSP_ID_ORG1)).thenReturn(orgBurnBalanceKeyComposite);


          BurnRequest existingBurnRequest = new BurnRequest(
                  "tx123",                                // some TX id
                  orgBurnBalanceKey,      // "burnBalanceOrg1MSP" or similar
                  "PL0000000000000000000000000",          // must match the "toIBAN" in JSON
                  100L,                                   // same amount in JSON
                  new Date(1735689600000L)
          );
          BurnRequestWithHash existingBurnWithHash =
                  new BurnRequestWithHash(existingBurnRequest, BURN_REQUEST_HASH);

          // Serialize that to JSON (or however your chaincode stored it):
          existingBurnJson = ConfirmationVerifier.marshalString(existingBurnWithHash);

          // Return it from the stub:
          System.out.println("burnKey mockup: "+burnKey.toString());
          System.out.println("burnKey mockup: "+burnKey.toString());

          when(stub.getStringState(burnKey)).thenReturn(existingBurnJson);

          // 6) The chaincode also checks the burn wallet. For this BurnRequest we used
          //    BURN_BALANCE_PREFIX + MSP_ID_ORG1 as burnWallet key => "burnBalanceOrg1MSP".
          //    Let's match exactly what's in existingBurnRequest.
          // For demonstration, we assume "burnBalanceOrg1MSP"
          // or "burnBalanceOrg1" – just ensure the same string is used consistently.
          when(stub.getStringState(orgBurnBalanceKey)).thenReturn("200"); // Enough to burn 100

          // 7) If chaincode checks if the transaction hash was "used", return null or empty
          String usedKey = USED_TRANSACTIONS_PREFIX.getValue() + BURN_REQUEST_HASH;
          when(stub.getStringState(usedKey)).thenReturn(null);

          // 8) Optional: If the chaincode uses getHistoryForKey
          //    to verify no prior usage, we can mock an empty iterator
          @SuppressWarnings("unchecked")
          QueryResultsIterator<KeyModification> mockHistory = mock(QueryResultsIterator.class);
          Iterator<KeyModification> emptyIterator = java.util.Collections.emptyIterator();
          when(mockHistory.iterator()).thenReturn(emptyIterator);
          when(stub.getHistoryForKey(burnKey.toString())).thenReturn(mockHistory);
      }

      @Test
      public void testFinalizeBurn_Success() {
          // 1) We expect a successful finalize, so no exception
          assertDoesNotThrow(() -> {
              contract.FinalizeBurn(ctx, finalizeBurnJsonContent);
          });

          // 2) Verify ledger updates

          // 2a) totalSupply: originally "1000000" => minus 100 => "999900"
          verify(stub).putStringState(TOTAL_SUPPLY_KEY.getValue(), "999900");

          // 2b) burn wallet: originally "200" => minus 100 => "100"
          // The wallet key was the one from the existing BurnRequest
          // e.g. "burnBalanceOrg1MSP" or "burnBalanceOrg1"
          // We'll capture it from existingBurnRequest
          // but we know it was 'BURN_BALANCE_PREFIX + MSP_ID_ORG1'
          CompositeKey orgBurnBalanceKeyComposite = new CompositeKey(BURN_BALANCE_PREFIX.getValue(), MSP_ID_ORG1);
          String orgBurnBalanceKey = orgBurnBalanceKeyComposite.toString();

          verify(stub).putStringState(orgBurnBalanceKey, "100");

          // 2c) The burnKey is removed from world state
          CompositeKey burnKeyComposite = new CompositeKey(BURN_TRANSACTIONS_PREFIX.getValue(), BURN_REQUEST_HASH);
          String burnKey = burnKeyComposite.toString();

          verify(stub).delState(burnKey);

          verify(stub).setEvent(eq("BurnFinalizedEvent"), any(byte[].class));


          // 2e) The chaincode "markAsUsed" => puts something like "used_<hash>" in the ledger
          String usedKey=USED_TRANSACTIONS_PREFIX.getValue()+BURN_REQUEST_HASH;
          verify(stub).putStringState(usedKey, existingBurnJson);
      }

      @Test
      public void testFinalizeBurn_UnauthorizedMSP() {
          // given
          when(ci.getMSPID()).thenReturn("SomeOtherMSP"); // Not Org1/Org2/ORG3

          // when & then
          ChaincodeException thrown = assertThrows(
                  ChaincodeException.class,
                  () -> contract.FinalizeBurn(ctx, finalizeBurnJsonContent)
          );

          String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);
          assertEquals(UNAUTHORIZED_SENDER.toString(), errorCode);
          assertTrue(thrown.getMessage().contains("Client is not authorized to finalize burn"));
      }

      @Test
      public void testFinalizeBurn_RoleNotAdmin() {
          // given
          when(ci.getAttributeValue("role")).thenReturn("viewer"); // or "user", etc.

          // when & then
          ChaincodeException thrown = assertThrows(
                  ChaincodeException.class,
                  () -> contract.FinalizeBurn(ctx, finalizeBurnJsonContent)
          );
          String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);
          assertEquals(UNAUTHORIZED_SENDER.toString(), errorCode);
          assertTrue(thrown.getMessage().contains("Only admin can finalize burn"));
      }

      @Test
      public void testFinalizeBurn_HashNotMatching() {
          // given
          // Let's say we can mock the result of 'verifyHashFromJson' if it's not static
          // or if you have a wrapper for it. For demonstration, assume you have a method:
          //   public VerificationResult verifyHashFromJson(String json) { ... }
          // We'll spy on the contract to override the behavior:
          try (MockedStatic<ConfirmationVerifier> mockedStatic = mockStatic(ConfirmationVerifier.class)) {
              // Mock the static method
              VerificationResult mockResult = mock(VerificationResult.class);
              when(mockResult.isMatches()).thenReturn(false); // Force mismatch
              when(mockResult.getConfirmation()).thenReturn(mock(Confirmation.class));

              mockedStatic.when(() -> ConfirmationVerifier.verifyHashFromJson(anyString()))
                      .thenReturn(mockResult);

              // Perform your test
              ChaincodeException thrown = assertThrows(
                      ChaincodeException.class,
                      () -> contract.FinalizeBurn(ctx, finalizeBurnJsonContent)
              );
              String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);
              assertEquals(UNAUTHORIZED_SENDER.toString(), errorCode);
              assertTrue(thrown.getMessage().contains("Hash of transaction data isn't equal"));
          }
      }

      @Test
      public void testFinalizeBurn_BurnRecordMissing() {
          // given
          // We'll mimic the same burnKey but return null from stub
          CompositeKey burnKeyComposite =
                  new CompositeKey(BURN_TRANSACTIONS_PREFIX.getValue(), BURN_REQUEST_HASH);
          when(stub.getStringState(burnKeyComposite.toString())).thenReturn(null); // no record

          // when & then
          ChaincodeException thrown = assertThrows(
                  ChaincodeException.class,
                  () -> contract.FinalizeBurn(ctx, finalizeBurnJsonContent)
          );
          String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);
          assertEquals(DUPLICATE_TRANSACTION_ID.toString(), errorCode);
          assertTrue(thrown.getMessage().contains("Burn ID doesn't exists in world state"));
      }

      @Test
      public void testFinalizeBurn_ReceiverIbanMismatch() {
          // given
          // Provide a client IBAN that differs from the "toIBAN" in confirmation
          when(ci.getAttributeValue("hf.iban")).thenReturn("PL1111111111111111111111111");
          // The finalizeBurnJsonContent says "toIBAN" is "PL0000000000000000000000000"

          // when & then
          ChaincodeException thrown = assertThrows(
                  ChaincodeException.class,
                  () -> contract.FinalizeBurn(ctx, finalizeBurnJsonContent)
          );
          String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);
          assertEquals("INVALID_IBAN", errorCode);
          assertTrue(thrown.getMessage().contains("Receiver IBAN isn't the org one"));
      }

      @Test
      public void testFinalizeBurn_InsufficientBurnBalance() {
          // given
          // The chaincode checks "burnBalance < burnRequestAmount" => throw ...
          // We'll mock the burn wallet to only have "50" but the request tries to burn "100"
          CompositeKey orgBurnBalanceKey =
                  new CompositeKey(BURN_BALANCE_PREFIX.getValue(), MSP_ID_ORG1);
          when(stub.getStringState(orgBurnBalanceKey.toString())).thenReturn("50"); // insufficient

          // when & then
          ChaincodeException thrown = assertThrows(
                  ChaincodeException.class,
                  () -> contract.FinalizeBurn(ctx, finalizeBurnJsonContent)
          );
          String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);
          assertEquals(NOT_FOUND.toString(), errorCode);
          assertTrue(thrown.getMessage().contains("Not sufficient amount on burn wallet"));
      }


      @Test
      public void testFinalizeBurn_InsufficientTotalSupply() {
          // given
          // The code checks if totalSupply < burnRequestAmount => throw ...
          when(stub.getStringState(TOTAL_SUPPLY_KEY.getValue())).thenReturn("50"); // less than 100

          // when & then
          ChaincodeException thrown = assertThrows(
                  ChaincodeException.class,
                  () -> contract.FinalizeBurn(ctx, finalizeBurnJsonContent)
          );
          String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);
          assertEquals(NOT_FOUND.toString(), errorCode);
          assertTrue(thrown.getMessage().contains("Not sufficient amount on total supply"));
      }


      @Test
      public void testFinalizeBurn_AmountMismatch() {
          // given
          // We can directly manipulate the JSON or the ledger to cause a mismatch.
          // E.g., if ledger says BurnRequest was for 100, but the JSON says 200.
          // Let’s just mock the confirmation amount:

          try (MockedStatic<ConfirmationVerifier> mockedStatic = mockStatic(ConfirmationVerifier.class)) {
              // Mock the static method
              Confirmation mockConfirmation = mock(Confirmation.class);
              VerificationResult mockResult = mock(VerificationResult.class);
              when(mockResult.isMatches()).thenReturn(true); // Force mismatch
              when(mockResult.getConfirmation()).thenReturn(mockConfirmation);

              when(mockConfirmation.getAmount()).thenReturn(200L);
              when(mockConfirmation.getFromIBAN()).thenReturn("PL9999999999999999999999999");
              when(mockConfirmation.getToIBAN()).thenReturn("PL0000000000000000000000000");
              // rawData => "BurnRequestHash" = BURN_REQUEST_HASH
              Map<String,Object> mockRawData = new HashMap<>();
              mockRawData.put("BurnRequestHash", BURN_REQUEST_HASH);
              when(mockConfirmation.getRawData()).thenReturn(mockRawData);

              mockedStatic.when(() -> ConfirmationVerifier.verifyHashFromJson(anyString()))
                      .thenReturn(mockResult);

              mockedStatic.when(() -> ConfirmationVerifier.unmarshalString(anyString(), any()))
                      .thenCallRealMethod();

              // Perform your test
              ChaincodeException thrown = assertThrows(
                      ChaincodeException.class,
                      () -> contract.FinalizeBurn(ctx, finalizeBurnJsonContent)
              );
              String errorCode = new String(thrown.getPayload(), StandardCharsets.UTF_8);
              assertEquals(INVALID_ARGUMENT.toString(), errorCode);
              assertTrue(thrown.getMessage().contains("Amount of request and actual transaction isn't the same"));
          }
      }
  }

  /*@Nested
  class InvokeQueryERC20TokenOptionsTransaction {

    @Test
    public void whenTokenNameExists() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      when(ctx.getStub()).thenReturn(stub);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      String tokenName = contract.TokenName(ctx);
      assertThat(tokenName).isEqualTo(NAME_VALUE.getValue());
    }

    @Test
    public void whenTokenNameDoesNotExist() {
      ERC20TokenContract contract = new ERC20TokenContract();
      final Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      when(ctx.getStub()).thenReturn(stub);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn("");
      Throwable thrown = catchThrowable(() -> contract.TokenName(ctx));
      assertThat(thrown)
          .isInstanceOf(ChaincodeException.class)
          .hasNoCause()
          .hasMessage("Contract options need to be set before calling any function, call Initialize() to initialize contract");
    }

    @Test
    public void whenTokenSymbolExists() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      when(ctx.getStub()).thenReturn(stub);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(stub.getStringState(SYMBOL_KEY.getValue())).thenReturn(SYMBOL_VALUE.getValue());
      String toknName = contract.TokenSymbol(ctx);
      assertThat(toknName).isEqualTo(SYMBOL_VALUE.getValue());
    }

    @Test
    public void whenTokenSymbolDoesNotExist() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      when(ctx.getStub()).thenReturn(stub);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(stub.getStringState(SYMBOL_KEY.getValue())).thenReturn("");
      Throwable thrown = catchThrowable(() -> contract.TokenSymbol(ctx));
      assertThat(thrown)
          .isInstanceOf(ChaincodeException.class)
          .hasNoCause()
          .hasMessage("Token symbol not found");
    }

    @Test
    public void whenTokenDecimalExists() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      when(ctx.getStub()).thenReturn(stub);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(stub.getStringState(DECIMALS_KEY.getValue())).thenReturn("18");
      long decimal = contract.Decimals(ctx);
      assertThat(decimal).isEqualTo(18);
    }

    @Test
    public void whenTokenDecimalNotExists() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      when(ctx.getStub()).thenReturn(stub);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(stub.getStringState(DECIMALS_KEY.getValue())).thenReturn("");
      Throwable thrown = catchThrowable(() -> contract.Decimals(ctx));
      assertThat(thrown)
          .isInstanceOf(ChaincodeException.class)
          .hasNoCause()
          .hasMessage("Decimal not found");
    }

    @Test
    public void whenTokenTotalSupplyExists() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      when(ctx.getStub()).thenReturn(stub);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(stub.getStringState(TOTAL_SUPPLY_KEY.getValue())).thenReturn("222222222222");
      long totalSupply = contract.TotalSupply(ctx);
      assertThat(totalSupply).isEqualTo(222222222222L);
    }

    @Test
    public void whenTokenTotalSupplyNotExists() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      when(ctx.getStub()).thenReturn(stub);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(stub.getStringState(TOTAL_SUPPLY_KEY.getValue())).thenReturn("");
      Throwable thrown = catchThrowable(() -> contract.TotalSupply(ctx));
      assertThat(thrown)
          .isInstanceOf(ChaincodeException.class)
          .hasNoCause()
          .hasMessage("Total Supply  not found");
    }

    @Test
    public void ClientAccountIDTest() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      ClientIdentity ci = mock(ClientIdentity.class);
      when(ctx.getClientIdentity()).thenReturn(ci);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(ci.getMSPID()).thenReturn(ORG1.getValue());
      when(ci.getId()).thenReturn(org1UserId);
      when(ctx.getStub()).thenReturn(stub);
      assertThat(ci.getMSPID()).isEqualTo(ORG1.getValue());
      String id = contract.ClientAccountID(ctx);
      assertThat(id).isEqualTo(org1UserId);
    }
  }

  @Nested
  class TokenOperationsInvoke {

    @Test
    public void invokeInitializeTest() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      when(ctx.getStub()).thenReturn(stub);
      ClientIdentity ci = mock(ClientIdentity.class);
      when(ctx.getClientIdentity()).thenReturn(ci);
      when(ci.getMSPID()).thenReturn(ORG1.getValue());
      contract.Initialize(ctx, NAME_VALUE.getValue(), SYMBOL_VALUE.getValue(), "18");
      verify(stub).putStringState(NAME_KEY.getValue(), NAME_VALUE.getValue());
      verify(stub).putStringState(SYMBOL_KEY.getValue(), SYMBOL_VALUE.getValue());
      verify(stub).putStringState(DECIMALS_KEY.getValue(), "18");
    }

    @Test
    public void invokeBalanceOfTest() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      when(ctx.getStub()).thenReturn(stub);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      ClientIdentity ci = mock(ClientIdentity.class);
      when(ctx.getClientIdentity()).thenReturn(ci);
      when(ci.getMSPID()).thenReturn(ORG1.getValue());
      when(ci.getId()).thenReturn(org1UserId);
      when(ctx.getStub()).thenReturn(stub);
      CompositeKey ck = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), org1UserId)).thenReturn(ck);
      when(ck.toString()).thenReturn(BALANCE_PREFIX.getValue() + org1UserId);
      when(stub.getStringState(ck.toString())).thenReturn("1000");
      long balance = contract.BalanceOf(ctx, org1UserId);
      assertThat(balance).isEqualTo(1000);
    }

    @Test
    public void invokeClientAccountBalanceTest() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      when(ctx.getStub()).thenReturn(stub);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      ClientIdentity ci = mock(ClientIdentity.class);
      when(ctx.getClientIdentity()).thenReturn(ci);
      when(ci.getMSPID()).thenReturn(ORG1.getValue());
      when(ci.getId()).thenReturn(org1UserId);
      when(ctx.getStub()).thenReturn(stub);
      CompositeKey ck = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), org1UserId)).thenReturn(ck);
      when(ck.toString()).thenReturn(BALANCE_PREFIX.getValue() + org1UserId);
      when(stub.getStringState(ck.toString())).thenReturn("1000");
      long balance = contract.ClientAccountBalance(ctx);
      assertThat(balance).isEqualTo(1000);
    }

    @Test
    public void invokeMintTokenTest() {

      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      ClientIdentity ci = mock(ClientIdentity.class);

      when(ctx.getClientIdentity()).thenReturn(ci);
      when(ci.getAttributeValue("role")).thenReturn("admin");
      when(ci.getMSPID()).thenReturn(ORG1.getValue());
      when(ci.getId()).thenReturn(org1UserId);
      when(ctx.getStub()).thenReturn(stub);


      CompositeKey ck = mock(CompositeKey.class);
      //when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), org1UserId)).thenReturn(ck);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), ORG1.getValue())).thenReturn(ck);
      when(ck.toString()).thenReturn(BALANCE_PREFIX.getValue() + ORG1.getValue());
      when(stub.getStringState(ck.toString())).thenReturn(null);

      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(ctx.getStub()).thenReturn(stub);

      when(stub.getStringState(USED_TRANSACTIONS_PREFIX.getValue()) + "12345").thenReturn(null);
      //when(stub.putStringState((USED_TRANSACTIONS_PREFIX.getValue() + "12345"), "used").thenReturn(null);

      contract.Mint(ctx, 1000, "12345");

      verify(stub).putStringState(USED_TRANSACTIONS_PREFIX.getValue() + "12345", "used");
      verify(stub).putStringState(TOTAL_SUPPLY_KEY.getValue(), "1000");
      verify(stub).putStringState(ck.toString(), "1000");

      String expectedPolicy = "AND('YachtSales.member', 'FurnituresMakers.member', 'WoodSupply.member')";
      verify(stub).setStateValidationParameter(TOTAL_SUPPLY_KEY.getValue(), expectedPolicy.getBytes(UTF_8));
    }

    @Test
    public void whenMintTokenUnAuthorized() {

      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      ClientIdentity ci = mock(ClientIdentity.class);
      when(ctx.getClientIdentity()).thenReturn(ci);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(ci.getMSPID()).thenReturn("Org2MSP");
      when(ci.getId()).thenReturn(org1UserId);
      when(ctx.getStub()).thenReturn(stub);
      CompositeKey ck = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), org1UserId)).thenReturn(ck);
      when(ck.toString()).thenReturn(BALANCE_PREFIX.getValue() + org1UserId);
      when(stub.getStringState(ck.toString())).thenReturn(null);
      when(ctx.getStub()).thenReturn(stub);
      Throwable thrown = catchThrowable(() -> contract.Mint(ctx, 1000, "12345"));
      assertThat(thrown)
          .isInstanceOf(ChaincodeException.class)
          .hasNoCause()
          .hasMessage("Client is not authorized to mint new tokens");
    }

    @Test
    public void invokeTokenTransferTest() {

      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      ClientIdentity ci = mock(ClientIdentity.class);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(ctx.getClientIdentity()).thenReturn(ci);
      when(ci.getMSPID()).thenReturn(ORG1.getValue());
      when(ci.getId()).thenReturn(org1UserId);
      when(ctx.getStub()).thenReturn(stub);
      String to =
          "x509::CN=User2@org2.example.com, L=San Francisco, ST=California,"
              + " C=US::CN=ca.org2.example.com, O=org2.example.com, L=San Francisco, ST=California, C=US";

      CompositeKey ckFrom = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), org1UserId)).thenReturn(ckFrom);
      when(ckFrom.toString()).thenReturn(BALANCE_PREFIX.getValue() + org1UserId);
      when(stub.getStringState(ckFrom.toString())).thenReturn("1000");
      CompositeKey ckTo = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), to)).thenReturn(ckTo);
      when(ckTo.toString()).thenReturn(BALANCE_PREFIX.getValue() + to);
      when(stub.getStringState(ckTo.toString())).thenReturn(null);
      contract.Transfer(ctx, to, 100);
      verify(stub).putStringState(ckTo.toString(), "100");
      verify(stub).putStringState(ckFrom.toString(), "900");
    }

    @Test
    public void whenZeroAmountTokenTransferTest() {

      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      ClientIdentity ci = mock(ClientIdentity.class);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(ctx.getClientIdentity()).thenReturn(ci);
      when(ci.getMSPID()).thenReturn(ORG1.getValue());
      when(ci.getId()).thenReturn(org1UserId);
      when(ctx.getStub()).thenReturn(stub);
      String to =
          "x509::CN=User2@org2.example.com, L=San Francisco, ST=California,"
              + " C=US::CN=ca.org2.example.com, O=org2.example.com, L=San Francisco, ST=California, C=US";

      CompositeKey ckFrom = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), org1UserId)).thenReturn(ckFrom);
      when(ckFrom.toString()).thenReturn(BALANCE_PREFIX.getValue() + org1UserId);
      when(stub.getStringState(ckFrom.toString())).thenReturn("1000");
      CompositeKey ckTo = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), to)).thenReturn(ckTo);
      when(ckTo.toString()).thenReturn(BALANCE_PREFIX.getValue() + to);
      when(stub.getStringState(ckTo.toString())).thenReturn(null);
      contract.Transfer(ctx, to, 0);
      verify(stub).putStringState(ckTo.toString(), "0");
      verify(stub).putStringState(ckFrom.toString(), "1000");
    }

    @Test
    public void whenTokenTransferNegativeAmount() {

      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      ClientIdentity ci = mock(ClientIdentity.class);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(ctx.getClientIdentity()).thenReturn(ci);
      when(ci.getMSPID()).thenReturn(ORG1.getValue());
      when(ci.getId()).thenReturn(org1UserId);
      when(ctx.getStub()).thenReturn(stub);
      String to =
          "x509::CN=User2@org2.example.com, L=San Francisco, ST=California,"
              + " C=US::CN=ca.org2.example.com, O=org2.example.com, L=San Francisco, ST=California, C=US";

      CompositeKey ckFrom = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), org1UserId)).thenReturn(ckFrom);
      when(ckFrom.toString()).thenReturn(BALANCE_PREFIX.getValue() + org1UserId);
      when(stub.getStringState(ckFrom.toString())).thenReturn("1000");
      CompositeKey ckTo = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), to)).thenReturn(ckTo);
      when(ckTo.toString()).thenReturn(BALANCE_PREFIX.getValue() + to);
      when(stub.getStringState(ckTo.toString())).thenReturn(null);

      Throwable thrown = catchThrowable(() -> contract.Transfer(ctx, to, -1));
      assertThat(thrown)
          .isInstanceOf(ChaincodeException.class)
          .hasNoCause()
          .hasMessage("Transfer amount cannot be negative");
    }

    @Test
    public void whenTokenTransferSameId() {

      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      ClientIdentity ci = mock(ClientIdentity.class);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(ctx.getClientIdentity()).thenReturn(ci);
      when(ci.getMSPID()).thenReturn(ORG1.getValue());
      when(ci.getId()).thenReturn(org1UserId);
      when(ctx.getStub()).thenReturn(stub);
      String to =
          "x509::CN=User2@org2.example.com, L=San Francisco, ST=California,"
              + " C=US::CN=ca.org2.example.com, O=org2.example.com, L=San Francisco, ST=California, C=US";

      CompositeKey ckFrom = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), org1UserId)).thenReturn(ckFrom);
      when(ckFrom.toString()).thenReturn(BALANCE_PREFIX.getValue() + org1UserId);
      when(stub.getStringState(ckFrom.toString())).thenReturn("1000");
      CompositeKey ckTo = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), to)).thenReturn(ckTo);
      when(ckTo.toString()).thenReturn(BALANCE_PREFIX.getValue() + to);
      when(stub.getStringState(ckTo.toString())).thenReturn(null);

      Throwable thrown = catchThrowable(() -> contract.Transfer(ctx, org1UserId, 10));
      assertThat(thrown)
          .isInstanceOf(ChaincodeException.class)
          .hasNoCause()
          .hasMessage("Cannot transfer to and from same client account");
    }

    @Test
    public void invokeTokenBurnTest() {

      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      ClientIdentity ci = mock(ClientIdentity.class);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(ctx.getClientIdentity()).thenReturn(ci);
      when(ci.getMSPID()).thenReturn(ORG1.getValue());
      when(ci.getId()).thenReturn(org1UserId);
      CompositeKey ck = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), org1UserId)).thenReturn(ck);
      when(ck.toString()).thenReturn(BALANCE_PREFIX.getValue() + org1UserId);
      when(stub.getStringState(ck.toString())).thenReturn(null);
      when(ctx.getStub()).thenReturn(stub);
      when(stub.getStringState(TOTAL_SUPPLY_KEY.getValue())).thenReturn("1000");
      when(stub.getStringState(ck.toString())).thenReturn("1000");
      contract.Burn(ctx, 100);
      verify(stub).putStringState(TOTAL_SUPPLY_KEY.getValue(), "900");
    }

    @Test
    public void whenTokenBurnUnAuthorizedTest() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      ClientIdentity ci = mock(ClientIdentity.class);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(ctx.getClientIdentity()).thenReturn(ci);
      when(ci.getMSPID()).thenReturn("Org2MSP");
      when(ci.getId()).thenReturn(spender);
      CompositeKey ck = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), spender)).thenReturn(ck);
      when(ck.toString()).thenReturn(BALANCE_PREFIX.getValue() + spender);
      when(stub.getStringState(ck.toString())).thenReturn(null);
      when(ctx.getStub()).thenReturn(stub);
      when(stub.getStringState(TOTAL_SUPPLY_KEY.getValue())).thenReturn("1000");
      when(stub.getStringState(ck.toString())).thenReturn("1000");

      Throwable thrown = catchThrowable(() -> contract.Burn(ctx, 100));
      assertThat(thrown)
          .isInstanceOf(ChaincodeException.class)
          .hasNoCause()
          .hasMessage("Client is not authorized to burn tokens");
    }

    @Test
    public void whenTokenBurnNegativeAmountTest() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      ClientIdentity ci = mock(ClientIdentity.class);

      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(ctx.getClientIdentity()).thenReturn(ci);
      when(ci.getMSPID()).thenReturn(ORG1.getValue());
      when(ci.getId()).thenReturn(org1UserId);
      CompositeKey ck = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), org1UserId)).thenReturn(ck);
      when(ck.toString()).thenReturn(BALANCE_PREFIX.getValue() + ORG1.getValue());
      when(stub.getStringState(ck.toString())).thenReturn(null);
      when(ctx.getStub()).thenReturn(stub);
      when(stub.getStringState(TOTAL_SUPPLY_KEY.getValue())).thenReturn("1000");
      when(stub.getStringState(ck.toString())).thenReturn("1000");

      Throwable thrown = catchThrowable(() -> contract.Burn(ctx, -100));
      assertThat(thrown)
          .isInstanceOf(ChaincodeException.class)
          .hasNoCause()
          .hasMessage("Burn amount must be a positive integer");
    }
  }

  @Nested
  class InvokeERC20AllowanceTransactions {

    @Test
    public void invokeAllowanceTest() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      ClientIdentity ci = mock(ClientIdentity.class);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(ctx.getClientIdentity()).thenReturn(ci);
      when(ci.getMSPID()).thenReturn(ORG1.getValue());
      when(ci.getId()).thenReturn(org1UserId);
      when(ctx.getStub()).thenReturn(stub);
      CompositeKey ck = mock(CompositeKey.class);
      when(stub.createCompositeKey(ALLOWANCE_PREFIX.getValue(), org1UserId, spender))
          .thenReturn(ck);
      when(ck.toString()).thenReturn(ALLOWANCE_PREFIX.getValue() + org1UserId + spender);
      when(stub.getStringState(ck.toString())).thenReturn("100");
      long allowance = contract.Allowance(ctx, org1UserId, spender);
      assertThat(allowance).isEqualTo(100);
    }

    @Test
    public void invokeApproveForTokenAllowanceTest() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      ClientIdentity ci = mock(ClientIdentity.class);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(ctx.getClientIdentity()).thenReturn(ci);
      when(ci.getMSPID()).thenReturn(ORG1.getValue());
      when(ci.getId()).thenReturn(org1UserId);
      when(ctx.getStub()).thenReturn(stub);
      CompositeKey ck = mock(CompositeKey.class);
      when(stub.createCompositeKey(ALLOWANCE_PREFIX.getValue(), org1UserId, spender))
          .thenReturn(ck);
      when(ck.toString()).thenReturn(ALLOWANCE_PREFIX.getValue() + org1UserId + spender);
      contract.Approve(ctx, spender, 200);
      verify(stub).putStringState(ck.toString(), String.valueOf(200));
    }

    @Test
    public void invokeAllowanceTransferFromTest() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      ClientIdentity ci;

      String to =
          "x509::CN=User3@org2.example.com, L=San Francisco, ST=California,"
              + " C=US::CN=ca.org2.example.com, O=org2.example.com, L=San Francisco, ST=California, C=US";

      CompositeKey ckFromBalance = mock(CompositeKey.class);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(stub.createCompositeKey(BALANCE_PREFIX.toString(), org1UserId))
          .thenReturn(ckFromBalance);
      when(ckFromBalance.toString()).thenReturn(BALANCE_PREFIX.getValue() + org1UserId);
      CompositeKey ckTOBalance = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.toString(), to)).thenReturn(ckTOBalance);
      when(ckFromBalance.toString()).thenReturn(BALANCE_PREFIX.getValue() + to);
      when(ctx.getStub()).thenReturn(stub);
      ci = mock(ClientIdentity.class);
      when(ctx.getClientIdentity()).thenReturn(ci);
      when(ci.getMSPID()).thenReturn(ORG1.getValue()); //Było ORG2
      when(ci.getId()).thenReturn(spender);
      CompositeKey ckAllowance = mock(CompositeKey.class);
      when(stub.createCompositeKey(ALLOWANCE_PREFIX.getValue(), org1UserId, spender))
          .thenReturn(ckAllowance);
      when(ckAllowance.toString()).thenReturn(ALLOWANCE_PREFIX.getValue() + org1UserId + spender);
      when(stub.getStringState(ckAllowance.toString())).thenReturn("200");
      CompositeKey ckFrom = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), org1UserId)).thenReturn(ckFrom);
      when(ckFrom.toString()).thenReturn(BALANCE_PREFIX.getValue() + org1UserId);
      when(stub.getStringState(ckFrom.toString())).thenReturn("1000");
      CompositeKey ckTo = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), to)).thenReturn(ckTo);
      when(ckTo.toString()).thenReturn(BALANCE_PREFIX.getValue() + to);
      when(stub.getStringState(ckTo.toString())).thenReturn(null);
      contract.TransferFrom(ctx, org1UserId, to, 100);
      verify(stub).putStringState(ckTo.toString(), String.valueOf(100));
      verify(stub).putStringState(ckFrom.toString(), String.valueOf(900));
    }

    @Test
    public void whenClientSameAllowanceTransferFrom() {
      ERC20TokenContract contract = new ERC20TokenContract();
      Context ctx = mock(Context.class);
      ChaincodeStub stub = mock(ChaincodeStub.class);
      ClientIdentity ci;
      String to =
          "x509::CN=User3@org2.example.com, L=San Francisco, ST=California,"
              + " C=US::CN=ca.org2.example.com, O=org2.example.com, L=San Francisco, ST=California, C=US";

      CompositeKey ckFromBalance = mock(CompositeKey.class);
      when(stub.getStringState(NAME_KEY.getValue())).thenReturn(NAME_VALUE.getValue());
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), org1UserId))
          .thenReturn(ckFromBalance);
      when(ckFromBalance.toString()).thenReturn(BALANCE_PREFIX.getValue() + org1UserId);
      CompositeKey ckTOBalance = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), to)).thenReturn(ckTOBalance);
      when(ckFromBalance.toString()).thenReturn(BALANCE_PREFIX.getValue() + to);
      when(ctx.getStub()).thenReturn(stub);
      ci = mock(ClientIdentity.class);
      when(ctx.getClientIdentity()).thenReturn(ci);
      when(ci.getMSPID()).thenReturn(ORG1.getValue()); //było org2
      when(ci.getId()).thenReturn(spender);
      CompositeKey ckAllowance = mock(CompositeKey.class);
      when(stub.createCompositeKey(ALLOWANCE_PREFIX.getValue(), org1UserId, spender))
          .thenReturn(ckAllowance);
      when(ckAllowance.toString()).thenReturn(ALLOWANCE_PREFIX.getValue() + org1UserId + spender);
      when(stub.getStringState(ckAllowance.toString())).thenReturn("200");
      CompositeKey ckFrom = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), org1UserId)).thenReturn(ckFrom);
      when(ckFrom.toString()).thenReturn(BALANCE_PREFIX.getValue() + org1UserId);
      when(stub.getStringState(ckFrom.toString())).thenReturn("1000");
      CompositeKey ckTo = mock(CompositeKey.class);
      when(stub.createCompositeKey(BALANCE_PREFIX.getValue(), to)).thenReturn(ckTo);
      when(ckTo.toString()).thenReturn(BALANCE_PREFIX.getValue() + to);
      when(stub.getStringState(ckTo.toString())).thenReturn(null);

      Throwable thrown =
          catchThrowable(() -> contract.TransferFrom(ctx, org1UserId, org1UserId, 100));
      assertThat(thrown)
          .isInstanceOf(ChaincodeException.class)
          .hasNoCause()
          .hasMessage("Cannot transfer to and from same client account");
    }
  }*/
}
