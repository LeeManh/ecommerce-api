package com.ecommerce.backend.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record ProductRequest(
    @NotBlank String name,
    @NotBlank String sku,
    String description,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
    @NotEmpty Set<Long> categoryIds,
    List<String> imageUrls) {}
