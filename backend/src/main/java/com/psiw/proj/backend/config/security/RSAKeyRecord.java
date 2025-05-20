package com.psiw.proj.backend.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "spring.security.oauth2.resource-server.jwt")
public record RSAKeyRecord(
        RSAPublicKey publicKey,
        RSAPrivateKey privateKey
) {
}