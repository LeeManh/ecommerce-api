package com.ecommerce.backend.product.dto;

import com.ecommerce.backend.product.entity.Product;
import com.ecommerce.backend.product.entity.ProductImage;
import java.math.BigDecimal;
import java.util.Comparator;

public record ProductSummaryResponse(
    Long id, String name, BigDecimal price, String thumbnailUrl, int quantity) {

  public static ProductSummaryResponse from(Product product, int quantity) {
    String thumbnail =
        product.getImages().stream()
            .min(Comparator.comparingInt(ProductImage::getDisplayOrder))
            .map(ProductImage::getImageUrl)
            .orElse(null);
    return new ProductSummaryResponse(
        product.getId(), product.getName(), product.getPrice(), thumbnail, quantity);
  }
}
