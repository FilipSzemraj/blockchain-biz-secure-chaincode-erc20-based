package com.blockchainbiz.app.network_api.controller;

import com.blockchainbiz.app.network_api.service.BlockchainEventService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/events")
public class BlockchainEventController {

    private final BlockchainEventService blockchainEventService;

    public BlockchainEventController(BlockchainEventService blockchainEventService) {
        this.blockchainEventService = blockchainEventService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamEvents() {
        return blockchainEventService.getEventStream();
    }
}