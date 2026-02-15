package com.blockchainbiz.app.client_api.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {
    private final HttpStatus status;
    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status=status;
    }
    public HttpStatus getStatus(){
        return status;
    }
    public CustomException(String message, Throwable cause) {
        super(message, cause);
        this.status=HttpStatus.NOT_FOUND;
    }
    public CustomException(String message) {
        super(message);
        this.status=HttpStatus.NOT_FOUND;
    }
}
