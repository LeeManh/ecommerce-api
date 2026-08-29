package com.ecommerce.backend.inventory.consumer;

import com.ecommerce.backend.inventory.service.InventoryService;
import com.ecommerce.backend.order.event.OrderCancelledEvent;
import com.ecommerce.backend.order.event.OrderCancelledEventListener;
import com.ecommerce.backend.order.event.OrderCreatedEvent;
import com.ecommerce.backend.order.event.OrderCreatedEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryOrderEventConsumer {

  private static final String GROUP_ID = "inventory-service";

  private final InventoryService inventoryService;

  @KafkaListener(topics = OrderCreatedEventListener.TOPIC, groupId = GROUP_ID)
  public void onOrderCreated(OrderCreatedEvent event) {
    log.info("Received order.created event for order {}", event.orderId());
    event.items().forEach(item -> inventoryService.deduct(item.productId(), item.quantity()));
  }

  @KafkaListener(topics = OrderCancelledEventListener.TOPIC, groupId = GROUP_ID)
  public void onOrderCancelled(OrderCancelledEvent event) {
    log.info("Received order.cancelled event for order {}", event.orderId());
    event.items().forEach(item -> inventoryService.restock(item.productId(), item.quantity()));
  }
}
