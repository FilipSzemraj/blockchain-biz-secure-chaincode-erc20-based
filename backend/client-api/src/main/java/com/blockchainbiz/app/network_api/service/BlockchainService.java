package com.blockchainbiz.app.network_api.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class BlockchainService {

    private final Gateway.Builder gatewayBuilder;

    @Autowired
    public BlockchainService(Gateway.Builder gatewayBuilder) {
        this.gatewayBuilder = gatewayBuilder;
    }

    public String queryState(String channelName, String chaincodeName, String functionName, String... args)
            throws GatewayException, EndorseException, SubmitException, CommitStatusException, CommitException {
        try (Gateway gateway = gatewayBuilder.connect()) {
            Network network = gateway.getNetwork(channelName);
            Contract contract = network.getContract(chaincodeName);

            byte[] result = contract.evaluateTransaction(functionName, args);
            return new String(result);
        }
    }


    public String submitTransaction(String channelName, String chaincodeName, String functionName, String... args)
            throws GatewayException, EndorseException, SubmitException, CommitStatusException, CommitException {
        try (Gateway gateway = gatewayBuilder.connect()) {
            Network network = gateway.getNetwork(channelName);
            Contract contract = network.getContract(chaincodeName);
            Proposal proposal = contract.newProposal(functionName)
                    .addArguments(args)
                    .setEndorsingOrganizations("YachtSales", "WoodSupply", "FurnituresMakers")
                    .build();
            Transaction endorsedTransaction = proposal.endorse();

            byte[] result = endorsedTransaction.submit();
            return new String(result);
        }
    }


}