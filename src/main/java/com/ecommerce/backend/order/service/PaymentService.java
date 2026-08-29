package com.ecommerce.backend.order.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

  public boolean charge(Long orderId, BigDecimal amount) {
    return true;
  }
}
