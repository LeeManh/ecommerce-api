package com.ecommerce.backend.order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyRevenueResponse(LocalDate date, BigDecimal revenue, long orderCount) {}
