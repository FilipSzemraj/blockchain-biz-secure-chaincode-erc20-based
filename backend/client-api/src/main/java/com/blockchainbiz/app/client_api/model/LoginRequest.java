package com.blockchainbiz.app.client_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data

@RequiredArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
}
