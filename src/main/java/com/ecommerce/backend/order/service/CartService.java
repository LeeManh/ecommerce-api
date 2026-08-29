package com.ecommerce.backend.order.service;

import com.ecommerce.backend.common.exception.ApiException;
import com.ecommerce.backend.common.exception.ErrorCode;
import com.ecommerce.backend.order.dto.CartItemRequest;
import com.ecommerce.backend.order.dto.CartResponse;
import com.ecommerce.backend.order.entity.Cart;
import com.ecommerce.backend.order.entity.CartItem;
import com.ecommerce.backend.order.repository.CartRepository;
import com.ecommerce.backend.product.entity.Product;
import com.ecommerce.backend.product.repository.ProductRepository;
import com.ecommerce.backend.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;
  private final ProductRepository productRepository;

  @Transactional
  public CartResponse getCart(User user) {
    return CartResponse.from(getOrCreateCart(user));
  }

  @Transactional
  public CartResponse addItem(User user, CartItemRequest request) {
    Cart cart = getOrCreateCart(user);
    Product product =
        productRepository
            .findByIdAndActiveTrue(request.productId())
            .orElseThrow(
                () ->
                    new ApiException(
                        ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + request.productId()));

    cart.getItems().stream()
        .filter(item -> item.getProduct().getId().equals(product.getId()))
        .findFirst()
        .ifPresentOrElse(
            existing -> existing.setQuantity(existing.getQuantity() + request.quantity()),
            () ->
                cart.getItems()
                    .add(
                        CartItem.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(request.quantity())
                            .build()));

    return CartResponse.from(cart);
  }

  @Transactional
  public CartResponse updateItemQuantity(User user, Long itemId, int quantity) {
    Cart cart = getOrCreateCart(user);
    findItemOrThrow(cart, itemId).setQuantity(quantity);
    return CartResponse.from(cart);
  }

  @Transactional
  public CartResponse removeItem(User user, Long itemId) {
    Cart cart = getOrCreateCart(user);
    cart.getItems().remove(findItemOrThrow(cart, itemId));
    return CartResponse.from(cart);
  }

  private CartItem findItemOrThrow(Cart cart, Long itemId) {
    return cart.getItems().stream()
        .filter(item -> item.getId().equals(itemId))
        .findFirst()
        .orElseThrow(
            () ->
                new ApiException(ErrorCode.CART_ITEM_NOT_FOUND, "Cart item not found: " + itemId));
  }

  private Cart getOrCreateCart(User user) {
    return cartRepository
        .findByUserId(user.getId())
        .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
  }
}
