package com.blockchainbiz.app.client_api.service;


import com.blockchainbiz.app.client_api.model.RefreshToken;
import com.blockchainbiz.app.client_api.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(String username, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .username(username)
                .token(token)
                .expiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60)) // 7 dni
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    public boolean isRefreshTokenValid(String username, String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        return refreshToken.isPresent() && refreshToken.get().getUsername().equals(username);
    }

    public void revokeRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}