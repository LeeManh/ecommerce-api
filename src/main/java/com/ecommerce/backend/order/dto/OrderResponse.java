package com.ecommerce.backend.order.dto;

import com.ecommerce.backend.order.entity.Order;
import com.ecommerce.backend.order.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
    Long id,
    OrderStatus status,
    String shippingAddress,
    BigDecimal totalAmount,
    List<Item> items,
    Instant createdAt) {

  public record Item(
      Long productId,
      String productName,
      BigDecimal unitPrice,
      int quantity,
      BigDecimal subtotal) {}

  public static OrderResponse from(Order order) {
    List<Item> items =
        order.getItems().stream()
            .map(
                oi ->
                    new Item(
                        oi.getProduct().getId(),
                        oi.getProductName(),
                        oi.getUnitPrice(),
                        oi.getQuantity(),
                        oi.getSubtotal()))
            .toList();
    return new OrderResponse(
        order.getId(),
        order.getStatus(),
        order.getShippingAddress(),
        order.getTotalAmount(),
        items,
        order.getCreatedAt());
  }
}
