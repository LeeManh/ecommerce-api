package com.ecommerce.backend.product.repository;

import com.ecommerce.backend.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  boolean existsByName(String name);
}
