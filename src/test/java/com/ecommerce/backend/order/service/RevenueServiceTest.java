package com.ecommerce.backend.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ecommerce.backend.order.dto.DailyRevenueResponse;
import com.ecommerce.backend.order.dto.RevenueSummaryResponse;
import com.ecommerce.backend.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {

  @Mock private OrderRepository orderRepository;

  @InjectMocks private RevenueService revenueService;

  @Test
  void getSummary_shouldReturnTotalRevenueAndOrderCount() {
    Instant from = Instant.parse("2026-08-01T00:00:00Z");
    Instant to = Instant.parse("2026-08-31T23:59:59Z");

    when(orderRepository.sumRevenueBetween(from, to)).thenReturn(new BigDecimal("1500.00"));
    when(orderRepository.countPaidOrdersBetween(from, to)).thenReturn(3L);

    RevenueSummaryResponse response = revenueService.getSummary(from, to);

    assertThat(response.totalRevenue()).isEqualByComparingTo("1500.00");
    assertThat(response.orderCount()).isEqualTo(3L);
    assertThat(response.from()).isEqualTo(from);
    assertThat(response.to()).isEqualTo(to);
  }

  @Test
  void getDailyRevenue_shouldMapRowsToDailyRevenueResponses() {
    Instant from = Instant.parse("2026-08-01T00:00:00Z");
    Instant to = Instant.parse("2026-08-31T23:59:59Z");
    Timestamp day = Timestamp.from(Instant.parse("2026-08-15T00:00:00Z"));

    Object[] row = {day, new BigDecimal("500.00"), 2L};
    List<Object[]> rows = Collections.singletonList(row);
    when(orderRepository.findDailyRevenueBetween(from, to)).thenReturn(rows);

    List<DailyRevenueResponse> result = revenueService.getDailyRevenue(from, to);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).revenue()).isEqualByComparingTo("500.00");
    assertThat(result.get(0).orderCount()).isEqualTo(2L);
  }
}
