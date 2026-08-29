package com.ecommerce.backend.auth.service;

import com.ecommerce.backend.common.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

  private final JwtProperties jwtProperties;

  public String generateAccessToken(String email) {
    return generateToken(email, accessTokenKey(), jwtProperties.accessToken().expiration());
  }

  public String generateRefreshToken(String email) {
    return generateToken(email, refreshTokenKey(), jwtProperties.refreshToken().expiration());
  }

  public String extractEmailFromAccessToken(String token) {
    return extractClaims(token, accessTokenKey()).getSubject();
  }

  public boolean isAccessTokenValid(String token) {
    return isTokenValid(token, accessTokenKey());
  }

  public boolean isRefreshTokenValid(String token) {
    return isTokenValid(token, refreshTokenKey());
  }

  private String generateToken(String subject, SecretKey key, long expirationMs) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationMs);
    return Jwts.builder().subject(subject).issuedAt(now).expiration(expiry).signWith(key).compact();
  }

  private Claims extractClaims(String token, SecretKey key) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }

  private boolean isTokenValid(String token, SecretKey key) {
    try {
      extractClaims(token, key);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  private SecretKey accessTokenKey() {
    return Keys.hmacShaKeyFor(
        jwtProperties.accessToken().secret().getBytes(StandardCharsets.UTF_8));
  }

  private SecretKey refreshTokenKey() {
    return Keys.hmacShaKeyFor(
        jwtProperties.refreshToken().secret().getBytes(StandardCharsets.UTF_8));
  }
}
