package com.ecommerce.backend.order.controller;

import com.ecommerce.backend.common.response.ApiResponse;
import com.ecommerce.backend.order.dto.DailyRevenueResponse;
import com.ecommerce.backend.order.dto.RevenueSummaryResponse;
import com.ecommerce.backend.order.service.RevenueService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats/revenue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRevenueController {

  private final RevenueService revenueService;

  @GetMapping("/summary")
  public ApiResponse<RevenueSummaryResponse> getSummary(
      @RequestParam Instant from, @RequestParam Instant to) {
    return ApiResponse.success(revenueService.getSummary(from, to), "OK");
  }

  @GetMapping("/daily")
  public ApiResponse<List<DailyRevenueResponse>> getDailyRevenue(
      @RequestParam Instant from, @RequestParam Instant to) {
    return ApiResponse.success(revenueService.getDailyRevenue(from, to), "OK");
  }
}
