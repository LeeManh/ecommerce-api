package com.ecommerce.backend.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.backend.common.exception.ApiException;
import com.ecommerce.backend.common.exception.ErrorCode;
import com.ecommerce.backend.product.dto.CategoryRequest;
import com.ecommerce.backend.product.dto.CategoryResponse;
import com.ecommerce.backend.product.entity.Category;
import com.ecommerce.backend.product.repository.CategoryRepository;
import com.ecommerce.backend.product.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock private CategoryRepository categoryRepository;
  @Mock private ProductRepository productRepository;

  @InjectMocks private CategoryService categoryService;

  @Test
  void getAll_shouldReturnAllCategories() {
    when(categoryRepository.findAll())
        .thenReturn(List.of(Category.builder().id(1L).name("Điện thoại").build()));

    List<CategoryResponse> result = categoryService.getAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).name()).isEqualTo("Điện thoại");
  }

  @Test
  void create_shouldSaveCategory_whenNameIsUnique() {
    CategoryRequest request = new CategoryRequest("Laptop");
    when(categoryRepository.existsByName("Laptop")).thenReturn(false);
    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(
            invocation -> {
              Category category = invocation.getArgument(0);
              category.setId(1L);
              return category;
            });

    CategoryResponse response = categoryService.create(request);

    assertThat(response.name()).isEqualTo("Laptop");
  }

  @Test
  void create_shouldThrow_whenNameAlreadyExists() {
    CategoryRequest request = new CategoryRequest("Laptop");
    when(categoryRepository.existsByName("Laptop")).thenReturn(true);

    assertThatThrownBy(() -> categoryService.create(request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.CATEGORY_ALREADY_EXISTS);

    verify(categoryRepository, never()).save(any());
  }

  @Test
  void update_shouldThrow_whenCategoryNotFound() {
    CategoryRequest request = new CategoryRequest("Laptop");
    when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.update(99L, request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
  }

  @Test
  void update_shouldThrow_whenNewNameConflictsWithAnotherCategory() {
    Category existing = Category.builder().id(1L).name("Laptop").build();
    CategoryRequest request = new CategoryRequest("Điện thoại");

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(categoryRepository.existsByName("Điện thoại")).thenReturn(true);

    assertThatThrownBy(() -> categoryService.update(1L, request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.CATEGORY_ALREADY_EXISTS);
  }

  @Test
  void delete_shouldRemoveCategory_whenNotAssignedToAnyProduct() {
    Category category = Category.builder().id(1L).name("Laptop").build();
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.existsByCategoriesId(1L)).thenReturn(false);

    categoryService.delete(1L);

    verify(categoryRepository).delete(category);
  }

  @Test
  void delete_shouldThrow_whenCategoryStillAssignedToProducts() {
    Category category = Category.builder().id(1L).name("Laptop").build();
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.existsByCategoriesId(1L)).thenReturn(true);

    assertThatThrownBy(() -> categoryService.delete(1L))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.CATEGORY_IN_USE);

    verify(categoryRepository, never()).delete(any());
  }

  @Test
  void delete_shouldThrow_whenCategoryNotFound() {
    when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.delete(99L))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
  }
}
