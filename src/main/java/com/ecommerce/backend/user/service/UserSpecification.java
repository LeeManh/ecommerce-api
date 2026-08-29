package com.ecommerce.backend.user.service;

import com.ecommerce.backend.user.entity.User;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

  private UserSpecification() {}

  public static Specification<User> filter(Boolean enabled, String email) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (enabled != null) {
        predicates.add(cb.equal(root.get("enabled"), enabled));
      }

      if (email != null && !email.isBlank()) {
        predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
