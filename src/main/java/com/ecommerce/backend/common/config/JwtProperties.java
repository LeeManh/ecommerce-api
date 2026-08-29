package com.ecommerce.backend.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(TokenConfig accessToken, TokenConfig refreshToken) {
  public record TokenConfig(String secret, long expiration) {}
}
