package com.blockchainbiz.app.client_api.exception;

import com.blockchainbiz.app.client_api.dto.ErrorResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.client.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<String> handleGrpcException(StatusRuntimeException ex) {
        Status status = ex.getStatus();
        String message = status.getDescription();

        if (message != null && message.contains("Delivery not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + message);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("gRPC error: " + message);
    }

    @ExceptionHandler(EndorseException.class)
    public ResponseEntity<String> handleEndorseException(EndorseException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Blockchain endorsement failed: " + ex.getMessage());
    }

    @ExceptionHandler({SubmitException.class, CommitException.class, CommitStatusException.class})
    public ResponseEntity<String> handleTransactionException(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Blockchain transaction failed: " + ex.getMessage());
    }

    @ExceptionHandler(GatewayException.class)
    public ResponseEntity<String> handleGatewayException(GatewayException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Blockchain gateway error: " + ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error: " + ex.getMessage());
    }


}
