package com.vannchhai.authservice.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    String secret, long accessTokenTtlSeconds, long refreshTokenTtlSeconds, String issuer) {}
