package com.ecommerce.backend.product.service;

import com.ecommerce.backend.common.exception.ApiException;
import com.ecommerce.backend.common.exception.ErrorCode;
import com.ecommerce.backend.inventory.service.InventoryService;
import com.ecommerce.backend.product.dto.AdminProductSummaryResponse;
import com.ecommerce.backend.product.dto.ProductRequest;
import com.ecommerce.backend.product.dto.ProductResponse;
import com.ecommerce.backend.product.dto.ProductSummaryResponse;
import com.ecommerce.backend.product.entity.Category;
import com.ecommerce.backend.product.entity.Product;
import com.ecommerce.backend.product.entity.ProductImage;
import com.ecommerce.backend.product.repository.CategoryRepository;
import com.ecommerce.backend.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final InventoryService inventoryService;

  @Transactional
  public ProductResponse create(ProductRequest request) {
    if (productRepository.existsBySku(request.sku())) {
      throw new ApiException(ErrorCode.SKU_ALREADY_EXISTS, "SKU already exists: " + request.sku());
    }

    Product product =
        Product.builder()
            .name(request.name())
            .sku(request.sku())
            .description(request.description())
            .price(request.price())
            .categories(resolveCategories(request.categoryIds()))
            .build();

    product.getImages().addAll(buildImages(request.imageUrls(), product));

    Product saved = productRepository.save(product);
    inventoryService.createForProduct(saved);

    return ProductResponse.from(saved);
  }

  @Transactional
  @CacheEvict(value = "products", key = "#id")
  public ProductResponse update(Long id, ProductRequest request) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(
                () -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + id));

    if (!product.getSku().equals(request.sku()) && productRepository.existsBySku(request.sku())) {
      throw new ApiException(ErrorCode.SKU_ALREADY_EXISTS, "SKU already exists: " + request.sku());
    }

    product.setName(request.name());
    product.setSku(request.sku());
    product.setDescription(request.description());
    product.setPrice(request.price());

    product.getCategories().clear();
    product.getCategories().addAll(resolveCategories(request.categoryIds()));

    product.getImages().clear();
    product.getImages().addAll(buildImages(request.imageUrls(), product));

    return ProductResponse.from(product);
  }

  @Transactional
  @CacheEvict(value = "products", key = "#id")
  public void softDelete(Long id) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(
                () -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + id));
    product.setActive(false);
  }

  @Transactional
  public ProductResponse restore(Long id) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(
                () -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + id));
    product.setActive(true);
    return ProductResponse.from(product);
  }

  public ProductResponse getForAdmin(Long id) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(
                () -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + id));
    return ProductResponse.from(product);
  }

  public Page<ProductSummaryResponse> search(String keyword, Long categoryId, Pageable pageable) {
    return productRepository
        .findAll(ProductSpecification.search(keyword, categoryId), pageable)
        .map(ProductSummaryResponse::from);
  }

  public Page<AdminProductSummaryResponse> searchForAdmin(
      String keyword, Long categoryId, Boolean active, Pageable pageable) {
    return productRepository
        .findAll(ProductSpecification.searchForAdmin(keyword, categoryId, active), pageable)
        .map(AdminProductSummaryResponse::from);
  }

  @Cacheable(value = "products", key = "#id")
  public ProductResponse getPublicDetail(Long id) {
    Product product =
        productRepository
            .findByIdAndActiveTrue(id)
            .orElseThrow(
                () -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + id));
    return ProductResponse.from(product);
  }

  private Set<Category> resolveCategories(Set<Long> categoryIds) {
    List<Category> found = categoryRepository.findAllById(categoryIds);
    if (found.size() != categoryIds.size()) {
      throw new ApiException(ErrorCode.CATEGORY_NOT_FOUND, "One or more categories not found");
    }
    return new HashSet<>(found);
  }

  private List<ProductImage> buildImages(List<String> imageUrls, Product product) {
    if (imageUrls == null) {
      return List.of();
    }
    List<ProductImage> images = new ArrayList<>();
    for (int i = 0; i < imageUrls.size(); i++) {
      images.add(
          ProductImage.builder()
              .product(product)
              .imageUrl(imageUrls.get(i))
              .displayOrder(i)
              .build());
    }
    return images;
  }
}
