package com.blockchainbiz.app.network_api.controller;

import com.blockchainbiz.app.network_api.dto.QueryRequest;
import com.blockchainbiz.app.network_api.service.BlockchainService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.client.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/blockchain")
public class BlockchainController {

    private final BlockchainService blockchainService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public BlockchainController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    @PostMapping("/query")
    public ResponseEntity<String> queryState(@RequestBody QueryRequest request)
            throws GatewayException, EndorseException, SubmitException, CommitStatusException, CommitException {

        System.out.println("REQUEST BODY:");
        System.out.println("Channel: " + request.getChannelName());
        System.out.println("Chaincode: " + request.getChaincodeName());
        System.out.println("Function: " + request.getFunctionName());
        System.out.println("Args: " + request.getArgs());


        String result = blockchainService.queryState(
                request.getChannelName(),
                request.getChaincodeName(),
                request.getFunctionName(),
                request.getArgs()
        );

        return ResponseEntity.ok(result);

    }

    @PostMapping("/submit")
    public String submitTransaction(@RequestParam String channelName,
                                    @RequestParam String chaincodeName,
                                    @RequestParam String functionName,
                                    @RequestParam String[] args)
            throws GatewayException, EndorseException, SubmitException, CommitStatusException, CommitException {
        return blockchainService.submitTransaction(channelName, chaincodeName, functionName, args);
    }


}
