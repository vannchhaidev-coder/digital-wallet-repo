package com.vannchhai.authservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "refresh_token",
    indexes = @Index(name = "idx_refresh_token_hash", columnList = "token_hash"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "revoked", nullable = false)
  @Builder.Default
  private boolean revoked = false;

  @Column(name = "created_at", nullable = false)
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "replaced_by_hash", length = 64)
  private String replacedByHash;

  public boolean isActive() {
    return !revoked && Instant.now().isBefore(expiresAt);
  }
}
