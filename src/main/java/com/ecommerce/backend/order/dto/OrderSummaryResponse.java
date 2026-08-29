package com.ecommerce.backend.order.dto;

import com.ecommerce.backend.order.entity.Order;
import com.ecommerce.backend.order.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryResponse(
    Long id, OrderStatus status, BigDecimal totalAmount, Instant createdAt) {

  public static OrderSummaryResponse from(Order order) {
    return new OrderSummaryResponse(
        order.getId(), order.getStatus(), order.getTotalAmount(), order.getCreatedAt());
  }
}
