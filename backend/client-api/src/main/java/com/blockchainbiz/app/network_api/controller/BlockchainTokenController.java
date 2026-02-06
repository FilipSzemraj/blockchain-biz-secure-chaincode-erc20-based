package com.blockchainbiz.app.network_api.controller;

import com.blockchainbiz.app.network_api.dto.token.*;
import com.blockchainbiz.app.network_api.model.MintResponse;
import com.blockchainbiz.app.network_api.service.BlockchainService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/blockchain")
@Slf4j
public class BlockchainTokenController {

    private final BlockchainService blockchainService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public BlockchainTokenController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    /**
     * Helper method that queries the blockchain using the given function name,
     * deserializes the JSON response into a list of BurnWithHash objects, and builds a BurnResponse.
     */
    private BurnResponse getBurnResponse(String channelName, String chaincodeName, String chaincodeFunction)
            throws JsonProcessingException, GatewayException, EndorseException, SubmitException,
            CommitStatusException, CommitException {

        // Query the chaincode function
        String result = blockchainService.queryState(channelName, chaincodeName, chaincodeFunction, "[]");

        // Deserialize the outer JSON array as a List of Strings
        List<String> jsonStrings = objectMapper.readValue(result, new TypeReference<List<String>>() {});

        // Convert each JSON string into a BurnWithHash object
        List<BurnWithHash> burnList = jsonStrings.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, BurnWithHash.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to deserialize burn JSON: " + json, e);
                    }
                })
                .collect(Collectors.toList());

        // Build and return the BurnResponse object
        return new BurnResponse(burnList);
    }

    /**
     * MINT
     * Corresponds to chaincode function:
     *   public void Mint(final Context ctx, final String jsonContent)
     */
    @PostMapping("/mint")
    public ResponseEntity<?> mint(
            @RequestParam String channelName,
            @RequestParam String chaincodeName,
            @RequestBody MintRequest request) {
        log.info(">>> Incoming raw POST body: {}", request.getJsonContent());
        try {
            // The chaincode function: Mint(jsonContent)
            String[] args = new String[] {
                    request.getJsonContent()
            };

            String result = blockchainService.submitTransaction(
                    channelName,
                    chaincodeName,
                    "Mint",
                    args
            );
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error in Mint:", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * BURN
     * Corresponds to chaincode function:
     *   public String Burn(final Context ctx, final String amountStr)
     *   (this function returns a String from chaincode)
     */
    @PostMapping("/burn")
    public ResponseEntity<?> burn(@RequestParam String channelName,
                                  @RequestParam String chaincodeName,
                                  @RequestBody BurnInvocationRequest request) {
        try {
            // The chaincode function: Burn(amountStr)
            String[] args = new String[] {
                    request.getAmountStr()
            };

            String result = blockchainService.submitTransaction(
                    channelName,
                    chaincodeName,
                    "Burn",
                    args
            );
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error in Burn:", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * FINALIZE BURN
     * Corresponds to chaincode function:
     *   public void FinalizeBurn(final Context ctx, final String jsonContent)
     */
    @PostMapping("/finalizeBurn")
    public ResponseEntity<?> finalizeBurn(@RequestParam String channelName,
                                          @RequestParam String chaincodeName,
                                          @RequestBody FinalizeBurnRequest request) {
        try {
            // The chaincode function: FinalizeBurn(jsonContent)
            String[] args = new String[] {
                    request.getJsonContent()
            };

            String result = blockchainService.submitTransaction(
                    channelName,
                    chaincodeName,
                    "FinalizeBurn",
                    args
            );
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error in FinalizeBurn:", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * TRANSFER
     * Corresponds to chaincode function:
     *   public void Transfer(final Context ctx, final String to, final long value)
     */
    @PostMapping("/transfer/send")
    public ResponseEntity<?> transfer(@RequestParam String channelName,
                                      @RequestParam String chaincodeName,
                                      @RequestParam String to,
                                      @RequestParam long value) {
        try {
            String result = blockchainService.submitTransaction(
                    channelName,
                    chaincodeName,
                    "Transfer",
                    new String[]{to, String.valueOf(value)}
            );
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error in Transfer:", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/transfer/history")
    public ResponseEntity<?> getTransferHistory(@RequestParam String channelName,
                                                @RequestParam String chaincodeName) {
        try {
            String result = blockchainService.queryState(
                    channelName,
                    chaincodeName,
                    "GetTransferHistory"
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error retrieving transfer history:", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/getBankIBAN")
    public ResponseEntity<String> getBankIBAN(@RequestParam String channelName,
                                              @RequestParam String chaincodeName)
            throws GatewayException, EndorseException, SubmitException, CommitStatusException, CommitException {
        String result = blockchainService.queryState(
                channelName,
                chaincodeName,
                "getBankIBAN",
                "[]"
        );

        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint to retrieve the list of minted transaction indexes from the blockchain.
     *
     * @return A ResponseEntity containing a list of minted transaction indexes.
     */
    @GetMapping("/mintIndexList")
    public ResponseEntity<List<String>> getMintIndexList(@RequestParam String channelName,
                                                    @RequestParam String chaincodeName)
            throws GatewayException, EndorseException, SubmitException, CommitStatusException, CommitException, JsonProcessingException {
        log.info(">>> getMintList invocation");
        String result = blockchainService.queryState(
                channelName,
                chaincodeName,
                "getMintList",
                "[]"
        );

        List<String> mintJsonStrings = objectMapper.readValue(result, new TypeReference<List<String>>() {});

        List<ConfirmationWithHashDTO> mintList = mintJsonStrings.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, ConfirmationWithHashDTO.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to deserialize mint JSON: " + json, e);
                    }
                })
                .collect(Collectors.toList());


        List<String> mintIndexes = mintList.stream()
                .map(ConfirmationWithHashDTO::getHash)
                .collect(Collectors.toList());

        return ResponseEntity.ok(mintIndexes);
    }

    /**
     * Endpoint to retrieve the list of minted transaction from the blockchain.
     *
     * @return A ResponseEntity containing a list of minted transaction indexes.
     */
    @GetMapping("/mintList")
    public ResponseEntity<Object> getMintList(@RequestParam String channelName,
                                              @RequestParam String chaincodeName)
            throws GatewayException, EndorseException, SubmitException, CommitStatusException, CommitException, JsonProcessingException {
        log.info(">>> getMintList invocation");
        String result = blockchainService.queryState(
                channelName,
                chaincodeName,
                "getMintList",
                "[]"
        );

        List<String> mintJsonStrings = objectMapper.readValue(result, new TypeReference<List<String>>() {});

        List<ConfirmationWithHashDTO> mintList = mintJsonStrings.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, ConfirmationWithHashDTO.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to deserialize mint JSON: " + json, e);
                    }
                })
                .collect(Collectors.toList());

        MintResponse response = new MintResponse(mintList);

        Link selfLink = WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(BlockchainTokenController.class)
                        .getMintList(channelName, chaincodeName)
        ).withSelfRel();
        response.add(selfLink);


        return ResponseEntity.ok(response);
    }



    @GetMapping("/finalizedBurnList")
    public ResponseEntity<BurnResponse> getFinalizedBurnList(
            @RequestParam String channelName,
            @RequestParam String chaincodeName)
            throws GatewayException, EndorseException, SubmitException,
            CommitStatusException, CommitException, JsonProcessingException {

        log.info(">>> getFinalizedBurnList invocation");

        BurnResponse response = getBurnResponse(channelName, chaincodeName, "getFinalizedBurnList");

        Link selfLink = WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(BlockchainTokenController.class)
                        .getFinalizedBurnList(channelName, chaincodeName)
        ).withSelfRel();
        response.add(selfLink);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pendingBurnList")
    public ResponseEntity<BurnResponse> getPendingBurnList(
            @RequestParam String channelName,
            @RequestParam String chaincodeName)
            throws GatewayException, EndorseException, SubmitException,
            CommitStatusException, CommitException, JsonProcessingException {

        log.info(">>> getPendingBurnList invocation");

        BurnResponse response = getBurnResponse(channelName, chaincodeName, "getPendingBurnList");

        Link selfLink = WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(BlockchainTokenController.class)
                        .getPendingBurnList(channelName, chaincodeName)
        ).withSelfRel();
        response.add(selfLink);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/pendingBurnTransaction")
    public ResponseEntity<String> getPendingBurnTransaction(
            @RequestParam String channelName,
            @RequestParam String chaincodeName,
            @RequestParam String burnRequestHash)
            throws GatewayException, EndorseException, SubmitException,
            CommitStatusException, CommitException, JsonProcessingException {

        log.info(">>> getPendingBurnTransaction invocation");

        String pendingBurnTxJson = blockchainService.queryState(
                channelName,
                chaincodeName,
                "getPendingBurnTransaction",
                burnRequestHash
        );

        return ResponseEntity.ok(pendingBurnTxJson);
    }
    @GetMapping("/finalizedBurnTransaction")
    public ResponseEntity<String> getFinalizedBurnTransaction(
            @RequestParam String channelName,
            @RequestParam String chaincodeName,
            @RequestParam String burnRequestHash)
            throws GatewayException, EndorseException, SubmitException,
            CommitStatusException, CommitException, JsonProcessingException {

        log.info(">>> getFinalizedBurnTransaction invocation");

        String finalizedBurnTxJson = blockchainService.queryState(
                channelName,
                chaincodeName,
                "getFinalizedBurnTransaction",
                burnRequestHash
        );

        return ResponseEntity.ok(finalizedBurnTxJson);
    }

    @GetMapping("/pendingBurnBody")
    public ResponseEntity<String> getPendingBurnBody(
            @RequestParam String channelName,
            @RequestParam String chaincodeName,
            @RequestParam String burnRequestHash)
            throws GatewayException, EndorseException, SubmitException,
            CommitStatusException, CommitException, JsonProcessingException {

        log.info(">>> getPendingBurnBody invocation");

        String pendingBurnBodyJson = blockchainService.queryState(
                channelName,
                chaincodeName,
                "getPendingBurnBody",
                burnRequestHash
        );

        return ResponseEntity.ok(pendingBurnBodyJson);
    }

    @GetMapping("/finalizedBurnBody")
    public ResponseEntity<String> getFinalizedBurnBody(
            @RequestParam String channelName,
            @RequestParam String chaincodeName,
            @RequestParam String burnRequestHash)
            throws GatewayException, EndorseException, SubmitException,
            CommitStatusException, CommitException, JsonProcessingException {

        log.info(">>> getFinalizedBurnBody invocation");

        String finalizedBurnBodyJson = blockchainService.queryState(
                channelName,
                chaincodeName,
                "getFinalizedBurnBody",
                burnRequestHash
        );

        return ResponseEntity.ok(finalizedBurnBodyJson);
    }
}