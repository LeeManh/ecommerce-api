package com.ecommerce.backend.order.controller;

import com.ecommerce.backend.common.response.ApiResponse;
import com.ecommerce.backend.order.dto.CartItemRequest;
import com.ecommerce.backend.order.dto.CartResponse;
import com.ecommerce.backend.order.dto.UpdateCartItemRequest;
import com.ecommerce.backend.order.service.CartService;
import com.ecommerce.backend.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;

  @GetMapping
  public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal User user) {
    return ApiResponse.success(cartService.getCart(user), "OK");
  }

  @PostMapping("/items")
  public ApiResponse<CartResponse> addItem(
      @AuthenticationPrincipal User user, @Valid @RequestBody CartItemRequest request) {
    return ApiResponse.success(cartService.addItem(user, request), "Item added to cart");
  }

  @PutMapping("/items/{itemId}")
  public ApiResponse<CartResponse> updateItem(
      @AuthenticationPrincipal User user,
      @PathVariable Long itemId,
      @Valid @RequestBody UpdateCartItemRequest request) {
    return ApiResponse.success(
        cartService.updateItemQuantity(user, itemId, request.quantity()), "Cart item updated");
  }

  @DeleteMapping("/items/{itemId}")
  public ApiResponse<CartResponse> removeItem(
      @AuthenticationPrincipal User user, @PathVariable Long itemId) {
    return ApiResponse.success(cartService.removeItem(user, itemId), "Item removed from cart");
  }
}
