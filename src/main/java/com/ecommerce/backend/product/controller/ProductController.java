package com.ecommerce.backend.product.controller;

import com.ecommerce.backend.common.response.ApiResponse;
import com.ecommerce.backend.product.dto.ProductResponse;
import com.ecommerce.backend.product.dto.ProductSummaryResponse;
import com.ecommerce.backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping
  public ApiResponse<Page<ProductSummaryResponse>> search(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long categoryId,
      Pageable pageable) {
    return ApiResponse.success(productService.search(keyword, categoryId, pageable), "OK");
  }

  @GetMapping("/{id}")
  public ApiResponse<ProductResponse> getById(@PathVariable Long id) {
    ProductResponse detail = productService.getPublicDetail(id);
    return ApiResponse.success(detail.withQuantity(productService.getStockQuantity(id)), "OK");
  }
}
