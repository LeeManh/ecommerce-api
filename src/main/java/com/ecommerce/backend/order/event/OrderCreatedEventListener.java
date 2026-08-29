package com.ecommerce.backend.order.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener {

  public static final String TOPIC = "order.created";

  private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onOrderCreated(OrderCreatedEvent event) {
    kafkaTemplate.send(TOPIC, event.orderId().toString(), event);
  }
}
