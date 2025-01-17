/*
 * SPDX-License-Identifier: Apache-2.0
 */
package com.blockchainbiz.erc20;

import static com.blockchainbiz.erc20.ContractConstants.*;
import static com.blockchainbiz.erc20.ContractErrors.*;
import static com.blockchainbiz.erc20.utils.ConfirmationVerifier.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static com.blockchainbiz.erc20.utils.ContractUtility.stringIsNullOrEmpty;

import com.blockchainbiz.erc20.model.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import com.blockchainbiz.erc20.utils.ConfirmationVerifier;
import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

@Contract(
    name = "erc20token",
    info =
        @Info(
            title = "ERC20Token Contract",
            description = "The erc20 fungible token implementation.",
            version = "0.0.1-SNAPSHOT",
            license =
                @License(
                    name = "Apache 2.0 License",
                    url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
            contact =
                @Contact(
                    email = "filip.szemraj@onet.pl",
                    name = "Filip Szemraj",
                    url = "https://github.com/FilipSzemraj")))
@Default
public final class ERC20TokenContract implements ContractInterface {

  final Logger logger = Logger.getLogger(ERC20TokenContract.class);

  /**
   * Mint creates new tokens and adds them to minter's account balance. This function triggers a
   * Transfer event.
   *
   * @param ctx the transaction context
   * @param jsonContent serialized {@link ConfirmationWithHash}
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public void Mint(final Context ctx, final String jsonContent) {

    // Check minter authorization
    String clientMSPID = ctx.getClientIdentity().getMSPID();
    ChaincodeStub stub = ctx.getStub();
    if (!(clientMSPID.equalsIgnoreCase(ORG1.getValue())
            || clientMSPID.equalsIgnoreCase(ORG2.getValue())
            || clientMSPID.equalsIgnoreCase(ORG3.getValue()))) {
      throw new ChaincodeException(
              "Client is not authorized to mint new tokens", UNAUTHORIZED_SENDER.toString());
    }

    String role = ctx.getClientIdentity().getAttributeValue("role");

    if (!"admin".equalsIgnoreCase(role)) {
      throw new ChaincodeException("Unauthorized: Only admin can mint tokens", UNAUTHORIZED_SENDER.toString());
    }
    //String policy = "AND('YachtSales.member', 'FurnituresMakers.member', 'WoodSupply.member')";
    //stub.setStateValidationParameter("totalSupply", policy.getBytes(UTF_8));

    this.checkInitialized(ctx);

    // Check correctness of confirmation and get transaction data.
    final VerificationResult result = verifyHashFromJson(jsonContent);
    final Confirmation confirmation = result.getConfirmation();
    final long amount = confirmation.getAmount();
    final String localHash = result.getLocalHash();
    final String sourceIBAN = confirmation.getFromIBAN();

    if(!result.isMatches()){
      throw new ChaincodeException("Unauthorized: Hash of transaction data isn't equal to the encrypted one", UNAUTHORIZED_SENDER.toString());
    }

    // Check contract options are already set first to execute the function
    if (wasAlreadyUsed(stub, localHash)) {
      throw new ChaincodeException(
              "Transaction ID %s was already used", DUPLICATE_TRANSACTION_ID.toString()
      );
    }
    String key = USED_TRANSACTIONS_PREFIX.getValue() + localHash;
    // Ledger history checking for burnKey existance
    QueryResultsIterator<KeyModification> history = stub.getHistoryForKey(key.toString());
    if (history.iterator().hasNext()) {
      throw new ChaincodeException("Burn ID already used in the past", DUPLICATE_TRANSACTION_ID.toString());
    }

    // Get ID of submitting client identity
    ClientIdentity clientIdentity = ctx.getClientIdentity();
    String minter = clientIdentity.getId();
    String clientIBAN = clientIdentity.getAttributeValue("hf.iban");
    String orgIBAN = confirmation.getToIBAN();

    if (clientIBAN == null || clientIBAN != sourceIBAN) {
      throw new RuntimeException("Client IBAN not found in certificate or isn't the same as the one from transaction details.");
    }

    String currentIBAN = stub.getStringState(IBAN_KEY.getValue());
    if(stringIsNullOrEmpty(currentIBAN)){
      throw new ChaincodeException(
              "No IBAN number found for the consortium. Initialization or operation cannot proceed.", NOT_FOUND.toString());
    }
    if(!currentIBAN.equalsIgnoreCase(orgIBAN)){
      throw new ChaincodeException(
              "The provided recipient IBAN does not match the consortium's IBAN. Operation cannot proceed.", INVALID_CONFIRMATION.toString());
    }

    if (amount <= 0) {
      throw new ChaincodeException(
          "Mint amount must be a positive integer", INVALID_AMOUNT.toString());
    }
    CompositeKey balanceKey = stub.createCompositeKey(BALANCE_PREFIX.getValue(), clientMSPID);
    String currentBalanceStr = stub.getStringState(balanceKey.toString());
    // If minter current balance doesn't yet exist, we'll create it with a current balance of 0
    long currentBalance = 0;
    if (!stringIsNullOrEmpty(currentBalanceStr)) {
      currentBalance = Long.parseLong(currentBalanceStr);
    }
    // Used safe math .
    long updatedBalance = Math.addExact(currentBalance, amount);
    stub.putStringState(balanceKey.toString(), String.valueOf(updatedBalance));
    // Increase totalSupply
    //String policyTotalSupply = "AND('YachtSales.member', 'FurnituresMakers.member', 'WoodSupply.member')";
    //stub.setStateValidationParameter("totalSupply", policyTotalSupply.getBytes(UTF_8));

    String totalSupplyStr = stub.getStringState(TOTAL_SUPPLY_KEY.getValue());
    long totalSupply = 0;
    if (!stringIsNullOrEmpty(totalSupplyStr)) {
      totalSupply = Long.parseLong(totalSupplyStr);
    }
    // Used safe math .
    totalSupply = Math.addExact(totalSupply, amount);
    stub.putStringState(TOTAL_SUPPLY_KEY.getValue(), String.valueOf(totalSupply));
    Transfer transferEvent = new Transfer("0x0", minter, amount);
    markAsUsed(stub, localHash);
    stub.setEvent(TRANSFER_EVENT.getValue(), marshalBytes(transferEvent));
    logger.info(
        String.format(
            "minter account %s balance updated from %d to %d",
            minter, currentBalance, updatedBalance));
  }

  /**
   * Burn redeems tokens the minter's account balance. This function triggers a Transfer event.
   *
   * @param ctx the transaction context
   * @param amount amount of tokens to be burned
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public String Burn(final Context ctx, final String amount) {
    // Checking for approval
    ClientIdentity clientIdentity = ctx.getClientIdentity();
    String clientMSPID = clientIdentity.getMSPID();
    ChaincodeStub stub = ctx.getStub();
    Long amountLong;
    try {
      amountLong = Long.parseLong(amount);
    } catch (NumberFormatException e) {
      throw new ChaincodeException("Invalid amount format", INVALID_AMOUNT.toString());
    }

    if (!(clientMSPID.equalsIgnoreCase(ORG1.getValue())
            || clientMSPID.equalsIgnoreCase(ORG2.getValue())
            || clientMSPID.equalsIgnoreCase(ORG3.getValue()))) {
      throw new ChaincodeException(
          "Client is not authorized to burn tokens", UNAUTHORIZED_SENDER.toString());
    }

    // Check contract options are already set first to execute the function
    this.checkInitialized(ctx);

    // Check correctness of confirmation and get transaction data.
    //final VerificationResult result = verifyHashFromJson(jsonContent);
    //final Confirmation confirmation = result.getConfirmation();
    //final long amount = confirmation.getAmount();
    //final String localHash = result.getLocalHash();
    if (amountLong <= 0) {
      throw new ChaincodeException(
              "Burn amount must be a positive integer", INVALID_AMOUNT.toString());
    }

    // obliczanie hasza
    CompositeKey burnBalanceKey = stub.createCompositeKey(BURN_BALANCE_PREFIX.getValue(), clientMSPID);

    // Get ID of submitting client identity
    String txId = stub.getTxId();
    String burnWallet = burnBalanceKey.toString();
    String clientIBAN = clientIdentity.getAttributeValue("hf.iban");
    if (clientIBAN == null || clientIBAN.isEmpty()) {
      throw new ChaincodeException("Client IBAN is missing or invalid", "INVALID_IBAN");
    }
    Date txTimestamp = Date.from(stub.getTxTimestamp());

    BurnRequest burnRequest = new BurnRequest(txId, burnWallet, clientIBAN, amountLong, txTimestamp);
    byte[] serializedBurnRequest = ConfirmationVerifier.marshalBytes(burnRequest);
    String burnRequestHash = ConfirmationVerifier.generateSHA256Hash(serializedBurnRequest);

    BurnRequestWithHash combinedRequest = new BurnRequestWithHash(burnRequest, burnRequestHash);

    // Check contract options are already set first to execute the function sprawdzenie tego hasza
    if (wasAlreadyUsed(stub, burnRequestHash)) {
      throw new ChaincodeException(
              "Hash of transaction data was already used", DUPLICATE_TRANSACTION_ID.toString()
      );
    }

    String minter = clientIdentity.getId();


    // Sprawdzenie wartości w portfelu organizacji
    CompositeKey balanceKey = stub.createCompositeKey(BALANCE_PREFIX.getValue(), clientMSPID);
    String currentBalanceStr = stub.getStringState(balanceKey.toString());
    if (stringIsNullOrEmpty(currentBalanceStr)) {
      throw new ChaincodeException("The balance does not exist", BALANCE_NOT_FOUND.toString());
    }
    long currentBalance = Long.parseLong(currentBalanceStr);
    // Check if the sender has enough tokens to burn.

    if (currentBalance < amountLong) {
      String errorMessage = String.format("Organization account %s has insufficient funds", clientMSPID);
      throw new ChaincodeException(errorMessage, INSUFFICIENT_FUND.toString());
    }
    long updatedBalance = Math.subtractExact(currentBalance, amountLong);

    // Sprawdzenie czy taka transakcja nie miała już miejsca
    CompositeKey burnKey = stub.createCompositeKey(BURN_TRANSACTIONS_PREFIX.getValue(), burnRequestHash);

    // Checking exsitance of burnId within world state.
    String existingBurn = stub.getStringState(burnKey.toString());
    if (!stringIsNullOrEmpty(existingBurn)) {
      throw new ChaincodeException("Burn ID already exists", DUPLICATE_TRANSACTION_ID.toString());
    }

    // Ledger history checking for burnKey existance
    try (QueryResultsIterator<KeyModification> history = stub.getHistoryForKey(burnKey.toString())) {
      if (history.iterator().hasNext()) {
        throw new ChaincodeException("Burn ID already used in the past", DUPLICATE_TRANSACTION_ID.toString());
      }
    }catch (Exception e) {
      throw new RuntimeException("Error while closing the iterator: " + e.getMessage(), e);
    }

    // Zamiana wartości w portfelu organizacji
    stub.putStringState(balanceKey.toString(), String.valueOf(updatedBalance));
    //markAsWaiting(stub, localHash);
    //burnBalanceKey
    String currentBalanceBurnKeyStr = stub.getStringState(burnBalanceKey.toString());
    long currentBalanceBurnKey = 0;
    if (!stringIsNullOrEmpty(currentBalanceBurnKeyStr)) {
      currentBalanceBurnKey = Long.parseLong(currentBalanceBurnKeyStr);
    }
    currentBalanceBurnKey = Math.addExact(amountLong, currentBalanceBurnKey);

    stub.putStringState(burnBalanceKey.toString(), String.valueOf(currentBalanceBurnKey));

    // Send tokens to temporary wallet.
    stub.putStringState(burnKey.toString(), marshalString(combinedRequest));
    stub.setEvent("BurnPendingEvent", marshalBytes(combinedRequest));
    logger.info(
        String.format(
            "minter account %s balance updated from %d to %d",
            minter, currentBalance, updatedBalance));

    return burnKey.toString();
  }

  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public void FinalizeBurn(final Context ctx, final String jsonContent) {
    ClientIdentity clientIdentity = ctx.getClientIdentity();

    final String clientMSPID = ctx.getClientIdentity().getMSPID();
    ChaincodeStub stub = ctx.getStub();
    if (!(clientMSPID.equalsIgnoreCase(ORG1.getValue())
            || clientMSPID.equalsIgnoreCase(ORG2.getValue())
            || clientMSPID.equalsIgnoreCase(ORG3.getValue()))) {
      throw new ChaincodeException(
              "Client is not authorized to finalize burn new tokens", UNAUTHORIZED_SENDER.toString());
    }

    String role = ctx.getClientIdentity().getAttributeValue("role");

    if (!"admin".equalsIgnoreCase(role)) {
      throw new ChaincodeException("Unauthorized: Only admin can mint tokens", UNAUTHORIZED_SENDER.toString());
    }

    final VerificationResult result = verifyHashFromJson(jsonContent);
    final Confirmation confirmation = result.getConfirmation();
    final Map<String, Object> rawData = confirmation.getRawData();


    if(!result.isMatches()){
      throw new ChaincodeException("Unauthorized: Hash of transaction data isn't equal to the encrypted one", UNAUTHORIZED_SENDER.toString());
    }

    //CompositeKey burnKey = stub.createCompositeKey(BURN_TRANSACTIONS_PREFIX.getValue(), burnRequestHash);

    // Zadeklaruj zmienną przed blokiem try-catch
    BurnRequestWithHash burnRequestWithHash = null;

    try {
      // Pobierz wartość z mapy
      Object value = rawData.get("BurnRequestWithHash");

      // Sprawdź, czy wartość istnieje
      if (value == null) {
        throw new IllegalArgumentException("Key 'BurnRequestWithHash' not found in rawData.");
      }

      // Sprawdź, czy wartość jest instancją oczekiwanej klasy
      if (!(value instanceof BurnRequestWithHash)) {
        throw new ClassCastException("Value for key 'BurnRequestWithHash' is not of type BurnRequestWithHash.");
      }

      // Jeśli wszystko się zgadza, dokonaj rzutowania
      burnRequestWithHash = (BurnRequestWithHash) value;

    } catch (IllegalArgumentException | ClassCastException e) {
      // Obsłuż wyjątek
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
      // Możesz tutaj podjąć odpowiednie działania, np. zakończyć metodę, zwrócić błąd itd.
    }

    if (burnRequestWithHash == null) {
      throw new ChaincodeException("There was no object within confirmation that contains burn data", INVALID_ARGUMENT.toString());
    }

    final long amount = confirmation.getAmount();
    final String localHash = result.getLocalHash();
    final String burnRequestHash = burnRequestWithHash.getHash();


    CompositeKey burnKey = stub.createCompositeKey(BURN_TRANSACTIONS_PREFIX.getValue(), burnRequestHash);

    // Checking exsitance of burnId within world state.
    String existingBurn = stub.getStringState(burnKey.toString());
    if (stringIsNullOrEmpty(existingBurn)) {
      throw new ChaincodeException("Burn ID doesn't exists in world state", DUPLICATE_TRANSACTION_ID.toString());
    }

    String clientIBAN = clientIdentity.getAttributeValue("hf.iban");
    if (clientIBAN == null || clientIBAN.isEmpty()) {
      throw new ChaincodeException("Client IBAN is missing or invalid", "INVALID_IBAN");
    }

    final String receiverIBAN = confirmation.getToIBAN();
    final String senderIBAN = confirmation.getFromIBAN();
    if(!(receiverIBAN.equalsIgnoreCase(clientIBAN))){
      throw new ChaincodeException("Receiver IBAN isn't the org one", "INVALID_IBAN");
    }
    final String currentIBAN = stub.getStringState(IBAN_KEY.getValue());
    if(stringIsNullOrEmpty(currentIBAN)){
      throw new ChaincodeException(
              "No IBAN number found for the consortium. Initialization or operation cannot proceed.", NOT_FOUND.toString());
    }

    if(!(senderIBAN.equalsIgnoreCase(clientIBAN))){
      throw new ChaincodeException("Sender IBAN isn't the consortium one", "INVALID_IBAN");
    }

    BurnRequest burnRequest = burnRequestWithHash.getBurnRequest();
    final Long burnRequestAmount = burnRequest.getAmount();
    final Long confirmationAmount = confirmation.getAmount();

    if(burnRequestAmount != confirmationAmount){
      throw new ChaincodeException("Amount of request and actual transaction isn't the same." + INVALID_ARGUMENT);
    }

    // Przesłanie tokenów na adres 0x0
    String totalSupplyStr = stub.getStringState(TOTAL_SUPPLY_KEY.getValue());
    if (stringIsNullOrEmpty(totalSupplyStr)) {
      throw new ChaincodeException("TotalSupply does not exist", NOT_FOUND.toString());
    }
    long totalSupply = Long.parseLong(totalSupplyStr);



    //CompositeKey burnBalanceKey = stub.createCompositeKey(BURN_BALANCE_PREFIX.getValue(), clientMSPID);
    String burnBalanceKey = burnRequest.getBurnWallet();
    String burnBalanceStr = stub.getStringState(burnBalanceKey);
    final Long burnBalance = Long.parseLong(burnBalanceStr);
    if(burnBalance < burnRequestAmount){
      throw new ChaincodeException("Not sufficient amount on burn wallet", NOT_FOUND.toString());
    }
    if(totalSupply < burnRequestAmount){
      throw new ChaincodeException("Not sufficient amount on total supply", NOT_FOUND.toString());
    }

    long updatedTotalSupply = Math.subtractExact(totalSupply, burnRequestAmount);
    stub.putStringState(TOTAL_SUPPLY_KEY.getValue(), String.valueOf(updatedTotalSupply));

    long updatedBurnBalance = Math.subtractExact(burnBalance, burnRequestAmount);
    stub.putStringState(burnBalanceKey, String.valueOf(updatedBurnBalance));

    // Emitowanie zdarzenia finalizacji przepalania
    //stub.setEvent("BurnFinalizedEvent", burnId.getBytes(StandardCharsets.UTF_8));

    // Usuwanie klucza burnId z World State
    stub.delState(burnKey.toString());
  }

  /**
   * Transfer transfers tokens from client account to recipient account. Recipient account must be a
   * valid client Id as returned by the ClientID() function must be a valid clientID as returned by
   * the ClientAccountID() function. This function triggers a Transfer event.
   *
   * @param ctx the transaction context
   * @param to the recipient
   * @param value the amount of token to be transferred
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public void Transfer(final Context ctx, final String to, final long value) {
    // Check contract options are already set first to execute the function
    this.checkInitialized(ctx);
    String from = ctx.getClientIdentity().getId();
    this.transferHelper(ctx, from, to, value);
    final Transfer transferEvent = new Transfer(from, to, value);
    ctx.getStub().setEvent(TRANSFER_EVENT.getValue(), marshalBytes(transferEvent));
  }

  /**
   * BalanceOf returns the balance of the given account.
   *
   * @param ctx the transaction context
   * @param owner the owner from which the balance will be retrieved
   * @return the account balance
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public long BalanceOf(final Context ctx, final String owner) {
    // Check contract options are already set first to execute the function
    this.checkInitialized(ctx);
    ChaincodeStub stub = ctx.getStub();
    CompositeKey balanceKey = stub.createCompositeKey(BALANCE_PREFIX.getValue(), owner);
    String balance = stub.getStringState(balanceKey.toString());
    if (stringIsNullOrEmpty(balance)) {
      String errorMessage = String.format("Balance of the owner  %s not exists", owner);
      throw new ChaincodeException(errorMessage, NOT_FOUND.toString());
    }
    logger.info(String.format("%s has balance of %s tokens", owner, balance));
    return Long.parseLong(balance);
  }

  /**
   * ClientAccountBalance returns the balance of the requesting client's account.
   *
   * @param ctx the transaction context
   * @return client the account balance
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public long ClientAccountBalance(final Context ctx) {
    // Check contract options are already set first to execute the function
    this.checkInitialized(ctx);
    // Get ID of submitting client identity
    ChaincodeStub stub = ctx.getStub();
    String clientAccountID = ctx.getClientIdentity().getId();
    CompositeKey balanceKey = stub.createCompositeKey(BALANCE_PREFIX.getValue(), clientAccountID);
    String balanceBytes = stub.getStringState(balanceKey.toString());
    if (stringIsNullOrEmpty(balanceBytes)) {
      String errorMessage = String.format("The account  %s does not exist", clientAccountID);
      throw new ChaincodeException(errorMessage, NOT_FOUND.toString());
    }
    long balance = Long.parseLong(balanceBytes);
    logger.info(String.format("%s has balance of %d tokens", clientAccountID, balance));
    return balance;
  }

  /**
   * ClientAccountID returns the id of the requesting client's account. In this implementation, the
   * client account ID is the clientId itself. Users can use this function to get their own account
   * id, which they can then give to others as the payment address.
   *
   * @return client account id .
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String ClientAccountID(final Context ctx) {
    // Check contract options are already set first to execute the function
    this.checkInitialized(ctx);
    // Get ID of submitting client identity
    return ctx.getClientIdentity().getId();
  }

  /**
   * Return the total token supply.
   *
   * @param ctx the transaction context
   * @return the total token supply
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public long TotalSupply(final Context ctx) {
    // Check contract options are already set first to execute the function
    this.checkInitialized(ctx);
    String totalSupply = ctx.getStub().getStringState(TOTAL_SUPPLY_KEY.getValue());
    if (stringIsNullOrEmpty(totalSupply)) {
      throw new ChaincodeException("Total Supply  not found", NOT_FOUND.toString());
    }
    logger.info(String.format("TotalSupply: %s tokens", totalSupply));
    return Long.parseLong(totalSupply);
  }

  /**
   * Allows `spender` to spend `value` amount of tokens from the owner.
   *
   * @param ctx the transaction context
   * @param spender The spender
   * @param value The amount of tokens to be approved for transfer
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public void Approve(final Context ctx, final String spender, final long value) {
    // Check contract options are already set first to execute the function
    this.checkInitialized(ctx);
    ChaincodeStub stub = ctx.getStub();
    String owner = ctx.getClientIdentity().getId();
    CompositeKey allowanceKey =
        stub.createCompositeKey(ALLOWANCE_PREFIX.getValue(), owner, spender);
    stub.putStringState(allowanceKey.toString(), String.valueOf(value));
    Approval approval = new Approval(owner, spender, value);
    stub.setEvent(APPROVAL.getValue(), marshalBytes(approval));
    logger.info(
        String.format(
            "client %s approved a withdrawal allowance of %d for spender %s",
            owner, value, spender));
  }

  /**
   * Returns the amount of tokens which `spender` is allowed to withdraw from `owner`.
   *
   * @param ctx the transaction context
   * @param owner The owner of tokens
   * @param spender The spender who are able to transfer the tokens
   * @return the amount of remaining tokens allowed to spent
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public long Allowance(final Context ctx, final String owner, final String spender) {
    // Check contract options are already set first to execute the function
    this.checkInitialized(ctx);
    ChaincodeStub stub = ctx.getStub();
    CompositeKey allowanceKey =
        stub.createCompositeKey(ALLOWANCE_PREFIX.getValue(), owner, spender);
    String allowanceBytes = stub.getStringState(allowanceKey.toString());
    long allowance = 0;
    if (!stringIsNullOrEmpty(allowanceBytes)) {
      allowance = Long.parseLong(allowanceBytes);
    }
    logger.info(
        String.format(
            "The allowance left for spender %s to withdraw from owner %s: %d",
            spender, owner, allowance));
    return allowance;
  }

  /**
   * Transfer `value` amount of tokens from `from` to `to`.
   *
   * @param ctx the transaction context
   * @param from The sender
   * @param to The recipient
   * @param value The amount of token to be transferred
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public void TransferFrom(
      final Context ctx, final String from, final String to, final long value) {
    // Check contract options are already set first to execute the function
    this.checkInitialized(ctx);
    String spender = ctx.getClientIdentity().getId();
    ChaincodeStub stub = ctx.getStub();
    // Retrieve the allowance of the spender
    CompositeKey allowanceKey = stub.createCompositeKey(ALLOWANCE_PREFIX.getValue(), from, spender);
    String currentAllowanceStr = stub.getStringState(allowanceKey.toString());
    if (stringIsNullOrEmpty(currentAllowanceStr)) {
      String errorMessage = String.format("Spender %s has no allowance from %s", spender, from);
      throw new ChaincodeException(errorMessage, NO_ALLOWANCE_FOUND.toString());
    }
    long currentAllowance = Long.parseLong(currentAllowanceStr);
    // Check if the transferred value is less than the allowance
    if (currentAllowance < value) {
      String errorMessage =
          String.format("Spender %s does not have enough allowance to spend", spender);
      throw new ChaincodeException(errorMessage, INSUFFICIENT_FUND.toString());
    }
    this.transferHelper(ctx, from, to, value);
    // Decrease the allowance
    long updatedAllowance = currentAllowance - value;
    stub.putStringState(allowanceKey.toString(), String.valueOf(updatedAllowance));
    final Transfer transferEvent = new Transfer(from, to, value);
    stub.setEvent(TRANSFER_EVENT.getValue(), marshalBytes(transferEvent));
    logger.info(
        String.format(
            "spender %s allowance updated from %d to %d",
            spender, currentAllowance, updatedAllowance));
  }

  /**
   * This is a helper function function that transfers tokens from the "from" address to the "to"
   * address. Dependent functions include Transfer and TransferFrom
   *
   * @param ctx the transaction context
   * @param from the sender
   * @param to the receiver
   * @param value the amount.
   */
  private void transferHelper(
      final Context ctx, final String from, final String to, final long value) {

    if (from.equalsIgnoreCase(to)) {
      throw new ChaincodeException(
          "Cannot transfer to and from same client account", INVALID_TRANSFER.toString());
    }
    // transfer of 0 is allowed in ERC20, so just validate against negative amounts
    if (value < 0) {
      throw new ChaincodeException("Transfer amount cannot be negative", INVALID_AMOUNT.toString());
    }
    ChaincodeStub stub = ctx.getStub();
    // Retrieve the current balance of the sender
    CompositeKey fromBalanceKey = stub.createCompositeKey(BALANCE_PREFIX.getValue(), from);
    String fromCurrentBalanceStr = stub.getStringState(fromBalanceKey.toString());
    if (stringIsNullOrEmpty(fromCurrentBalanceStr)) {
      String errorMessage = String.format("Client account %s has no balance", from);
      throw new ChaincodeException(errorMessage, INSUFFICIENT_FUND.toString());
    }
    long fromCurrentBalance = Long.parseLong(fromCurrentBalanceStr);
    // Check if the sender has enough tokens to spend.
    if (fromCurrentBalance < value) {
      String errorMessage = String.format("Client account %s has insufficient funds", from);
      throw new ChaincodeException(errorMessage, INSUFFICIENT_FUND.toString());
    }
    // Retrieve the current balance of the recipient
    CompositeKey toBalanceKey = stub.createCompositeKey(BALANCE_PREFIX.getValue(), to);
    String toCurrentBalanceStr = stub.getStringState(toBalanceKey.toString());
    long toCurrentBalance = 0;
    // If recipient current balance doesn't yet exist, we'll create it with a
    // current balance of 0
    if (!stringIsNullOrEmpty(toCurrentBalanceStr)) {
      toCurrentBalance = Long.parseLong(toCurrentBalanceStr.trim());
    }
    // Update the balance
    long fromUpdatedBalance = Math.subtractExact(fromCurrentBalance, value);
    long toUpdatedBalance = Math.addExact(toCurrentBalance, value);
    stub.putStringState(fromBalanceKey.toString(), String.valueOf(fromUpdatedBalance));
    stub.putStringState(toBalanceKey.toString(), String.valueOf(toUpdatedBalance));
    logger.info(
        String.format(
            "client %s balance updated from %d to %d",
            from, fromCurrentBalance, fromUpdatedBalance));
    logger.info(
        String.format(
            "recipient %s balance updated from %d to %d", to, toCurrentBalance, toUpdatedBalance));
  }

  /**
   * Initialize the contract with essential token information.
   *
   * This function sets the initial state of the contract, including the token name,
   * symbol, decimals, and the total supply. It must be called once during the
   * chaincode initialization phase using the `--isInit` flag.
   *
   * Preconditions:
   * - The contract must not already be initialized.
   * - The calling client must belong to one of the following organizations:
   *   - `ORG1` = {com.blockchainbiz.erc20.ContractConstants.ORG1}
   *   - `ORG2` = {com.blockchainbiz.erc20.ContractConstants.ORG2}
   *   - `ORG3` = {com.blockchainbiz.erc20.ContractConstants.ORG3}
   *
   * Emits:
   * - `ContractInitialized`: An event containing details of the initialization.
   *
   * @param ctx the transaction context
   * @param name The name of the token
   * @param symbol The symbol of the token
   * @param decimals The number of decimals for the token
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public void Initialize(
      final Context ctx, final String name, final String symbol, final String decimals) {
    ChaincodeStub stub = ctx.getStub();

    // Check minter authorization - this sample assumes Org1 is the central banker with privilege to set Options for these tokens
    String clientMSPID = ctx.getClientIdentity().getMSPID();
    if (!(clientMSPID.equalsIgnoreCase(ORG1.getValue())
            || clientMSPID.equalsIgnoreCase(ORG2.getValue())
            || clientMSPID.equalsIgnoreCase(ORG3.getValue()))) {
      throw new ChaincodeException(
          "Client is not authorized to initialize contract", UNAUTHORIZED_SENDER.toString());
    }

    // Check contract options are not already set, client is not authorized to change them once intitialized
    String tokenName = stub.getStringState(ContractConstants.NAME_KEY.getValue());
    if (!stringIsNullOrEmpty(tokenName)) {
      throw new ChaincodeException("contract options are already set, client is not authorized to change them");
    }
    // Walidacja danych wejściowych
    if (stringIsNullOrEmpty(name) || stringIsNullOrEmpty(symbol) || stringIsNullOrEmpty(decimals)) {
      throw new ChaincodeException("All initialization parameters (name, symbol, decimals, initialTotalSupply) must be provided.", INVALID_ARGUMENT.toString());
    }

    stub.putStringState(NAME_KEY.getValue(), name);
    stub.putStringState(SYMBOL_KEY.getValue(), symbol);
    stub.putStringState(DECIMALS_KEY.getValue(), decimals);

    stub.putStringState(TOTAL_SUPPLY_KEY.getValue(), "0");

    String policy = "AND('YachtSales.member', 'FurnituresMakers.member', 'WoodSupply.member')";
    stub.setStateValidationParameter(TOTAL_SUPPLY_KEY.getValue(), policy.getBytes(UTF_8));

    // Emitowanie zdarzenia inicjalizacji
    String initEventPayload = String.format(
            "{\"totalSupply\":%d,\"name\":\"%s\",\"symbol\":\"%s\",\"decimals\":\"%s\"}",
            Integer.valueOf(0), name, symbol, decimals
    );
    stub.setEvent("InitEvent", initEventPayload.getBytes(StandardCharsets.UTF_8));

    logger.info("Contract initialized with totalSupply: " + 0 + ", name: " + name + ", symbol: " + symbol + ", decimals: " + decimals);
  }

  /**
   * Return the name of the token - e.g. "MyToken". The original function name is `name` in ERC20
   * specification. However, 'name' conflicts with a parameter `name` in `Contract` class. As a work
   * around, we use `TokenName` as an alternative function name.
   *
   * @param ctx the transaction context
   * @return the name of the token
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String TokenName(final Context ctx) {
    // Check contract options are already set first to execute the function
    this.checkInitialized(ctx);
    String tokenName = ctx.getStub().getStringState(ContractConstants.NAME_KEY.getValue());
    if (stringIsNullOrEmpty(tokenName)) {
      throw new ChaincodeException("Token name not found", NOT_FOUND.toString());
    }
    return tokenName;
  }

  /**
   * Return the symbol of the token.
   *
   * @param ctx the transaction context
   * @return the symbol of the token
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public String TokenSymbol(final Context ctx) {
    // Check contract options are already set first to execute the function
    this.checkInitialized(ctx);
    String tokenSymbol = ctx.getStub().getStringState(SYMBOL_KEY.getValue());
    if (stringIsNullOrEmpty(tokenSymbol)) {
      throw new ChaincodeException("Token symbol not found", NOT_FOUND.toString());
    }
    return tokenSymbol;
  }

  /**
   * Return the number of decimals the token uses e.g. 8, means to divide the token amount by
   * 100000000 to get its user representation.
   *
   * @param ctx the transaction context
   * @return the number of decimals
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public int Decimals(final Context ctx) {
    // Check contract options are already set first to execute the function
    this.checkInitialized(ctx);
    String decimals = ctx.getStub().getStringState(DECIMALS_KEY.getValue());
    if (stringIsNullOrEmpty(decimals)) {
      throw new ChaincodeException("Decimal not found", NOT_FOUND.toString());
    }
    return Integer.parseInt(decimals);
  }
  /**
   * Checks that contract options have been already initialized
   *
   * @param ctx the transaction context
   * @return the number of decimals
   */
  private void checkInitialized(final Context ctx) {
    String tokenName = ctx.getStub().getStringState(ContractConstants.NAME_KEY.getValue());
    if (stringIsNullOrEmpty(tokenName)) {
      throw new ChaincodeException("Contract options need to be set before calling any function, call Initialize() to initialize contract", NOT_FOUND.toString());
    }
  }
  /**
   * Checks if a transaction ID has already been used. This prevents double-spending or
   * reprocessing of the same transaction. Operates within the atomicity guarantees of Fabric.
   * @param stub Object to manage transaction context
   * @param txId id of external transaction which indicates to bank income
   */
  private boolean wasAlreadyUsed(final ChaincodeStub stub, final String txId) {
    String key = USED_TRANSACTIONS_PREFIX.getValue() + txId;
    String usedValue = stub.getStringState(key);
    return usedValue != null && usedValue.equals("used");
  }
  /**
   * Checks if a transaction ID has already been used. This prevents double-spending or
   * reprocessing of the same transaction. Operates within the atomicity guarantees of Fabric.
   * @param stub Object to manage transaction context
   * @param txId id of external transaction which indicates to bank income
   */
  private void markAsUsed(final ChaincodeStub stub, final String txId) {
    String key = USED_TRANSACTIONS_PREFIX.getValue() + txId;
    stub.putStringState(key, "used");
  }
  private void markAsWaiting(final ChaincodeStub stub, final String txId) {
    String key = USED_TRANSACTIONS_PREFIX.getValue() + txId;
    stub.putStringState(key, "waiting");
  }
}
