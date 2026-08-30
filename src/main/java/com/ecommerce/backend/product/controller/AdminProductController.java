package com.ecommerce.backend.product.controller;

import com.ecommerce.backend.common.response.ApiResponse;
import com.ecommerce.backend.product.dto.AdminProductSummaryResponse;
import com.ecommerce.backend.product.dto.ProductRequest;
import com.ecommerce.backend.product.dto.ProductResponse;
import com.ecommerce.backend.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

  private final ProductService productService;

  @GetMapping
  public ApiResponse<Page<AdminProductSummaryResponse>> getProducts(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) Boolean active,
      Pageable pageable) {
    return ApiResponse.success(
        productService.searchForAdmin(keyword, categoryId, active, pageable), "OK");
  }

  @PostMapping
  public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
    return ApiResponse.success(productService.create(request), "Product created");
  }

  @PutMapping("/{id}")
  public ApiResponse<ProductResponse> update(
      @PathVariable Long id, @Valid @RequestBody ProductRequest request) {
    return ApiResponse.success(productService.update(id, request), "Product updated");
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    productService.softDelete(id);
    return ApiResponse.success(null, "Product deleted");
  }

  @PostMapping("/{id}/restore")
  public ApiResponse<ProductResponse> restore(@PathVariable Long id) {
    return ApiResponse.success(productService.restore(id), "Product restored");
  }

  @GetMapping("/{id}")
  public ApiResponse<ProductResponse> getById(@PathVariable Long id) {
    return ApiResponse.success(productService.getForAdmin(id), "OK");
  }
}
