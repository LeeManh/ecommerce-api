package com.ecommerce.backend.order.controller;

import com.ecommerce.backend.common.response.ApiResponse;
import com.ecommerce.backend.order.dto.AdminOrderResponse;
import com.ecommerce.backend.order.dto.AdminOrderSummaryResponse;
import com.ecommerce.backend.order.entity.OrderStatus;
import com.ecommerce.backend.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

  private final OrderService orderService;

  @GetMapping
  public ApiResponse<Page<AdminOrderSummaryResponse>> getOrders(
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) Long userId,
      Pageable pageable) {
    return ApiResponse.success(orderService.searchOrdersForAdmin(status, userId, pageable), "OK");
  }

  @GetMapping("/{id}")
  public ApiResponse<AdminOrderResponse> getOrder(@PathVariable Long id) {
    return ApiResponse.success(orderService.getOrderForAdmin(id), "OK");
  }

  @PostMapping("/{id}/cancel")
  public ApiResponse<AdminOrderResponse> cancelOrder(@PathVariable Long id) {
    return ApiResponse.success(orderService.cancelOrderByAdmin(id), "Order cancelled");
  }
}
