package com.ecommerce.backend.order.dto;

import com.ecommerce.backend.order.entity.Cart;
import com.ecommerce.backend.order.entity.CartItem;
import java.math.BigDecimal;
import java.util.List;

public record CartResponse(Long id, List<Item> items, BigDecimal totalAmount) {

  public record Item(
      Long itemId,
      Long productId,
      String productName,
      BigDecimal unitPrice,
      int quantity,
      BigDecimal subtotal) {}

  public static CartResponse from(Cart cart) {
    List<Item> items = cart.getItems().stream().map(CartResponse::toItem).toList();
    BigDecimal total = items.stream().map(Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    return new CartResponse(cart.getId(), items, total);
  }

  private static Item toItem(CartItem cartItem) {
    BigDecimal unitPrice = cartItem.getProduct().getPrice();
    BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
    return new Item(
        cartItem.getId(),
        cartItem.getProduct().getId(),
        cartItem.getProduct().getName(),
        unitPrice,
        cartItem.getQuantity(),
        subtotal);
  }
}
