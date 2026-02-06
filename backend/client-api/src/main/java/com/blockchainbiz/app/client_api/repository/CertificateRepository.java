package com.blockchainbiz.app.client_api.repository;

import com.blockchainbiz.app.client_api.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByUserId(Long userId);
}