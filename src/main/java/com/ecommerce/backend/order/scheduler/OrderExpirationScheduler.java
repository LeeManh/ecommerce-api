package com.ecommerce.backend.order.scheduler;

import com.ecommerce.backend.order.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpirationScheduler {

  private static final long FIXED_DELAY_MS = 60_000;

  private final OrderService orderService;

  @Scheduled(fixedDelay = FIXED_DELAY_MS)
  public void cancelExpiredPendingOrders() {
    List<Long> expiredOrderIds = orderService.findExpiredPendingOrderIds();
    if (expiredOrderIds.isEmpty()) {
      return;
    }

    log.info("Found {} expired pending order(s) to cancel", expiredOrderIds.size());
    expiredOrderIds.forEach(orderService::cancelExpiredOrder);
  }
}
