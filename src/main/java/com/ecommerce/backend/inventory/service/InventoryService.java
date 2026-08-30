package com.ecommerce.backend.inventory.service;

import com.ecommerce.backend.common.exception.ApiException;
import com.ecommerce.backend.common.exception.ErrorCode;
import com.ecommerce.backend.inventory.dto.InventoryResponse;
import com.ecommerce.backend.inventory.entity.Inventory;
import com.ecommerce.backend.inventory.repository.InventoryRepository;
import com.ecommerce.backend.product.entity.Product;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

  private final InventoryRepository inventoryRepository;

  @Transactional
  public void createForProduct(Product product) {
    inventoryRepository.save(Inventory.builder().product(product).quantity(0).build());
  }

  @Transactional
  public InventoryResponse updateQuantity(Long productId, int quantity) {
    Inventory inventory = getOrThrow(productId);
    inventory.setQuantity(quantity);
    return InventoryResponse.from(inventory);
  }

  @Transactional
  public void deduct(Long productId, int quantity) {
    Inventory inventory = getOrThrow(productId);
    inventory.setQuantity(inventory.getQuantity() - quantity);
    log.info(
        "Deducted {} unit(s) of product {}, remaining: {}",
        quantity,
        productId,
        inventory.getQuantity());
  }

  @Transactional
  public void restock(Long productId, int quantity) {
    Inventory inventory = getOrThrow(productId);
    inventory.setQuantity(inventory.getQuantity() + quantity);
    log.info(
        "Restocked {} unit(s) of product {}, new total: {}",
        quantity,
        productId,
        inventory.getQuantity());
  }

  public int getQuantity(Long productId) {
    return inventoryRepository.findByProductId(productId).map(Inventory::getQuantity).orElse(0);
  }

  public Map<Long, Integer> getQuantitiesByProductIds(Collection<Long> productIds) {
    return inventoryRepository.findByProductIdIn(productIds).stream()
        .collect(
            Collectors.toMap(inventory -> inventory.getProduct().getId(), Inventory::getQuantity));
  }

  private Inventory getOrThrow(Long productId) {
    return inventoryRepository
        .findByProductId(productId)
        .orElseThrow(
            () ->
                new ApiException(
                    ErrorCode.INVENTORY_NOT_FOUND,
                    "Inventory not found for product: " + productId));
  }
}
