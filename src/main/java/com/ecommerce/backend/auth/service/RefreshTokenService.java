package com.ecommerce.backend.auth.service;

import com.ecommerce.backend.common.config.JwtProperties;
import com.ecommerce.backend.common.exception.ApiException;
import com.ecommerce.backend.common.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
  private static final String KEY_PREFIX = "refresh_token:";

  private final StringRedisTemplate redisTemplate;
  private final JwtProperties jwtProperties;

  public void store(String email, String refreshToken) {
    redisTemplate
        .opsForValue()
        .set(
            KEY_PREFIX + hash(refreshToken),
            email,
            Duration.ofMillis(jwtProperties.refreshToken().expiration()));
  }

  public String getEmail(String refreshToken) {
    String email = redisTemplate.opsForValue().get(KEY_PREFIX + hash(refreshToken));
    if (email == null) {
      throw new ApiException(ErrorCode.INVALID_TOKEN, "Refresh token is invalid or expired");
    }
    return email;
  }

  public void revoke(String refreshToken) {
    redisTemplate.delete(KEY_PREFIX + hash(refreshToken));
  }

  private String hash(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
