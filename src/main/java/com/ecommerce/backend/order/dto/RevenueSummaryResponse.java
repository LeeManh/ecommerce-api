package com.ecommerce.backend.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RevenueSummaryResponse(
    BigDecimal totalRevenue, long orderCount, Instant from, Instant to) {}
