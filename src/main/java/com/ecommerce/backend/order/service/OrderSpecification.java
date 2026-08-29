package com.ecommerce.backend.order.service;

import com.ecommerce.backend.order.entity.Order;
import com.ecommerce.backend.order.entity.OrderStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecification {

  private OrderSpecification() {}

  public static Specification<Order> filter(OrderStatus status, Long userId) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (status != null) {
        predicates.add(cb.equal(root.get("status"), status));
      }

      if (userId != null) {
        predicates.add(cb.equal(root.get("user").get("id"), userId));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
