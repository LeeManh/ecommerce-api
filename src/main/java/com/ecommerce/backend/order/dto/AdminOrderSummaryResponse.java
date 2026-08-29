package com.ecommerce.backend.order.dto;

import com.ecommerce.backend.order.entity.Order;
import com.ecommerce.backend.order.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record AdminOrderSummaryResponse(
    Long id,
    Long userId,
    String userEmail,
    OrderStatus status,
    BigDecimal totalAmount,
    Instant createdAt,
    Instant paidAt) {

  public static AdminOrderSummaryResponse from(Order order) {
    return new AdminOrderSummaryResponse(
        order.getId(),
        order.getUser().getId(),
        order.getUser().getEmail(),
        order.getStatus(),
        order.getTotalAmount(),
        order.getCreatedAt(),
        order.getPaidAt());
  }
}
