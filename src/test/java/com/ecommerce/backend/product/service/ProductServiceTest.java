package com.ecommerce.backend.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.backend.common.exception.ApiException;
import com.ecommerce.backend.common.exception.ErrorCode;
import com.ecommerce.backend.inventory.service.InventoryService;
import com.ecommerce.backend.product.dto.AdminProductSummaryResponse;
import com.ecommerce.backend.product.dto.ProductRequest;
import com.ecommerce.backend.product.dto.ProductResponse;
import com.ecommerce.backend.product.entity.Category;
import com.ecommerce.backend.product.entity.Product;
import com.ecommerce.backend.product.repository.CategoryRepository;
import com.ecommerce.backend.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @Mock private ProductRepository productRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private InventoryService inventoryService;

  @InjectMocks private ProductService productService;

  @Test
  void create_shouldSaveProductAndCreateInventory_whenSkuIsUnique() {
    ProductRequest request =
        new ProductRequest(
            "iPhone 15", "IPHONE-15", "desc", new BigDecimal("29990000"), Set.of(1L), List.of());
    Category category = Category.builder().id(1L).name("Điện thoại").build();

    when(productRepository.existsBySku("IPHONE-15")).thenReturn(false);
    when(categoryRepository.findAllById(Set.of(1L))).thenReturn(List.of(category));
    when(productRepository.save(any(Product.class)))
        .thenAnswer(
            invocation -> {
              Product p = invocation.getArgument(0);
              p.setId(1L);
              return p;
            });

    ProductResponse response = productService.create(request);

    assertThat(response.name()).isEqualTo("iPhone 15");
    assertThat(response.sku()).isEqualTo("IPHONE-15");
    assertThat(response.categories()).containsExactly("Điện thoại");
    verify(inventoryService).createForProduct(any(Product.class));
  }

  @Test
  void create_shouldThrow_whenSkuAlreadyExists() {
    ProductRequest request =
        new ProductRequest(
            "iPhone 15", "IPHONE-15", "desc", new BigDecimal("100"), Set.of(1L), List.of());
    when(productRepository.existsBySku("IPHONE-15")).thenReturn(true);

    assertThatThrownBy(() -> productService.create(request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.SKU_ALREADY_EXISTS);

    verify(productRepository, never()).save(any());
  }

  @Test
  void create_shouldThrow_whenSomeCategoryNotFound() {
    ProductRequest request =
        new ProductRequest(
            "iPhone 15", "IPHONE-15", "desc", new BigDecimal("100"), Set.of(1L, 2L), List.of());
    when(productRepository.existsBySku("IPHONE-15")).thenReturn(false);
    when(categoryRepository.findAllById(Set.of(1L, 2L)))
        .thenReturn(List.of(Category.builder().id(1L).name("Điện thoại").build()));

    assertThatThrownBy(() -> productService.create(request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
  }

  @Test
  void update_shouldThrow_whenProductNotFound() {
    ProductRequest request =
        new ProductRequest("Name", "SKU", "desc", new BigDecimal("100"), Set.of(1L), List.of());
    when(productRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.update(99L, request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
  }

  @Test
  void update_shouldThrow_whenNewSkuConflictsWithAnotherProduct() {
    Product existing =
        Product.builder().id(1L).name("Old").sku("OLD-SKU").price(new BigDecimal("100")).build();
    ProductRequest request =
        new ProductRequest("New", "NEW-SKU", "desc", new BigDecimal("200"), Set.of(1L), List.of());

    when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(productRepository.existsBySku("NEW-SKU")).thenReturn(true);

    assertThatThrownBy(() -> productService.update(1L, request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.SKU_ALREADY_EXISTS);
  }

  @Test
  void softDelete_shouldSetActiveFalse_whenProductExists() {
    Product product =
        Product.builder()
            .id(1L)
            .name("Name")
            .sku("SKU")
            .price(new BigDecimal("100"))
            .active(true)
            .build();
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));

    productService.softDelete(1L);

    assertThat(product.isActive()).isFalse();
  }

  @Test
  void softDelete_shouldThrow_whenProductNotFound() {
    when(productRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.softDelete(99L))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
  }

  @Test
  void getPublicDetail_shouldThrow_whenProductInactiveOrMissing() {
    when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.getPublicDetail(1L))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
  }

  @Test
  void restore_shouldSetActiveTrue_whenProductExists() {
    Product product =
        Product.builder()
            .id(1L)
            .name("Name")
            .sku("SKU")
            .price(new BigDecimal("100"))
            .active(false)
            .build();
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));

    ProductResponse response = productService.restore(1L);

    assertThat(product.isActive()).isTrue();
    assertThat(response.active()).isTrue();
  }

  @Test
  void restore_shouldThrow_whenProductNotFound() {
    when(productRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.restore(99L))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
  }

  @Test
  void searchForAdmin_shouldReturnMappedPage_includingInactiveProducts() {
    Product product =
        Product.builder()
            .id(1L)
            .name("Old Stock")
            .sku("OLD-STOCK")
            .price(new BigDecimal("100"))
            .active(false)
            .build();
    Pageable pageable = Pageable.unpaged();
    Page<Product> page = new PageImpl<>(List.of(product));

    when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

    Page<AdminProductSummaryResponse> result =
        productService.searchForAdmin(null, null, false, pageable);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).sku()).isEqualTo("OLD-STOCK");
    assertThat(result.getContent().get(0).active()).isFalse();
  }
}
