package com.ecommerce.backend.product.repository;

import com.ecommerce.backend.product.entity.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository
    extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
  boolean existsBySku(String sku);

  Optional<Product> findByIdAndActiveTrue(Long id);
}
