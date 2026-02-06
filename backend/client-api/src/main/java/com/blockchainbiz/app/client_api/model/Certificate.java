package com.blockchainbiz.app.client_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    //@Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String certificate;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
