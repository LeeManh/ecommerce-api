package com.ecommerce.backend.order.controller;

import com.ecommerce.backend.common.response.ApiResponse;
import com.ecommerce.backend.order.dto.CreateOrderRequest;
import com.ecommerce.backend.order.dto.OrderResponse;
import com.ecommerce.backend.order.dto.OrderSummaryResponse;
import com.ecommerce.backend.order.service.OrderService;
import com.ecommerce.backend.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  public ApiResponse<OrderResponse> createOrder(
      @AuthenticationPrincipal User user,
      @RequestHeader("Idempotency-Key") String idempotencyKey,
      @Valid @RequestBody CreateOrderRequest request) {
    return ApiResponse.success(
        orderService.createOrder(user, idempotencyKey, request), "Order created");
  }

  @GetMapping
  public ApiResponse<Page<OrderSummaryResponse>> getOrders(
      @AuthenticationPrincipal User user, Pageable pageable) {
    return ApiResponse.success(orderService.getOrders(user, pageable), "OK");
  }

  @GetMapping("/{id}")
  public ApiResponse<OrderResponse> getOrder(
      @AuthenticationPrincipal User user, @PathVariable Long id) {
    return ApiResponse.success(orderService.getOrder(user, id), "OK");
  }

  @PostMapping("/{id}/pay")
  public ApiResponse<OrderResponse> pay(@AuthenticationPrincipal User user, @PathVariable Long id) {
    return ApiResponse.success(orderService.pay(user, id), "Payment successful");
  }
}
