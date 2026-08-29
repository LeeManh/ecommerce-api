package com.ecommerce.backend.order.event;

import java.util.List;

public record OrderCreatedEvent(Long orderId, Long userId, List<Item> items) {

  public record Item(Long productId, int quantity) {}
}
