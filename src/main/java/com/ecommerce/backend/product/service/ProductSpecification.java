package com.ecommerce.backend.product.service;

import com.ecommerce.backend.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

  private ProductSpecification() {}

  public static Specification<Product> search(String keyword, Long categoryId) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.isTrue(root.get("active")));

      if (keyword != null && !keyword.isBlank()) {
        predicates.add(cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"));
      }

      if (categoryId != null) {
        predicates.add(cb.equal(root.join("categories").get("id"), categoryId));
        query.distinct(true);
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
