package com.ecommerce.backend.order.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderCancelledEventListener {

  public static final String TOPIC = "order.cancelled";

  private final KafkaTemplate<String, OrderCancelledEvent> kafkaTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onOrderCancelled(OrderCancelledEvent event) {
    kafkaTemplate.send(TOPIC, event.orderId().toString(), event);
  }
}
