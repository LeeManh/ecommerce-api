package com.ecommerce.backend.inventory.repository;

import com.ecommerce.backend.inventory.entity.Inventory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
  Optional<Inventory> findByProductId(Long productId);
}
