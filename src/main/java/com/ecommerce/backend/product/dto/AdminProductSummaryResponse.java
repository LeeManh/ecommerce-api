package com.ecommerce.backend.product.dto;

import com.ecommerce.backend.product.entity.Product;
import com.ecommerce.backend.product.entity.ProductImage;
import java.math.BigDecimal;
import java.util.Comparator;

public record AdminProductSummaryResponse(
    Long id,
    String name,
    String sku,
    BigDecimal price,
    boolean active,
    String thumbnailUrl,
    int quantity) {

  public static AdminProductSummaryResponse from(Product product, int quantity) {
    String thumbnail =
        product.getImages().stream()
            .min(Comparator.comparingInt(ProductImage::getDisplayOrder))
            .map(ProductImage::getImageUrl)
            .orElse(null);
    return new AdminProductSummaryResponse(
        product.getId(),
        product.getName(),
        product.getSku(),
        product.getPrice(),
        product.isActive(),
        thumbnail,
        quantity);
  }
}
