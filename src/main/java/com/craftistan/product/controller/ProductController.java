package com.craftistan.product.controller;

import com.craftistan.common.dto.ApiResponse;
import com.craftistan.product.dto.ProductDto;
import com.craftistan.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products with optional filters")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getProducts(
            @Parameter(description = "Language code for translations (en, ur, pa, sd, ps, bal)") @RequestParam(required = false, defaultValue = "en") String lang,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ProductDto> products;

        if (search != null && !search.isBlank()) {
            products = productService.searchProducts(search, pageable, lang);
        } else if (category != null || style != null || minPrice != null || maxPrice != null) {
            products = productService.filterProducts(category, style, minPrice, maxPrice, pageable, lang);
        } else {
            products = productService.getAllProducts(pageable, lang);
        }

        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductDto>> getProduct(
            @PathVariable Long id,
            @Parameter(description = "Language code for translations (en, ur, pa, sd, ps, bal)") @RequestParam(required = false, defaultValue = "en") String lang) {
        ProductDto product = productService.getProductById(id, lang);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getByCategory(
            @PathVariable String category,
            @Parameter(description = "Language code for translations (en, ur, pa, sd, ps, bal)") @RequestParam(required = false, defaultValue = "en") String lang,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductDto> products = productService.getProductsByCategory(category, pageable, lang);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/artisan/{artisanId}")
    @Operation(summary = "Get products by artisan ID")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getArtisanProducts(
            @PathVariable String artisanId,
            @Parameter(description = "Language code for translations") @RequestParam(required = false, defaultValue = "en") String lang,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductDto> products = productService.getArtisanProducts(artisanId, pageable, lang);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PostMapping("/translate-all")
    @Operation(summary = "Backfill translations for all non-translated products (Admin/System)")
    public ResponseEntity<ApiResponse<Void>> translateAll() {
        productService.triggerAllTranslations();
        return ResponseEntity.ok(ApiResponse.success(null, "Mass translation backfill triggered in the background."));
    }
}
