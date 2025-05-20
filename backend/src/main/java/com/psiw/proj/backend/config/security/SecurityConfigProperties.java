package com.psiw.proj.backend.config.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableConfigurationProperties(RSAKeyRecord.class)
@Profile("!test")
public class SecurityConfigProperties {
}
