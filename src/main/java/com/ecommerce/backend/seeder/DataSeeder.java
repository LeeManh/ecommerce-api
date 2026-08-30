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
    Category tablet = categoryRepository.save(Category.builder().name("Máy tính bảng").build());
    Category smartwatch =
        categoryRepository.save(Category.builder().name("Đồng hồ thông minh").build());
    Category speaker = categoryRepository.save(Category.builder().name("Loa & Âm thanh").build());
    Category gaming = categoryRepository.save(Category.builder().name("Gaming").build());

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

    seedProduct(
        "Google Pixel 8 Pro",
        "PIXEL-8-PRO-256",
        "Camera AI vượt trội, chip Tensor G3.",
        new BigDecimal("21990000"),
        Set.of(phone.getId()),
        List.of("https://picsum.photos/seed/pixel8/600/600"),
        35);

    seedProduct(
        "ASUS ROG Zephyrus G14",
        "ROG-ZEPHYRUS-G14",
        "Laptop gaming mỏng nhẹ, RTX 4060, màn hình OLED 165Hz.",
        new BigDecimal("42990000"),
        Set.of(laptop.getId(), gaming.getId()),
        List.of("https://picsum.photos/seed/rogzephyrus/600/600"),
        15);

    seedProduct(
        "Sony WH-1000XM5",
        "SONY-WH1000XM5",
        "Tai nghe chống ồn hàng đầu, âm thanh Hi-Res.",
        new BigDecimal("8490000"),
        Set.of(accessory.getId()),
        List.of("https://picsum.photos/seed/sonywh1000/600/600"),
        60);

    seedProduct(
        "iPad Pro M4",
        "IPAD-PRO-M4-256",
        "Máy tính bảng cao cấp, chip M4, màn hình Ultra Retina XDR.",
        new BigDecimal("26990000"),
        Set.of(tablet.getId()),
        List.of("https://picsum.photos/seed/ipadpro/600/600"),
        25);

    seedProduct(
        "Xiaomi Pad 6",
        "XIAOMI-PAD-6-128",
        "Máy tính bảng giá tốt, màn hình 144Hz.",
        new BigDecimal("7990000"),
        Set.of(tablet.getId()),
        List.of("https://picsum.photos/seed/xiaomipad6/600/600"),
        45);

    seedProduct(
        "Apple Watch Series 9",
        "APPLE-WATCH-S9-45",
        "Đồng hồ thông minh, chip S9, theo dõi sức khoẻ toàn diện.",
        new BigDecimal("10990000"),
        Set.of(smartwatch.getId()),
        List.of("https://picsum.photos/seed/applewatch9/600/600"),
        40);

    seedProduct(
        "Samsung Galaxy Watch 6",
        "GALAXY-WATCH-6-44",
        "Đồng hồ thông minh Android, đo điện tâm đồ.",
        new BigDecimal("7490000"),
        Set.of(smartwatch.getId()),
        List.of("https://picsum.photos/seed/galaxywatch6/600/600"),
        50);

    seedProduct(
        "JBL Flip 6",
        "JBL-FLIP-6",
        "Loa bluetooth di động, chống nước IP67.",
        new BigDecimal("2990000"),
        Set.of(speaker.getId()),
        List.of("https://picsum.photos/seed/jblflip6/600/600"),
        70);

    seedProduct(
        "Logitech G Pro X Superlight",
        "LOGITECH-GPROX-SL",
        "Chuột gaming siêu nhẹ, cảm biến HERO 25K.",
        new BigDecimal("2690000"),
        Set.of(gaming.getId(), accessory.getId()),
        List.of("https://picsum.photos/seed/logitechgprox/600/600"),
        80);

    seedProduct(
        "Razer BlackWidow V4",
        "RAZER-BLACKWIDOW-V4",
        "Bàn phím cơ gaming, switch Green, đèn RGB Chroma.",
        new BigDecimal("3490000"),
        Set.of(gaming.getId(), accessory.getId()),
        List.of("https://picsum.photos/seed/razerblackwidow/600/600"),
        55);

    log.info("Seeded 7 categories and 15 products");
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
