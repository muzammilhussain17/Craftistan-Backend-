package com.craftistan.config;

import com.craftistan.product.entity.Category;
import com.craftistan.product.entity.Product;
import com.craftistan.product.repository.CategoryRepository;
import com.craftistan.product.repository.ProductRepository;
import com.craftistan.user.entity.Role;
import com.craftistan.user.entity.User;
import com.craftistan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Initialize categories
        if (categoryRepository.count() == 0) {
            initCategories();
        }

        // Initialize users
        if (userRepository.count() == 0) {
            initUsers();
        }

        // Initialize sample products
        if (productRepository.count() == 0) {
            initProducts();
        }

        log.info("Data initialization complete!");
    }

    private void initCategories() {
        List<Category> categories = List.of(
                Category.builder().slug("new-arrivals").name("New Arrivals").icon("✨").sortOrder(1).build(),
                Category.builder().slug("home-decor").name("Home Decor").icon("🏠").sortOrder(2).build(),
                Category.builder().slug("textiles").name("Textiles").icon("🧵").sortOrder(3).build(),
                Category.builder().slug("jewelry").name("Jewelry").icon("💍").sortOrder(4).build());
        categoryRepository.saveAll(categories);
        log.info("Created {} categories", categories.size());
    }

    private void initUsers() {
        User buyer = User.builder()
                .name("Buyer User")
                .email("buyer@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.BUYER)
                .build();

        User artisan = User.builder()
                .name("Fatima Zahra")
                .email("artisan@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ARTISAN)
                .build();

        userRepository.saveAll(List.of(buyer, artisan));
        log.info("Created demo users: buyer@example.com, artisan@example.com (password: password123)");
    }

    private void initProducts() {
        User artisan = userRepository.findByEmail("artisan@example.com").orElse(null);
        if (artisan == null)
            return;

        List<Product> products = List.of(
                Product.builder()
                        .name("Hand-Woven Silk Scarf")
                        .price(new BigDecimal("8500"))
                        .category("new-arrivals")
                        .style("modern")
                        .images(List.of("https://images.unsplash.com/photo-1601924994987-69e26d50dc26?w=400"))
                        .artisanId(artisan.getId())
                        .artisanName(artisan.getName())
                        .rating(4.8)
                        .stock(15)
                        .build(),
                Product.builder()
                        .name("Ceramic Tea Set")
                        .price(new BigDecimal("12000"))
                        .category("home-decor")
                        .style("classic")
                        .images(List.of("https://images.unsplash.com/photo-1578749556568-bc2c40e68b61?w=400"))
                        .artisanId(artisan.getId())
                        .artisanName(artisan.getName())
                        .rating(4.9)
                        .stock(8)
                        .build(),
                Product.builder()
                        .name("Pashmina Shawl")
                        .price(new BigDecimal("22000"))
                        .category("textiles")
                        .style("classic")
                        .images(List.of("https://images.unsplash.com/photo-1601924994987-69e26d50dc26?w=400"))
                        .artisanId(artisan.getId())
                        .artisanName(artisan.getName())
                        .rating(5.0)
                        .stock(5)
                        .build(),
                Product.builder()
                        .name("Kundan Necklace Set")
                        .price(new BigDecimal("28000"))
                        .category("jewelry")
                        .style("classic")
                        .images(List.of("https://images.unsplash.com/photo-1611591437281-460bfbe1220a?w=400"))
                        .artisanId(artisan.getId())
                        .artisanName(artisan.getName())
                        .rating(4.9)
                        .isNew(true)
                        .stock(3)
                        .build());

        productRepository.saveAll(products);
        log.info("Created {} sample products", products.size());
    }
}
