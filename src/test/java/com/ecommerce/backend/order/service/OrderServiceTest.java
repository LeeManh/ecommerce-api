package com.ecommerce.backend.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.backend.common.exception.ApiException;
import com.ecommerce.backend.common.exception.ErrorCode;
import com.ecommerce.backend.order.dto.CreateOrderRequest;
import com.ecommerce.backend.order.dto.OrderResponse;
import com.ecommerce.backend.order.entity.Cart;
import com.ecommerce.backend.order.entity.CartItem;
import com.ecommerce.backend.order.entity.Order;
import com.ecommerce.backend.order.entity.OrderItem;
import com.ecommerce.backend.order.entity.OrderStatus;
import com.ecommerce.backend.order.event.OrderCancelledEvent;
import com.ecommerce.backend.order.event.OrderCreatedEvent;
import com.ecommerce.backend.order.repository.CartRepository;
import com.ecommerce.backend.order.repository.OrderRepository;
import com.ecommerce.backend.product.entity.Product;
import com.ecommerce.backend.user.entity.User;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;
  @Mock private CartRepository cartRepository;
  @Mock private PaymentService paymentService;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private StringRedisTemplate redisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;

  @InjectMocks private OrderService orderService;

  private final User user = User.builder().id(1L).email("user@shop.com").build();

  @Test
  void createOrder_shouldCreateOrderAndPublishEvent_whenCartHasActiveProducts() {
    Product product =
        Product.builder()
            .id(100L)
            .name("iPhone")
            .sku("SKU")
            .price(new BigDecimal("1000"))
            .active(true)
            .build();
    Cart cart = Cart.builder().id(10L).user(user).build();
    cart.getItems().add(CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build());
    CreateOrderRequest request = new CreateOrderRequest("123 Main St");

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(true);
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            invocation -> {
              Order order = invocation.getArgument(0);
              order.setId(500L);
              return order;
            });

    OrderResponse response = orderService.createOrder(user, "idem-key-1", request);

    assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
    assertThat(response.totalAmount()).isEqualByComparingTo("2000");
    assertThat(cart.getItems()).isEmpty();
    verify(eventPublisher).publishEvent(any(OrderCreatedEvent.class));
  }

  @Test
  void createOrder_shouldThrow_whenIdempotencyKeyAlreadyUsed() {
    CreateOrderRequest request = new CreateOrderRequest("123 Main St");

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(false);

    assertThatThrownBy(() -> orderService.createOrder(user, "dup-key", request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.DUPLICATE_REQUEST);

    verify(cartRepository, never()).findByUserId(any());
  }

  @Test
  void createOrder_shouldThrow_whenCartIsEmpty() {
    CreateOrderRequest request = new CreateOrderRequest("123 Main St");
    Cart cart = Cart.builder().id(10L).user(user).build();

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(true);
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

    assertThatThrownBy(() -> orderService.createOrder(user, "key", request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.CART_EMPTY);
  }

  @Test
  void createOrder_shouldThrow_whenProductInCartIsNoLongerActive() {
    Product inactiveProduct =
        Product.builder()
            .id(100L)
            .name("iPhone")
            .sku("SKU")
            .price(new BigDecimal("1000"))
            .active(false)
            .build();
    Cart cart = Cart.builder().id(10L).user(user).build();
    cart.getItems()
        .add(CartItem.builder().id(1L).cart(cart).product(inactiveProduct).quantity(1).build());
    CreateOrderRequest request = new CreateOrderRequest("123 Main St");

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(true);
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

    assertThatThrownBy(() -> orderService.createOrder(user, "key", request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.PRODUCT_NOT_AVAILABLE);
  }

  @Test
  void pay_shouldMarkOrderPaid_whenPaymentSucceeds() {
    Order order = buildPendingOrder();
    when(orderRepository.findById(500L)).thenReturn(Optional.of(order));
    when(paymentService.charge(500L, order.getTotalAmount())).thenReturn(true);

    OrderResponse response = orderService.pay(user, 500L);

    assertThat(response.status()).isEqualTo(OrderStatus.PAID);
    verify(eventPublisher, never()).publishEvent(any(OrderCancelledEvent.class));
  }

  @Test
  void pay_shouldCancelOrderAndPublishEvent_whenPaymentFails() {
    Order order = buildPendingOrder();
    when(orderRepository.findById(500L)).thenReturn(Optional.of(order));
    when(paymentService.charge(500L, order.getTotalAmount())).thenReturn(false);

    assertThatThrownBy(() -> orderService.pay(user, 500L))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.PAYMENT_FAILED);

    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    verify(eventPublisher).publishEvent(any(OrderCancelledEvent.class));
  }

  @Test
  void pay_shouldThrow_whenOrderNotFoundOrNotOwnedByUser() {
    when(orderRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.pay(user, 999L))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
  }

  @Test
  void pay_shouldThrow_whenOrderNotPending() {
    Order order = buildPendingOrder();
    order.setStatus(OrderStatus.PAID);
    when(orderRepository.findById(500L)).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.pay(user, 500L))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.ORDER_NOT_PENDING);

    verify(paymentService, never()).charge(any(), any());
  }

  @Test
  void getOrder_shouldReturnOrder_whenOwnedByUser() {
    Order order = buildPendingOrder();
    when(orderRepository.findById(500L)).thenReturn(Optional.of(order));

    OrderResponse response = orderService.getOrder(user, 500L);

    assertThat(response.id()).isEqualTo(500L);
  }

  @Test
  void getOrder_shouldThrow_whenOrderBelongsToAnotherUser() {
    Order order = buildPendingOrder();
    order.setUser(User.builder().id(999L).email("other@shop.com").build());
    when(orderRepository.findById(500L)).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.getOrder(user, 500L))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
  }

  private Order buildPendingOrder() {
    Product product =
        Product.builder().id(100L).name("iPhone").sku("SKU").price(new BigDecimal("1000")).build();
    Order order =
        Order.builder()
            .id(500L)
            .user(user)
            .status(OrderStatus.PENDING)
            .shippingAddress("123 Main St")
            .totalAmount(new BigDecimal("2000"))
            .build();
    order
        .getItems()
        .add(
            OrderItem.builder()
                .order(order)
                .product(product)
                .productName(product.getName())
                .unitPrice(product.getPrice())
                .quantity(2)
                .subtotal(new BigDecimal("2000"))
                .build());
    return order;
  }
}
