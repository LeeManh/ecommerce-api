package com.ecommerce.backend.inventory.repository;

import com.ecommerce.backend.inventory.entity.Inventory;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
  Optional<Inventory> findByProductId(Long productId);

  List<Inventory> findByProductIdIn(Collection<Long> productIds);
}
