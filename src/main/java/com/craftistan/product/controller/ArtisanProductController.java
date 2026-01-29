package com.craftistan.product.controller;

import com.craftistan.common.dto.ApiResponse;
import com.craftistan.product.dto.CreateProductRequest;
import com.craftistan.product.dto.ProductDto;
import com.craftistan.product.service.ProductService;
import com.craftistan.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/artisan/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ARTISAN')")
@Tag(name = "Artisan Products", description = "Product management for artisans")
public class ArtisanProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get artisan's products")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getMyProducts(
            @AuthenticationPrincipal User artisan,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductDto> products = productService.getArtisanProducts(artisan.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal User artisan) {
        ProductDto product = productService.createProduct(request, artisan);
        return ResponseEntity.ok(ApiResponse.success(product, "Product created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal User artisan) {
        ProductDto product = productService.updateProduct(id, request, artisan);
        return ResponseEntity.ok(ApiResponse.success(product, "Product updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal User artisan) {
        productService.deleteProduct(id, artisan);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }
}
