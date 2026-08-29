package com.ecommerce.backend.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

  @Mock private CartRepository cartRepository;
  @Mock private ProductRepository productRepository;

  @InjectMocks private CartService cartService;

  private final User user = User.builder().id(1L).email("user@shop.com").build();

  @Test
  void getCart_shouldCreateNewCart_whenUserHasNone() {
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
    when(cartRepository.save(any(Cart.class)))
        .thenAnswer(
            invocation -> {
              Cart cart = invocation.getArgument(0);
              cart.setId(10L);
              return cart;
            });

    CartResponse response = cartService.getCart(user);

    assertThat(response.id()).isEqualTo(10L);
    assertThat(response.items()).isEmpty();
  }

  @Test
  void addItem_shouldAddNewLine_whenProductNotAlreadyInCart() {
    Cart cart = Cart.builder().id(10L).user(user).build();
    Product product =
        Product.builder().id(100L).name("iPhone").sku("SKU").price(new BigDecimal("1000")).build();
    CartItemRequest request = new CartItemRequest(100L, 2);

    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
    when(productRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(product));

    CartResponse response = cartService.addItem(user, request);

    assertThat(response.items()).hasSize(1);
    assertThat(response.items().get(0).quantity()).isEqualTo(2);
    assertThat(response.totalAmount()).isEqualByComparingTo("2000");
  }

  @Test
  void addItem_shouldIncrementQuantity_whenProductAlreadyInCart() {
    Product product =
        Product.builder().id(100L).name("iPhone").sku("SKU").price(new BigDecimal("1000")).build();
    Cart cart = Cart.builder().id(10L).user(user).build();
    cart.getItems().add(CartItem.builder().id(1L).cart(cart).product(product).quantity(1).build());
    CartItemRequest request = new CartItemRequest(100L, 2);

    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
    when(productRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(product));

    CartResponse response = cartService.addItem(user, request);

    assertThat(response.items()).hasSize(1);
    assertThat(response.items().get(0).quantity()).isEqualTo(3);
  }

  @Test
  void addItem_shouldThrow_whenProductNotFoundOrInactive() {
    Cart cart = Cart.builder().id(10L).user(user).build();
    CartItemRequest request = new CartItemRequest(999L, 1);

    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
    when(productRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cartService.addItem(user, request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
  }

  @Test
  void updateItemQuantity_shouldUpdate_whenItemExists() {
    Product product =
        Product.builder().id(100L).name("iPhone").sku("SKU").price(new BigDecimal("1000")).build();
    Cart cart = Cart.builder().id(10L).user(user).build();
    cart.getItems().add(CartItem.builder().id(1L).cart(cart).product(product).quantity(1).build());

    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

    CartResponse response = cartService.updateItemQuantity(user, 1L, 5);

    assertThat(response.items().get(0).quantity()).isEqualTo(5);
  }

  @Test
  void updateItemQuantity_shouldThrow_whenItemNotFound() {
    Cart cart = Cart.builder().id(10L).user(user).build();
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

    assertThatThrownBy(() -> cartService.updateItemQuantity(user, 999L, 5))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
  }

  @Test
  void removeItem_shouldRemoveItem_whenExists() {
    Product product =
        Product.builder().id(100L).name("iPhone").sku("SKU").price(new BigDecimal("1000")).build();
    Cart cart = Cart.builder().id(10L).user(user).build();
    cart.getItems().add(CartItem.builder().id(1L).cart(cart).product(product).quantity(1).build());

    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

    CartResponse response = cartService.removeItem(user, 1L);

    assertThat(response.items()).isEmpty();
  }
}
