package com.ecommerce.backend.product.entity;

import com.ecommerce.backend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@Table(name = "categories")
public class Category extends BaseEntity {
  @Column(unique = true, nullable = false)
  private String name;
}
