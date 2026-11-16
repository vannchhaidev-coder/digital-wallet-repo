package com.vannchhai.authservice.security;

import com.vannchhai.authservice.model.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class JwtService {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final JwtProperties properties;
  private final SecretKey signingKey;

  public JwtService(JwtProperties properties) {
    this.properties = properties;
    this.signingKey = Keys.hmacShaKeyFor(decodeSecret(properties.secret()));
  }

  private static byte[] decodeSecret(String secret) {
    byte[] decode;
    try {
      decode = Base64.getDecoder().decode(secret);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException(
          "app.jwt.secret must be valid base64 - generate one with JwtKeyGenerator", ex);
    }
    if (decode.length < 32) {
      throw new IllegalStateException(
          "app.jwt.secret must decode to at least 256 bits (32 bytes) for HS256");
    }
    return decode;
  }

  public String generateAccessToken(UserEntity user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .issuer(properties.issuer())
        .subject(user.getId().toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(properties.accessTokenTtlSeconds())))
        .claims(
            Map.of(
                "username", user.getUsername(),
                "role", user.getUserRole().name()))
        .signWith(signingKey, Jwts.SIG.HS256)
        .compact();
  }

  public Claims parseAndValidate(String token) throws JwtException {
    return Jwts.parser()
        .verifyWith(signingKey)
        .requireIssuer(properties.issuer())
        .build()
        .parseEncryptedClaims(token)
        .getPayload();
  }

  public long getAccessTokenTtlSeconds() {
    return properties.accessTokenTtlSeconds();
  }

  public long getRefreshTokenTtlSeconds() {
    return properties.refreshTokenTtlSeconds();
  }

  public String generateOpaqueRefreshToken() {
    byte[] bytes = new byte[32];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public String hashToken(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
