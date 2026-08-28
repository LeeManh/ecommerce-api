package com.ecommerce.backend.auth.dto;

import com.ecommerce.backend.user.dto.UserResponse;

public record AuthResponse(String accessToken, String refreshToken, UserResponse user) {}