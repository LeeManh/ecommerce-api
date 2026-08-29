package com.ecommerce.backend.seeder;

import com.ecommerce.backend.auth.entity.Role;
import com.ecommerce.backend.auth.repository.RoleRepository;
import com.ecommerce.backend.inventory.service.InventoryService;
import com.ecommerce.backend.product.dto.ProductRequest;
import com.ecommerce.backend.product.dto.ProductResponse;
import com.ecommerce.backend.product.entity.Category;
import com.ecommerce.backend.product.repository.CategoryRepository;
import com.ecommerce.backend.product.service.ProductService;
import com.ecommerce.backend.user.entity.User;
import com.ecommerce.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final CategoryRepository categoryRepository;
  private final ProductService productService;
  private final InventoryService inventoryService;

  @Override
  @Transactional
  public void run(String... args) {
    if (userRepository.count() > 0) {
      log.info("Data already seeded, skipping.");
      return;
    }

    seedUsers();
    seedCatalog();
  }

  private void seedUsers() {
    Role adminRole =
        roleRepository
            .findByName("ROLE_ADMIN")
            .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found — check migration"));

    User admin =
        User.builder()
            .email("admin@shop.com")
            .password(passwordEncoder.encode("Admin@123"))
            .fullName("Shop Admin")
            .roles(Set.of(adminRole))
            .build();
    userRepository.save(admin);

    log.info("Seeded admin user: admin@shop.com / Admin@123");
  }

  private void seedCatalog() {
    Category phone = categoryRepository.save(Category.builder().name("Điện thoại").build());
    Category laptop = categoryRepository.save(Category.builder().name("Laptop").build());
    Category accessory = categoryRepository.save(Category.builder().name("Phụ kiện").build());

    seedProduct(
        "iPhone 15 Pro Max",
        "IPHONE-15-PM-256",
        "Flagship iPhone với chip A17 Pro, camera 48MP.",
        new BigDecimal("29990000"),
        Set.of(phone.getId()),
        List.of("https://picsum.photos/seed/iphone15/600/600"),
        50);

    seedProduct(
        "MacBook Air M3",
        "MACBOOK-AIR-M3-13",
        "Laptop mỏng nhẹ, chip M3, pin 18 giờ.",
        new BigDecimal("27990000"),
        Set.of(laptop.getId()),
        List.of("https://picsum.photos/seed/macbook/600/600"),
        30);

    seedProduct(
        "Samsung Galaxy S24",
        "GALAXY-S24-256",
        "Flagship Android với AI tích hợp.",
        new BigDecimal("22990000"),
        Set.of(phone.getId()),
        List.of("https://picsum.photos/seed/galaxy/600/600"),
        40);

    seedProduct(
        "Dell XPS 13",
        "DELL-XPS-13-2024",
        "Laptop Windows cao cấp, màn hình InfinityEdge.",
        new BigDecimal("31990000"),
        Set.of(laptop.getId()),
        List.of("https://picsum.photos/seed/dellxps/600/600"),
        20);

    seedProduct(
        "AirPods Pro 2",
        "AIRPODS-PRO-2",
        "Tai nghe chống ồn chủ động, chip H2.",
        new BigDecimal("5990000"),
        Set.of(accessory.getId()),
        List.of("https://picsum.photos/seed/airpods/600/600"),
        100);

    log.info("Seeded 3 categories and 5 products");
  }

  private void seedProduct(
      String name,
      String sku,
      String description,
      BigDecimal price,
      Set<Long> categoryIds,
      List<String> imageUrls,
      int quantity) {
    ProductRequest request =
        new ProductRequest(name, sku, description, price, categoryIds, imageUrls);
    ProductResponse product = productService.create(request);
    inventoryService.updateQuantity(product.id(), quantity);
  }
}
