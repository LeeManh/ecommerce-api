package com.ecommerce.backend.order.dto;

import com.ecommerce.backend.order.entity.Order;
import com.ecommerce.backend.order.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AdminOrderResponse(
    Long id,
    Long userId,
    String userEmail,
    OrderStatus status,
    String shippingAddress,
    BigDecimal totalAmount,
    List<OrderResponse.Item> items,
    Instant createdAt) {

  public static AdminOrderResponse from(Order order) {
    List<OrderResponse.Item> items =
        order.getItems().stream()
            .map(
                oi ->
                    new OrderResponse.Item(
                        oi.getProduct().getId(),
                        oi.getProductName(),
                        oi.getUnitPrice(),
                        oi.getQuantity(),
                        oi.getSubtotal()))
            .toList();

    return new AdminOrderResponse(
        order.getId(),
        order.getUser().getId(),
        order.getUser().getEmail(),
        order.getStatus(),
        order.getShippingAddress(),
        order.getTotalAmount(),
        items,
        order.getCreatedAt());
  }
}
