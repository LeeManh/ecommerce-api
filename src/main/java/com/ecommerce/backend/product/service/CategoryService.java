package com.ecommerce.backend.product.service;

import com.ecommerce.backend.common.exception.ApiException;
import com.ecommerce.backend.common.exception.ErrorCode;
import com.ecommerce.backend.product.dto.CategoryRequest;
import com.ecommerce.backend.product.dto.CategoryResponse;
import com.ecommerce.backend.product.entity.Category;
import com.ecommerce.backend.product.repository.CategoryRepository;
import com.ecommerce.backend.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;

  public List<CategoryResponse> getAll() {
    return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
  }

  @Transactional
  public CategoryResponse create(CategoryRequest request) {
    if (categoryRepository.existsByName(request.name())) {
      throw new ApiException(
          ErrorCode.CATEGORY_ALREADY_EXISTS, "Category already exists: " + request.name());
    }

    Category category = Category.builder().name(request.name()).build();
    return CategoryResponse.from(categoryRepository.save(category));
  }

  @Transactional
  public CategoryResponse update(Long id, CategoryRequest request) {
    Category category = getOrThrow(id);

    if (!category.getName().equals(request.name())
        && categoryRepository.existsByName(request.name())) {
      throw new ApiException(
          ErrorCode.CATEGORY_ALREADY_EXISTS, "Category already exists: " + request.name());
    }

    category.setName(request.name());
    return CategoryResponse.from(category);
  }

  @Transactional
  public void delete(Long id) {
    Category category = getOrThrow(id);

    if (productRepository.existsByCategoriesId(id)) {
      throw new ApiException(
          ErrorCode.CATEGORY_IN_USE, "Category is still assigned to products: " + id);
    }

    categoryRepository.delete(category);
  }

  private Category getOrThrow(Long id) {
    return categoryRepository
        .findById(id)
        .orElseThrow(
            () -> new ApiException(ErrorCode.CATEGORY_NOT_FOUND, "Category not found: " + id));
  }
}
