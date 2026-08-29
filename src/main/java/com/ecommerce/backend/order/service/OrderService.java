package com.ecommerce.backend.order.service;

import com.ecommerce.backend.common.exception.ApiException;
import com.ecommerce.backend.common.exception.ErrorCode;
import com.ecommerce.backend.order.config.OrderProperties;
import com.ecommerce.backend.order.dto.AdminOrderResponse;
import com.ecommerce.backend.order.dto.AdminOrderSummaryResponse;
import com.ecommerce.backend.order.dto.CreateOrderRequest;
import com.ecommerce.backend.order.dto.OrderResponse;
import com.ecommerce.backend.order.dto.OrderSummaryResponse;
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
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

  private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:order:";
  private static final Duration IDEMPOTENCY_KEY_TTL = Duration.ofMinutes(5);

  private final OrderRepository orderRepository;
  private final CartRepository cartRepository;
  private final PaymentService paymentService;
  private final ApplicationEventPublisher eventPublisher;
  private final StringRedisTemplate redisTemplate;
  private final OrderProperties orderProperties;

  @Transactional
  public OrderResponse createOrder(User user, String idempotencyKey, CreateOrderRequest request) {
    Boolean acquired =
        redisTemplate
            .opsForValue()
            .setIfAbsent(IDEMPOTENCY_KEY_PREFIX + idempotencyKey, "1", IDEMPOTENCY_KEY_TTL);
    if (!Boolean.TRUE.equals(acquired)) {
      throw new ApiException(
          ErrorCode.DUPLICATE_REQUEST, "Duplicate order request: " + idempotencyKey);
    }

    Cart cart =
        cartRepository
            .findByUserId(user.getId())
            .orElseThrow(() -> new ApiException(ErrorCode.CART_EMPTY, "Cart is empty"));

    if (cart.getItems().isEmpty()) {
      throw new ApiException(ErrorCode.CART_EMPTY, "Cart is empty");
    }

    List<OrderItem> orderItems = new ArrayList<>();
    BigDecimal totalAmount = BigDecimal.ZERO;

    for (CartItem cartItem : cart.getItems()) {
      Product product = cartItem.getProduct();
      if (!product.isActive()) {
        throw new ApiException(
            ErrorCode.PRODUCT_NOT_AVAILABLE, "Product no longer available: " + product.getName());
      }

      BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
      totalAmount = totalAmount.add(subtotal);

      orderItems.add(
          OrderItem.builder()
              .product(product)
              .productName(product.getName())
              .unitPrice(product.getPrice())
              .quantity(cartItem.getQuantity())
              .subtotal(subtotal)
              .build());
    }

    Order order =
        Order.builder()
            .user(user)
            .status(OrderStatus.PENDING)
            .shippingAddress(request.shippingAddress())
            .totalAmount(totalAmount)
            .build();

    orderItems.forEach(item -> item.setOrder(order));
    order.getItems().addAll(orderItems);

    Order saved = orderRepository.save(order);

    cart.getItems().clear();

    List<OrderCreatedEvent.Item> eventItems =
        orderItems.stream()
            .map(item -> new OrderCreatedEvent.Item(item.getProduct().getId(), item.getQuantity()))
            .toList();
    eventPublisher.publishEvent(new OrderCreatedEvent(saved.getId(), user.getId(), eventItems));

    return OrderResponse.from(saved);
  }

  @Transactional
  public Page<OrderSummaryResponse> getOrders(User user, Pageable pageable) {
    return orderRepository.findByUserId(user.getId(), pageable).map(OrderSummaryResponse::from);
  }

  @Transactional(dontRollbackOn = ApiException.class)
  public OrderResponse pay(User user, Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .filter(o -> o.getUser().getId().equals(user.getId()))
            .orElseThrow(
                () -> new ApiException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderId));

    if (order.getStatus() != OrderStatus.PENDING) {
      throw new ApiException(
          ErrorCode.ORDER_NOT_PENDING, "Order is not pending payment: " + orderId);
    }

    boolean success = paymentService.charge(order.getId(), order.getTotalAmount());

    if (!success) {
      order.setStatus(OrderStatus.CANCELLED);
      publishOrderCancelledEvent(order);
      throw new ApiException(ErrorCode.PAYMENT_FAILED, "Payment failed for order: " + orderId);
    }

    order.setStatus(OrderStatus.PAID);
    return OrderResponse.from(order);
  }

  public List<Long> findExpiredPendingOrderIds() {
    Instant cutoff =
        Instant.now().minus(orderProperties.pendingExpirationMinutes(), ChronoUnit.MINUTES);
    return orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoff).stream()
        .map(Order::getId)
        .toList();
  }

  @Transactional
  public void cancelExpiredOrder(Long orderId) {
    Order order = orderRepository.findById(orderId).orElse(null);
    if (order == null || order.getStatus() != OrderStatus.PENDING) {
      return;
    }

    order.setStatus(OrderStatus.CANCELLED);
    publishOrderCancelledEvent(order);

    log.info("Auto-cancelled expired pending order {}", orderId);
  }

  @Transactional
  public OrderResponse getOrder(User user, Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .filter(o -> o.getUser().getId().equals(user.getId()))
            .orElseThrow(
                () -> new ApiException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderId));

    return OrderResponse.from(order);
  }

  @Transactional
  public Page<AdminOrderSummaryResponse> searchOrdersForAdmin(
      OrderStatus status, Long userId, Pageable pageable) {
    return orderRepository
        .findAll(OrderSpecification.filter(status, userId), pageable)
        .map(AdminOrderSummaryResponse::from);
  }

  @Transactional
  public AdminOrderResponse getOrderForAdmin(Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new ApiException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderId));
    return AdminOrderResponse.from(order);
  }

  @Transactional
  public AdminOrderResponse cancelOrderByAdmin(Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new ApiException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderId));

    if (order.getStatus() == OrderStatus.CANCELLED) {
      throw new ApiException(ErrorCode.ORDER_NOT_PENDING, "Order already cancelled: " + orderId);
    }

    order.setStatus(OrderStatus.CANCELLED);
    publishOrderCancelledEvent(order);

    log.info("Order {} cancelled by admin", orderId);
    return AdminOrderResponse.from(order);
  }

  private void publishOrderCancelledEvent(Order order) {
    List<OrderCancelledEvent.Item> eventItems =
        order.getItems().stream()
            .map(
                item -> new OrderCancelledEvent.Item(item.getProduct().getId(), item.getQuantity()))
            .toList();
    eventPublisher.publishEvent(new OrderCancelledEvent(order.getId(), eventItems));
  }
}
