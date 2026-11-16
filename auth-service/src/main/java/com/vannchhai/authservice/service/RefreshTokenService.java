package com.vannchhai.authservice.service;

import com.vannchhai.authservice.exception.InvalidRefreshTokenException;
import com.vannchhai.authservice.model.RefreshToken;
import com.vannchhai.authservice.repository.RefreshTokenRepository;
import com.vannchhai.authservice.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtService jwtService;

  @Transactional
  public String issue(UUID userId) {
    String rawToken = jwtService.generateOpaqueRefreshToken();
    RefreshToken entity =
        RefreshToken.builder()
            .userId(userId)
            .tokenHash(jwtService.hashToken(rawToken))
            .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTokenTtlSeconds()))
            .build();
    refreshTokenRepository.save(entity);
    return rawToken;
  }

  @Transactional
  public RotationResult rotate(String rawToken) {
    String hash = jwtService.hashToken(rawToken);
    RefreshToken existing =
        refreshTokenRepository
            .findByTokenHash(hash)
            .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not recognized"));

    if (!existing.isActive()) {
      throw new InvalidRefreshTokenException("Refresh token expired or already used");
    }

    String newRawToken = jwtService.generateOpaqueRefreshToken();
    String newHash = jwtService.hashToken(newRawToken);

    existing.setRevoked(true);
    existing.setReplacedByHash(newHash);
    refreshTokenRepository.save(existing);

    RefreshToken replacement =
        RefreshToken.builder()
            .userId(existing.getUserId())
            .tokenHash(newHash)
            .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTokenTtlSeconds()))
            .build();
    refreshTokenRepository.save(replacement);

    return new RotationResult(existing.getUserId(), newRawToken);
  }

  @Transactional
  public void revokeAllForUser(UUID userId) {
    refreshTokenRepository.revokeAllForUser(userId);
  }

  public record RotationResult(UUID userId, String newRawToken) {}
}
