package com.ecommerce.backend.product.dto;

import com.ecommerce.backend.product.entity.Category;

public record CategoryResponse(Long id, String name) {

  public static CategoryResponse from(Category category) {
    return new CategoryResponse(category.getId(), category.getName());
  }
}
