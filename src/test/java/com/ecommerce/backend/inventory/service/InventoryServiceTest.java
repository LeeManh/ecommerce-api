package com.ecommerce.backend.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.backend.common.exception.ApiException;
import com.ecommerce.backend.common.exception.ErrorCode;
import com.ecommerce.backend.inventory.dto.InventoryResponse;
import com.ecommerce.backend.inventory.entity.Inventory;
import com.ecommerce.backend.inventory.repository.InventoryRepository;
import com.ecommerce.backend.product.entity.Product;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

  @Mock private InventoryRepository inventoryRepository;

  @InjectMocks private InventoryService inventoryService;

  @Test
  void createForProduct_shouldSaveInventoryWithZeroQuantity() {
    Product product =
        Product.builder().id(1L).name("iPhone").sku("SKU").price(new BigDecimal("100")).build();

    inventoryService.createForProduct(product);

    ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
    verify(inventoryRepository).save(captor.capture());
    assertThat(captor.getValue().getProduct()).isEqualTo(product);
    assertThat(captor.getValue().getQuantity()).isZero();
  }

  @Test
  void updateQuantity_shouldSetAbsoluteQuantity_whenInventoryExists() {
    Product product =
        Product.builder().id(1L).name("iPhone").sku("SKU").price(new BigDecimal("100")).build();
    Inventory inventory = Inventory.builder().id(1L).product(product).quantity(5).build();
    when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

    InventoryResponse response = inventoryService.updateQuantity(1L, 50);

    assertThat(response.quantity()).isEqualTo(50);
  }

  @Test
  void deduct_shouldDecreaseQuantity_whenInventoryExists() {
    Product product =
        Product.builder().id(1L).name("iPhone").sku("SKU").price(new BigDecimal("100")).build();
    Inventory inventory = Inventory.builder().id(1L).product(product).quantity(10).build();
    when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

    inventoryService.deduct(1L, 3);

    assertThat(inventory.getQuantity()).isEqualTo(7);
  }

  @Test
  void restock_shouldIncreaseQuantity_whenInventoryExists() {
    Product product =
        Product.builder().id(1L).name("iPhone").sku("SKU").price(new BigDecimal("100")).build();
    Inventory inventory = Inventory.builder().id(1L).product(product).quantity(10).build();
    when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

    inventoryService.restock(1L, 5);

    assertThat(inventory.getQuantity()).isEqualTo(15);
  }

  @Test
  void deduct_shouldThrow_whenInventoryNotFound() {
    when(inventoryRepository.findByProductId(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> inventoryService.deduct(99L, 1))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.INVENTORY_NOT_FOUND);
  }
}
