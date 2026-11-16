package com.vannchhai.authservice.model;

import com.vannchhai.authservice.enums.UserRole;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@Table(name = "user_info")
public class UserEntity extends AuditableEntity implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(name = "user_role", nullable = false)
  @Enumerated(EnumType.STRING)
  private UserRole userRole;

  @Column(nullable = false)
  private Boolean enabled = true;

  @Column(nullable = false)
  private Boolean accountNonLocked = true;

  @Column(name = "failed_attempts", nullable = false)
  @Builder.Default
  private int failedAttempts = 0;

  @Column(name = "locked_at")
  private Instant lockedAt;

  public void registerFailedLogin(int maxAttempts) {
    this.failedAttempts++;
    if (this.failedAttempts >= maxAttempts) {
      this.accountNonLocked = false;
      this.lockedAt = Instant.now();
    }
  }

  public void registerSuccessfulLogin() {
    this.failedAttempts = 0;
  }

  public boolean unlockIfEligible(long lockoutDurationMinutes) {
    if (Boolean.TRUE.equals(this.accountNonLocked) || this.lockedAt == null) {
      return false;
    }
    boolean eligible =
        Instant.now().isAfter(this.lockedAt.plusSeconds(lockoutDurationMinutes * 60));
    if (eligible) {
      this.accountNonLocked = true;
      this.failedAttempts = 0;
      this.lockedAt = null;
    }
    return eligible;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  @Override
  public boolean isAccountNonExpired() {
    return UserDetails.super.isAccountNonExpired();
  }

  @Override
  public boolean isAccountNonLocked() {
    return UserDetails.super.isAccountNonLocked();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return UserDetails.super.isCredentialsNonExpired();
  }

  @Override
  public boolean isEnabled() {
    return UserDetails.super.isEnabled();
  }
}
