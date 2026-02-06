package com.blockchainbiz.app.network_api.config;

import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.identity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@Slf4j
@Configuration
public class FabricConfig {

    @Value("${fabric.msp.id}")
    private String mspId;

    @Value("${fabric.gateway.address}")
    private String gatewayAddress;

    @Value("${fabric.cert.path}")
    private String certPathProp;

    @Value("${fabric.key.path}")
    private String keyPathProp;

    @Value("${fabric.tls.cert.ca.path}")
    private String tlsCertCaPathProp;

    @Value("${fabric.tls.cert.path}")
    private String tlsCertPathProp;

    @Value("${fabric.tls.key.path}")
    private String tlsKeyPathProp;

    @Bean
    public Gateway.Builder gatewayBuilder() throws Exception {
        Path certPath = Paths.get(certPathProp);
        Path keyPath  = Paths.get(keyPathProp);
        Path tlsCertCaPath = Paths.get(tlsCertCaPathProp);
        Path tlsCertPath = Paths.get(tlsCertPathProp);
        Path tlsKeyPath = Paths.get(tlsKeyPathProp);

        // Tworzenie tożsamości klienta
        X509Certificate certificate = Identities.readX509Certificate(Files.newBufferedReader(certPath));
        Identity identity = new X509Identity(mspId, certificate);

        // Tworzenie implementacji podpisywania
        PrivateKey privateKey = Identities.readPrivateKey(Files.newBufferedReader(keyPath));
        Signer signer = Signers.newPrivateKeySigner(privateKey);

        // Tworzenie połączenia gRPC
        ChannelCredentials tlsCredentials = TlsChannelCredentials.newBuilder()
                .trustManager(tlsCertCaPath.toFile())
                .keyManager(tlsCertPath.toFile(), tlsKeyPath.toFile())
                .build();
        ManagedChannel grpcChannel = Grpc.newChannelBuilder(gatewayAddress, tlsCredentials).build();

        // Zwracanie konfiguracji Gateway.Builder jako beana
        return Gateway.newInstance()
                .identity(identity)
                .signer(signer)
                .connection(grpcChannel);
    }

    /**
     * Bean for Gateway - Used in BlockchainEventService for a persistent event listener.
     */
    @Bean
    public Gateway gateway(Gateway.Builder builder) {
        return builder.connect();
    }
}