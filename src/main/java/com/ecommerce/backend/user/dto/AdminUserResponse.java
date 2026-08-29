package com.ecommerce.backend.user.dto;

import com.ecommerce.backend.auth.entity.Role;
import com.ecommerce.backend.user.entity.User;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record AdminUserResponse(
    Long id, String email, String fullName, Set<String> roles, boolean enabled, Instant createdAt) {

  public static AdminUserResponse from(User user) {
    return new AdminUserResponse(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
        user.isEnabled(),
        user.getCreatedAt());
  }
}
