package com.ecommerce.backend.product.entity;

import com.ecommerce.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@Table(name = "products")
public class Product extends BaseEntity {
  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String sku;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal price;

  @Builder.Default private boolean active = true;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "product_categories",
      joinColumns = @JoinColumn(name = "product_id"),
      inverseJoinColumns = @JoinColumn(name = "category_id"))
  @Builder.Default
  private Set<Category> categories = new HashSet<>();

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("displayOrder ASC")
  @Builder.Default
  private List<ProductImage> images = new ArrayList<>();
}
