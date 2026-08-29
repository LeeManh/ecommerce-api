package com.ecommerce.backend.inventory.controller;

import com.ecommerce.backend.common.response.ApiResponse;
import com.ecommerce.backend.inventory.dto.InventoryResponse;
import com.ecommerce.backend.inventory.dto.InventoryUpdateRequest;
import com.ecommerce.backend.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminInventoryController {

  private final InventoryService inventoryService;

  @PutMapping("/{productId}")
  public ApiResponse<InventoryResponse> updateQuantity(
      @PathVariable Long productId, @Valid @RequestBody InventoryUpdateRequest request) {
    return ApiResponse.success(
        inventoryService.updateQuantity(productId, request.quantity()), "Inventory updated");
  }
}
