package com.craftistan.wishlist.service;

import com.craftistan.common.exception.ResourceNotFoundException;
import com.craftistan.product.dto.ProductDto;
import com.craftistan.product.entity.Product;
import com.craftistan.product.repository.ProductRepository;
import com.craftistan.wishlist.entity.WishlistItem;
import com.craftistan.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public List<ProductDto> getWishlist(String userId) {
        List<WishlistItem> items = wishlistRepository.findByUserId(userId);

        return items.stream()
                .map(item -> productRepository.findById(item.getProductId()).orElse(null))
                .filter(product -> product != null && product.getIsActive())
                .map(this::toProductDto)
                .toList();
    }

    @Transactional
    public void addToWishlist(String userId, Long productId) {
        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        // Check if already in wishlist
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            return; // Already in wishlist, no action needed
        }

        WishlistItem item = WishlistItem.builder()
                .userId(userId)
                .productId(productId)
                .build();

        wishlistRepository.save(item);
    }

    @Transactional
    public void removeFromWishlist(String userId, Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public void clearWishlist(String userId) {
        wishlistRepository.deleteByUserId(userId);
    }

    public boolean isInWishlist(String userId, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    public long getWishlistCount(String userId) {
        return wishlistRepository.countByUserId(userId);
    }

    private ProductDto toProductDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .category(product.getCategory())
                .style(product.getStyle())
                .image(product.getImage())
                .images(product.getImages())
                .artisanId(product.getArtisanId())
                .artisanName(product.getArtisanName())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .isNew(product.getIsNew())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
