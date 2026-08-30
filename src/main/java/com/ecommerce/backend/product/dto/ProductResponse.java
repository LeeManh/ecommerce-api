package com.ecommerce.backend.product.dto;

import com.ecommerce.backend.product.entity.Category;
import com.ecommerce.backend.product.entity.Product;
import com.ecommerce.backend.product.entity.ProductImage;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record ProductResponse(
    Long id,
    String name,
    String sku,
    String description,
    BigDecimal price,
    boolean active,
    Set<String> categories,
    List<String> images,
    int quantity) {

  public static ProductResponse from(Product product, int quantity) {
    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getSku(),
        product.getDescription(),
        product.getPrice(),
        product.isActive(),
        product.getCategories().stream().map(Category::getName).collect(Collectors.toSet()),
        product.getImages().stream()
            .sorted(Comparator.comparingInt(ProductImage::getDisplayOrder))
            .map(ProductImage::getImageUrl)
            .toList(),
        quantity);
  }

  // Kept out of the @Cacheable path in ProductService.getPublicDetail — quantity must always be
  // live, never baked into a cached response, so it's merged in after the cache lookup.
  public ProductResponse withQuantity(int quantity) {
    return new ProductResponse(
        id, name, sku, description, price, active, categories, images, quantity);
  }
}
