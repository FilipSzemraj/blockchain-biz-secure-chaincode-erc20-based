package com.blockchainbiz.app.client_api.repository;

import com.blockchainbiz.app.client_api.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    boolean existsByToken(String token);
}
