package com.ecommerce.backend.order.service;

import com.ecommerce.backend.common.exception.ApiException;
import com.ecommerce.backend.common.exception.ErrorCode;
import com.ecommerce.backend.order.dto.CreateOrderRequest;
import com.ecommerce.backend.order.dto.OrderResponse;
import com.ecommerce.backend.order.dto.OrderSummaryResponse;
import com.ecommerce.backend.order.entity.Cart;
import com.ecommerce.backend.order.entity.CartItem;
import com.ecommerce.backend.order.entity.Order;
import com.ecommerce.backend.order.entity.OrderItem;
import com.ecommerce.backend.order.entity.OrderStatus;
import com.ecommerce.backend.order.repository.CartRepository;
import com.ecommerce.backend.order.repository.OrderRepository;
import com.ecommerce.backend.product.entity.Product;
import com.ecommerce.backend.user.entity.User;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final CartRepository cartRepository;

  @Transactional
  public OrderResponse createOrder(User user, CreateOrderRequest request) {
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

    return OrderResponse.from(saved);
  }

  @Transactional
  public Page<OrderSummaryResponse> getOrders(User user, Pageable pageable) {
    return orderRepository.findByUserId(user.getId(), pageable).map(OrderSummaryResponse::from);
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
}
