package com.craftistan.wishlist.controller;

import com.craftistan.common.dto.ApiResponse;
import com.craftistan.product.dto.ProductDto;
import com.craftistan.user.entity.User;
import com.craftistan.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Wishlist management endpoints")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Get user's wishlist")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getWishlist(
            @AuthenticationPrincipal User user) {
        List<ProductDto> wishlist = wishlistService.getWishlist(user.getId());
        return ResponseEntity.ok(ApiResponse.success(wishlist));
    }

    @PostMapping("/{productId}")
    @Operation(summary = "Add product to wishlist")
    public ResponseEntity<ApiResponse<Void>> addToWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal User user) {
        wishlistService.addToWishlist(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success(null, "Added to wishlist"));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove product from wishlist")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal User user) {
        wishlistService.removeFromWishlist(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success(null, "Removed from wishlist"));
    }

    @DeleteMapping
    @Operation(summary = "Clear entire wishlist")
    public ResponseEntity<ApiResponse<Void>> clearWishlist(
            @AuthenticationPrincipal User user) {
        wishlistService.clearWishlist(user.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Wishlist cleared"));
    }

    @GetMapping("/check/{productId}")
    @Operation(summary = "Check if product is in wishlist")
    public ResponseEntity<ApiResponse<Boolean>> checkWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal User user) {
        boolean inWishlist = wishlistService.isInWishlist(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success(inWishlist));
    }
}
