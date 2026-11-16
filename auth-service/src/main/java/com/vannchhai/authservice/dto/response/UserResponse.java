package com.vannchhai.authservice.dto.response;

import com.vannchhai.authservice.enums.UserRole;

import java.util.UUID;

public record UserResponse(UUID id, String username, String email, UserRole userRole) {}
