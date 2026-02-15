package com.blockchainbiz.app.client_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class LoginResponse {
    private String token;
    private Long id;

    public LoginResponse(String token, Long id) {
        this.token = token;
        this.id = id;
    }

}
