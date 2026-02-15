package com.blockchainbiz.app.network_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class QueryRequest {
    private String channelName;
    private String chaincodeName;
    private String functionName;
    private String[] args;
}