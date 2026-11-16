package com.vannchhai.authservice.service;

import com.vannchhai.authservice.dto.request.LoginRequest;
import com.vannchhai.authservice.dto.request.RegisterRequest;
import com.vannchhai.authservice.dto.response.AuthResponse;
import com.vannchhai.authservice.dto.response.UserResponse;

import java.util.UUID;

public interface AuthService {
  UserResponse register(RegisterRequest request);
  AuthResponse login(LoginRequest request);
  AuthResponse refresh(String rawRefreshToken);
  void logout(UUID userId);
}
