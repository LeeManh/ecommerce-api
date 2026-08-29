package com.ecommerce.backend.product.controller;

import com.ecommerce.backend.common.response.ApiResponse;
import com.ecommerce.backend.product.dto.CategoryRequest;
import com.ecommerce.backend.product.dto.CategoryResponse;
import com.ecommerce.backend.product.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

  private final CategoryService categoryService;

  @PostMapping
  public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
    return ApiResponse.success(categoryService.create(request), "Category created");
  }

  @PutMapping("/{id}")
  public ApiResponse<CategoryResponse> update(
      @PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
    return ApiResponse.success(categoryService.update(id, request), "Category updated");
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    categoryService.delete(id);
    return ApiResponse.success(null, "Category deleted");
  }
}
