package com.ecommerce.backend.user.dto;

import com.ecommerce.backend.auth.entity.Role;
import com.ecommerce.backend.user.entity.User;
import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(Long id, String email, String fullName, Set<String> roles) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
    }
}