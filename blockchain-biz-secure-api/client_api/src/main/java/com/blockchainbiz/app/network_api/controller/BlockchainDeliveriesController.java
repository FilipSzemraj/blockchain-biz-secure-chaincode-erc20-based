package com.blockchainbiz.app.network_api.controller;

import com.blockchainbiz.app.network_api.dto.deliveries.*;
import com.blockchainbiz.app.network_api.model.DeliveriesResponse;
import com.blockchainbiz.app.network_api.model.Delivery;
import com.blockchainbiz.app.network_api.service.BlockchainService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/blockchain")
public class BlockchainDeliveriesController {

    private final BlockchainService blockchainService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public BlockchainDeliveriesController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }


    @GetMapping("/queryDeliveries")
    public ResponseEntity<Object> queryDeliveries(
            @RequestParam String channelName,
            @RequestParam String chaincodeName
    ) {
        try {
            String functionName = "GetAllDeliveries";
            String[] args = new String[0];



            System.out.println("REQUEST PARAMETERS:");
            System.out.println("Channel: " + channelName);
            System.out.println("Chaincode: " + chaincodeName);
            System.out.println("Function: " + functionName);
            System.out.println("Args: " + (args != null ? args : "[]"));

            String result = blockchainService.submitTransaction(
                    channelName,
                    chaincodeName,
                    functionName,
                    args
            );


            List<Delivery> deliveries = objectMapper.readValue(result, new TypeReference<List<Delivery>>() {});
            DeliveriesResponse response = new DeliveriesResponse(deliveries);

            Link selfLink = WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(BlockchainDeliveriesController.class)
                            .queryDeliveries(channelName, chaincodeName)
            ).withSelfRel();
            response.add(selfLink);

            return ResponseEntity.ok(response);

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            return ResponseEntity.badRequest().body(DeliveriesResponse.fromError("Invalid JSON response: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(DeliveriesResponse.fromError("Internal Server Error: " + e.getMessage()));
        }
    }

    @PostMapping("/createDelivery")
    public ResponseEntity<?> createDelivery(@RequestBody CreateDeliveryRequest request) {
        try {
            String expiryDateString = request.getExpiryTimestamp();
            if (expiryDateString == null || expiryDateString.isEmpty()) {
                return ResponseEntity.badRequest().body("expiryTimestamp is required.");
            }


            String rawDataJson = null;
            if (request.getRawData() != null) {
                rawDataJson = new ObjectMapper().writeValueAsString(request.getRawData());
            } else {
                rawDataJson = "{}";
            }

            // 3. Construct the argument array
            String[] transactionArgs = new String[] {
                    request.getDeliveryId(),
                    request.getBuyerId(),
                    request.getSellerId(),
                    request.getArbitratorId(),
                    String.valueOf(request.getTokenAmount()),
                    request.getGoodsType(),
                    request.getGoodsDetails(),
                    String.valueOf(request.getGoodsQuantity()),
                    expiryDateString,
                    rawDataJson,
            };

            String result = blockchainService.submitTransaction(
                    request.getChannelName(),
                    request.getChaincodeName(),
                    "CreateDelivery",
                    transactionArgs
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creating delivery: " + e.getMessage());
        }
    }

    /**
     * START DELIVERY
     * Corresponds to chaincode function:
     *     public void StartDelivery(Context ctx, String deliveryId)
     */
    @PostMapping("/startDelivery")
    public ResponseEntity<?> startDelivery(@RequestBody StartDeliveryRequest request) {
        try {
            // Chaincode expects: StartDelivery(String deliveryId)
            String[] args = new String[] {
                    request.getDeliveryId()
            };

            String result = blockchainService.submitTransaction(
                    request.getChannelName(),
                    request.getChaincodeName(),
                    "StartDelivery",
                    args
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in startDelivery: ", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * ADD PARTIAL DELIVERY
     * Corresponds to chaincode function:
     *   public void AddPartialDelivery(Context ctx, String deliveryId, String goodsType,
     *                                  String goodsDetails, long goodsQuantity,
     *                                  Date expiryTimestamp, Map<String, Object> rawData)
     */
    @PostMapping("/addPartialDelivery")
    public ResponseEntity<?> addPartialDelivery(@RequestBody AddPartialDeliveryRequest request) {
        try {
            // Convert rawData to JSON
            String rawDataJson = "{}";
            if (request.getRawData() != null) {
                rawDataJson = objectMapper.writeValueAsString(request.getRawData());
            }

            String[] args = new String[] {
                    request.getDeliveryId(),
                    request.getGoodsType(),
                    request.getGoodsDetails(),
                    String.valueOf(request.getGoodsQuantity()),
                    request.getExpiryTimestamp(),
                    rawDataJson
            };

            String result = blockchainService.submitTransaction(
                    request.getChannelName(),
                    request.getChaincodeName(),
                    "AddPartialDelivery",
                    args
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in addPartialDelivery: ", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }



    /**
     * RESOLVE PARTIAL DELIVERY DISPUTE
     * Corresponds to chaincode function:
     *  public void ResolvePartialDeliveryDispute(Context ctx, String deliveryId,
     *                                            String partialDeliveryId,
     *                                            long arbitratorAcceptedQuantity)
     */
    @PostMapping("/resolvePartialDeliveryDispute")
    public ResponseEntity<?> resolvePartialDeliveryDispute(
            @RequestBody ResolvePartialDeliveryDisputeRequest request) {
        try {
            // Chaincode expects: (deliveryId, partialDeliveryId, arbitratorAcceptedQuantity)
            String[] args = new String[] {
                    request.getDeliveryId(),
                    request.getPartialDeliveryId(),
                    String.valueOf(request.getArbitratorAcceptedQuantity())
            };

            String result = blockchainService.submitTransaction(
                    request.getChannelName(),
                    request.getChaincodeName(),
                    "ResolvePartialDeliveryDispute",
                    args
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in resolvePartialDeliveryDispute: ", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * CONFIRM PARTIAL DELIVERY
     * Corresponds to chaincode function:
     *   public void ConfirmPartialDelivery(Context ctx,
     *                                      String deliveryId,
     *                                      String partialDeliveryId,
     *                                      long acceptedQuantity)
     */
    @PostMapping("/confirmPartialDelivery")
    public ResponseEntity<?> confirmPartialDelivery(
            @RequestBody ConfirmPartialDeliveryRequest request) {
        try {
            // Chaincode expects: (deliveryId, partialDeliveryId, acceptedQuantity)
            String[] args = new String[] {
                    request.getDeliveryId(),
                    request.getPartialDeliveryId(),
                    String.valueOf(request.getAcceptedQuantity())
            };

            String result = blockchainService.submitTransaction(
                    request.getChannelName(),
                    request.getChaincodeName(),
                    "ConfirmPartialDelivery",
                    args
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in confirmPartialDelivery: ", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * CONFIRM DELIVERY
     * Corresponds to chaincode function:
     *   public void ConfirmDelivery(Context ctx, String deliveryId)
     */
    @PostMapping("/confirmDelivery")
    public ResponseEntity<?> confirmDelivery(@RequestBody ConfirmDeliveryRequest request) {
        try {
            // Chaincode expects: (deliveryId)
            String[] args = new String[] {
                    request.getDeliveryId()
            };

            String result = blockchainService.submitTransaction(
                    request.getChannelName(),
                    request.getChaincodeName(),
                    "ConfirmDelivery",
                    args
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in confirmDelivery: ", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * CANCEL PARTIAL DELIVERY
     * Corresponds to chaincode function:
     *   public void CancelPartialDelivery(Context ctx, String deliveryId, String partialDeliveryId)
     */
    @PostMapping("/cancelPartialDelivery")
    public ResponseEntity<?> cancelPartialDelivery(@RequestBody CancelPartialDeliveryRequest request) {
        try {
            // Chaincode expects: (deliveryId, partialDeliveryId)
            String[] args = new String[] {
                    request.getDeliveryId(),
                    request.getPartialDeliveryId()
            };

            String result = blockchainService.submitTransaction(
                    request.getChannelName(),
                    request.getChaincodeName(),
                    "CancelPartialDelivery",
                    args
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in cancelPartialDelivery: ", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * CANCEL DELIVERY
     * Corresponds to chaincode function:
     *   public void CancelDelivery(Context ctx, String deliveryId)
     */
    @PostMapping("/cancelDelivery")
    public ResponseEntity<?> cancelDelivery(@RequestBody CancelDeliveryRequest request) {
        try {
            // Chaincode expects: (deliveryId)
            String[] args = new String[] {
                    request.getDeliveryId()
            };

            String result = blockchainService.submitTransaction(
                    request.getChannelName(),
                    request.getChaincodeName(),
                    "CancelDelivery",
                    args
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in cancelDelivery: ", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
