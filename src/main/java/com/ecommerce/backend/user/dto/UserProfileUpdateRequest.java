package com.ecommerce.backend.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserProfileUpdateRequest(@NotBlank String fullName) {}
