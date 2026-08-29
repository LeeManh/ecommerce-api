package com.ecommerce.backend.order.service;

import com.ecommerce.backend.order.dto.DailyRevenueResponse;
import com.ecommerce.backend.order.dto.RevenueSummaryResponse;
import com.ecommerce.backend.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RevenueService {

  private final OrderRepository orderRepository;

  @Transactional
  public RevenueSummaryResponse getSummary(Instant from, Instant to) {
    BigDecimal totalRevenue = orderRepository.sumRevenueBetween(from, to);
    long orderCount = orderRepository.countPaidOrdersBetween(from, to);
    return new RevenueSummaryResponse(totalRevenue, orderCount, from, to);
  }

  @Transactional
  public List<DailyRevenueResponse> getDailyRevenue(Instant from, Instant to) {
    return orderRepository.findDailyRevenueBetween(from, to).stream()
        .map(
            row ->
                new DailyRevenueResponse(
                    ((Timestamp) row[0]).toInstant().atZone(ZoneOffset.UTC).toLocalDate(),
                    (BigDecimal) row[1],
                    ((Number) row[2]).longValue()))
        .toList();
  }
}
