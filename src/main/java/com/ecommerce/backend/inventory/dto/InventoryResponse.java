package com.ecommerce.backend.inventory.dto;

import com.ecommerce.backend.inventory.entity.Inventory;

public record InventoryResponse(Long productId, int quantity) {

  public static InventoryResponse from(Inventory inventory) {
    return new InventoryResponse(inventory.getProduct().getId(), inventory.getQuantity());
  }
}
