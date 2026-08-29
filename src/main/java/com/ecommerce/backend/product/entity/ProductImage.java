package com.ecommerce.backend.product.entity;

import com.ecommerce.backend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ProductImage extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "image_url", nullable = false)
  private String imageUrl;

  @Column(name = "display_order", nullable = false)
  @Builder.Default
  private int displayOrder = 0;
}
