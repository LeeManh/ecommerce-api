package com.ecommerce.backend.inventory.entity;

import com.ecommerce.backend.common.entity.BaseEntity;
import com.ecommerce.backend.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "inventories")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Inventory extends BaseEntity {

  @OneToOne
  @JoinColumn(name = "product_id", nullable = false, unique = true)
  private Product product;

  @Column(nullable = false)
  private int quantity;

  @Version private Long version;
}
