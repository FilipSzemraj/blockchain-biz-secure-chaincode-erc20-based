package com.blockchainbiz.app.network_api.service;

import org.hyperledger.fabric.client.ChaincodeEvent;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class BlockchainEventService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainEventService.class);
    private final Sinks.Many<String> eventSink;
    private final Gateway gateway;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public BlockchainEventService(Gateway gateway) {
        this.gateway = gateway;
        this.eventSink = Sinks.many().multicast().onBackpressureBuffer();
        startListening();
    }

    private void startListening() {
        executorService.execute(() -> {
            try {
                Network network = gateway.getNetwork("yfw-channel");
                Flux.create(sink -> {
                    network.getChaincodeEvents("basic").forEachRemaining(event -> {
                        String eventData = new String(event.getPayload());
                        String eventName = event.getEventName();
                        log.info("Received Chaincode Event: {} -> {}", event.getEventName(), eventData);

                        String payload = String.format("{\"eventName\":\"%s\", \"data\": %s}", eventName, eventData);
                        eventSink.tryEmitNext(payload);
                    });
                }).subscribe();
                log.info("Listening for Hyperledger Fabric chaincode events...");
            } catch (Exception e) {
                log.error("Error setting up Fabric chaincode event listener", e);
                reconnect();
            }
        });
    }

    private void reconnect() {
        log.warn("Reconnecting to Fabric network...");
        try {
            Thread.sleep(5000); // Wait 5 seconds before reconnecting
            startListening();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Flux<String> getEventStream() {
        String welcomeJson = "{\"eventName\": \"InitEvent\", \"data\": \"Welcome! You are connected to the blockchain event stream.\"}";

        return eventSink.asFlux()
                            .startWith(welcomeJson);

    }
}