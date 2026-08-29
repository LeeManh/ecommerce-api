package com.ecommerce.backend.user.controller;

import com.ecommerce.backend.common.response.ApiResponse;
import com.ecommerce.backend.user.dto.AdminUserResponse;
import com.ecommerce.backend.user.entity.User;
import com.ecommerce.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final UserService userService;

  @GetMapping
  public ApiResponse<Page<AdminUserResponse>> getUsers(
      @RequestParam(required = false) Boolean enabled,
      @RequestParam(required = false) String email,
      Pageable pageable) {
    return ApiResponse.success(userService.searchUsersForAdmin(enabled, email, pageable), "OK");
  }

  @GetMapping("/{id}")
  public ApiResponse<AdminUserResponse> getUser(@PathVariable Long id) {
    return ApiResponse.success(userService.getUserForAdmin(id), "OK");
  }

  @PostMapping("/{id}/enable")
  public ApiResponse<AdminUserResponse> enableUser(
      @AuthenticationPrincipal User currentAdmin, @PathVariable Long id) {
    return ApiResponse.success(userService.setUserEnabled(currentAdmin, id, true), "User enabled");
  }

  @PostMapping("/{id}/disable")
  public ApiResponse<AdminUserResponse> disableUser(
      @AuthenticationPrincipal User currentAdmin, @PathVariable Long id) {
    return ApiResponse.success(
        userService.setUserEnabled(currentAdmin, id, false), "User disabled");
  }
}
