package com.ecommerce.backend.user.controller;

import com.ecommerce.backend.common.response.ApiResponse;
import com.ecommerce.backend.user.dto.ChangePasswordRequest;
import com.ecommerce.backend.user.dto.UserProfileUpdateRequest;
import com.ecommerce.backend.user.dto.UserResponse;
import com.ecommerce.backend.user.entity.User;
import com.ecommerce.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserProfileController {

  private final UserService userService;

  @GetMapping
  public ApiResponse<UserResponse> getProfile(@AuthenticationPrincipal User user) {
    return ApiResponse.success(userService.getProfile(user), "OK");
  }

  @PutMapping
  public ApiResponse<UserResponse> updateProfile(
      @AuthenticationPrincipal User user, @Valid @RequestBody UserProfileUpdateRequest request) {
    return ApiResponse.success(userService.updateProfile(user, request), "Profile updated");
  }

  @PutMapping("/password")
  public ApiResponse<Void> changePassword(
      @AuthenticationPrincipal User user, @Valid @RequestBody ChangePasswordRequest request) {
    userService.changePassword(user, request);
    return ApiResponse.success(null, "Password changed");
  }
}
