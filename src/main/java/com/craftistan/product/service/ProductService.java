package com.craftistan.product.service;

import com.craftistan.common.exception.ResourceNotFoundException;
import com.craftistan.product.dto.CreateProductRequest;
import com.craftistan.product.dto.ProductDto;
import com.craftistan.product.entity.ApprovalStatus;
import com.craftistan.product.entity.Product;
import com.craftistan.product.repository.ProductRepository;
import com.craftistan.translation.dto.TranslatedContent;
import com.craftistan.translation.service.TranslationService;
import com.craftistan.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final TranslationService translationService;

    // ============================================
    // PUBLIC GET methods - APPROVED products only
    // ============================================

    public Page<ProductDto> getAllProducts(Pageable pageable, String language) {
        return productRepository.findByIsActiveTrueAndApprovalStatus(ApprovalStatus.APPROVED, pageable)
                .map(p -> toDto(p, language));
    }

    public Page<ProductDto> getAllProducts(Pageable pageable) {
        return getAllProducts(pageable, null);
    }

    public Page<ProductDto> getProductsByCategory(String category, Pageable pageable, String language) {
        return productRepository.findByCategoryAndIsActiveTrueAndApprovalStatus(
                category, ApprovalStatus.APPROVED, pageable)
                .map(p -> toDto(p, language));
    }

    public Page<ProductDto> getProductsByCategory(String category, Pageable pageable) {
        return getProductsByCategory(category, pageable, null);
    }

    public Page<ProductDto> searchProducts(String query, Pageable pageable, String language) {
        return productRepository.searchProducts(query, pageable)
                .map(p -> toDto(p, language));
    }

    public Page<ProductDto> searchProducts(String query, Pageable pageable) {
        return searchProducts(query, pageable, null);
    }

    public Page<ProductDto> filterProducts(
            String category,
            String style,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable,
            String language) {
        return productRepository.findByFilters(category, style, minPrice, maxPrice, pageable)
                .map(p -> toDto(p, language));
    }

    public Page<ProductDto> filterProducts(
            String category,
            String style,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {
        return filterProducts(category, style, minPrice, maxPrice, pageable, null);
    }

    public ProductDto getProductById(Long id, String language) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return toDto(product, language);
    }

    public ProductDto getProductById(Long id) {
        return getProductById(id, null);
    }

    public Page<ProductDto> getArtisanProducts(String artisanId, Pageable pageable, String language) {
        return productRepository.findByArtisanIdAndIsActiveTrue(artisanId, pageable)
                .map(p -> toDto(p, language));
    }

    public Page<ProductDto> getArtisanProducts(String artisanId, Pageable pageable) {
        return getArtisanProducts(artisanId, pageable, null);
    }

    // ============================================
    // CREATE/UPDATE/DELETE methods
    // ============================================

    @Transactional
    public ProductDto createProduct(CreateProductRequest request, User artisan) {
        log.info("Creating product: name={}, category={}, artisanId={}",
                request.getName(), request.getCategory(), artisan.getId());

        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .category(request.getCategory())
                .style(request.getStyle())
                .images(request.getImages())
                .stock(request.getStock())
                .artisanId(artisan.getId())
                .artisanName(artisan.getName())
                .originalLanguage("en") // Default, could be from request
                .build();

        log.debug("Product entity before save: {}", product);
        Product saved = productRepository.save(product);
        log.info("Product saved successfully with ID: {}", saved.getId());

        // Trigger async translation
        translationService.translateProductAsync(saved);
        log.info("Translation triggered for product ID: {}", saved.getId());

        // Verify immediately after save
        long count = productRepository.count();
        log.info("Total products in database after save: {}", count);

        return toDto(saved, null);
    }

    @Transactional
    public ProductDto updateProduct(Long id, CreateProductRequest request, User artisan) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Check ownership
        if (!product.getArtisanId().equals(artisan.getId())) {
            throw new RuntimeException("You can only edit your own products");
        }

        // Check if name or description changed - need re-translation
        boolean needsTranslation = !product.getName().equals(request.getName()) ||
                !String.valueOf(product.getDescription()).equals(String.valueOf(request.getDescription()));

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setStyle(request.getStyle());
        if (request.getImages() != null) {
            product.setImages(request.getImages());
        }
        product.setStock(request.getStock());

        Product saved = productRepository.save(product);

        // Re-translate if content changed
        if (needsTranslation) {
            saved.setTranslations(null); // Clear old translations
            productRepository.save(saved);
            translationService.translateProductAsync(saved);
            log.info("Re-translation triggered for product ID: {}", saved.getId());
        }

        return toDto(saved, null);
    }

    @Transactional
    public void deleteProduct(Long id, User artisan) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (!product.getArtisanId().equals(artisan.getId())) {
            throw new RuntimeException("You can only delete your own products");
        }

        product.setIsActive(false);
        productRepository.save(product);
    }

    // ============================================
    // UTILITY METHODS
    // ============================================
    
    @Transactional
    public void triggerAllTranslations() {
        List<Product> products = productRepository.findAll();
        long count = 0;
        for (Product product : products) {
            if (product.getTranslations() == null || product.getTranslations().isEmpty() || product.getTranslations().equals("{}")) {
                translationService.translateProductAsync(product);
                count++;
            }
        }
        log.info("Triggered mass translation backfill for {} products", count);
    }

    // ============================================
    // DTO conversion with translation support
    // ============================================

    private ProductDto toDto(Product product, String language) {
        String name = product.getName();
        String description = product.getDescription();

        // Apply translation if language specified and translations available
        if (language != null && !language.isEmpty() && product.getTranslations() != null) {
            TranslatedContent translated = translationService.getTranslation(
                    product.getTranslations(), language);
            if (translated != null) {
                if (translated.getName() != null && !translated.getName().isEmpty()) {
                    name = translated.getName();
                }
                if (translated.getDescription() != null && !translated.getDescription().isEmpty()) {
                    description = translated.getDescription();
                }
            }
        }

        return ProductDto.builder()
                .id(product.getId())
                .name(name)
                .price(product.getPrice())
                .description(description)
                .category(product.getCategory())
                .style(product.getStyle())
                .image(product.getImage())
                .images(product.getImages())
                .stock(product.getStock())
                .artisanId(product.getArtisanId())
                .artisanName(product.getArtisanName())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .isNew(product.getIsNew())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
