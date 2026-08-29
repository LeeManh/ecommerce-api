package com.ecommerce.backend.auth.controller;

import com.ecommerce.backend.auth.dto.AuthResponse;
import com.ecommerce.backend.auth.dto.LoginRequest;
import com.ecommerce.backend.auth.dto.RefreshTokenRequest;
import com.ecommerce.backend.auth.dto.RegisterRequest;
import com.ecommerce.backend.auth.service.AuthService;
import com.ecommerce.backend.common.response.ApiResponse;
import com.ecommerce.backend.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ApiResponse.success(authService.register(request), "Registration successful");
  }

  @PostMapping("/login")
  public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ApiResponse.success(authService.login(request), "Login successful");
  }

  @PostMapping("/refresh-token")
  public ApiResponse<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
    return ApiResponse.success(authService.refreshToken(request.refreshToken()), "Token refreshed");
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
    authService.logout(request.refreshToken());
    return ApiResponse.success(null, "Logout successful");
  }
}
