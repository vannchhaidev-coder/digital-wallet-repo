package com.vannchhai.authservice.service;

import com.vannchhai.authservice.dto.request.LoginRequest;
import com.vannchhai.authservice.dto.request.RegisterRequest;
import com.vannchhai.authservice.dto.response.AuthResponse;
import com.vannchhai.authservice.dto.response.UserResponse;
import com.vannchhai.authservice.enums.UserRole;
import com.vannchhai.authservice.exception.AccountLockedException;
import com.vannchhai.authservice.exception.InvalidCredentialsException;
import com.vannchhai.authservice.exception.UserAlreadyExistsException;
import com.vannchhai.authservice.mapper.UserMapper;
import com.vannchhai.authservice.model.UserEntity;
import com.vannchhai.authservice.repository.UserRepository;
import com.vannchhai.authservice.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;
  private final UserMapper userMapper;

  @org.springframework.beans.factory.annotation.Value("${app.security.max-failed-attempts:5}")
  private int maxFailedAttempts;

  @org.springframework.beans.factory.annotation.Value("${app.security.lockout-duration-minutes:15}")
  private long lockoutDurationMinutes;

  @Override
  @Transactional
  public UserResponse register(RegisterRequest request) {
    if (userRepository.existsByUsername(request.username())) {
      throw new UserAlreadyExistsException("Username already taken: " + request.username());
    }
    if (userRepository.existsByEmail(request.email())) {
      throw new UserAlreadyExistsException("Email already registered: " + request.email());
    }

    UserEntity user =
        UserEntity.builder()
            .username(request.username())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .userRole(UserRole.USER)
            .build();

    UserEntity saved = userRepository.save(user);
    log.info("New user registered: userId={}", saved.getId());
    return userMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public AuthResponse login(LoginRequest request) {
    UserEntity user =
        userRepository
            .findByUsername(request.email())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

    user.unlockIfEligible(lockoutDurationMinutes);

    if (!user.isAccountNonLocked()) {
      throw new AccountLockedException(
          "Account is locked due to repeated failed login attempts. Try again later.");
    }

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      user.registerFailedLogin(maxFailedAttempts);
      userRepository.save(user);
      log.warn(
          "Failed login attempt: userId={}, attempts={}", user.getId(), user.getFailedAttempts());
      throw new InvalidCredentialsException("Invalid username or password");
    }

    user.registerSuccessfulLogin();
    userRepository.save(user);

    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = refreshTokenService.issue(user.getId());

    log.info("User logged in: userId={}", user.getId());
    return AuthResponse.of(accessToken, refreshToken, jwtService.getAccessTokenTtlSeconds());
  }

  @Override
  @Transactional
  public AuthResponse refresh(String rawRefreshToken) {
    RefreshTokenService.RotationResult rotation = refreshTokenService.rotate(rawRefreshToken);

    UserEntity user =
        userRepository
            .findById(rotation.userId())
            .orElseThrow(() -> new InvalidCredentialsException("User no longer exists"));

    String accessToken = jwtService.generateAccessToken(user);
    return AuthResponse.of(
        accessToken, rotation.newRawToken(), jwtService.getAccessTokenTtlSeconds());
  }

  @Override
  @Transactional
  public void logout(UUID userId) {
    refreshTokenService.revokeAllForUser(userId);
    log.info("User logged out, all refresh tokens revoked: userId={}", userId);
  }
}
