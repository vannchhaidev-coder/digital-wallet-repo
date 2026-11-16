package com.vannchhai.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Username is required.") @Size(min = 3, max = 50) String username,
    @NotBlank(message = "Email is required.") @Email @Size(max = 100) String email,
    @NotBlank(message = "Password is required.") @Size(min = 8, max = 72) String password) {}
