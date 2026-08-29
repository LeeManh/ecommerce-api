package com.ecommerce.backend.order.event;

import com.ecommerce.backend.common.logging.CorrelationIdFilter;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
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
    ProducerRecord<String, OrderCancelledEvent> record =
        new ProducerRecord<>(TOPIC, event.orderId().toString(), event);

    String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
    if (correlationId != null) {
      record
          .headers()
          .add(CorrelationIdFilter.MDC_KEY, correlationId.getBytes(StandardCharsets.UTF_8));
    }

    kafkaTemplate.send(record);
  }
}
