package com.ecommerce.backend.order.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(@NotBlank String shippingAddress) {}
