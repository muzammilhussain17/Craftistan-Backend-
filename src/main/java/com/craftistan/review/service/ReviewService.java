package com.craftistan.review.service;

import com.craftistan.common.exception.ResourceNotFoundException;
import com.craftistan.product.entity.Product;
import com.craftistan.product.repository.ProductRepository;
import com.craftistan.review.dto.CreateReviewRequest;
import com.craftistan.review.dto.ReviewDto;
import com.craftistan.review.entity.Review;
import com.craftistan.review.repository.ReviewRepository;
import com.craftistan.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public Page<ReviewDto> getProductReviews(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(this::toDto);
    }

    @Transactional
    public ReviewDto createReview(Long productId, CreateReviewRequest request, User user) {
        // Check if product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Check if user already reviewed
        if (reviewRepository.existsByProductIdAndUserId(productId, user.getId())) {
            throw new RuntimeException("You have already reviewed this product");
        }

        Review review = Review.builder()
                .productId(productId)
                .userId(user.getId())
                .userName(user.getName())
                .userAvatar(user.getAvatar())
                .rating(request.getRating())
                .comment(request.getComment())
                .verified(false) // TODO: Check if user has purchased this product
                .build();

        Review saved = reviewRepository.save(review);

        // Update product rating
        updateProductRating(productId);

        return toDto(saved);
    }

    @Transactional
    public ReviewDto updateReview(Long reviewId, CreateReviewRequest request, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getUserId().equals(user.getId())) {
            throw new RuntimeException("You can only edit your own reviews");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);

        // Update product rating
        updateProductRating(review.getProductId());

        return toDto(saved);
    }

    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getUserId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own reviews");
        }

        Long productId = review.getProductId();
        reviewRepository.delete(review);

        // Update product rating
        updateProductRating(productId);
    }

    @Transactional
    public ReviewDto markHelpful(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        review.setHelpful(review.getHelpful() + 1);
        Review saved = reviewRepository.save(review);

        return toDto(saved);
    }

    private void updateProductRating(Long productId) {
        Double avgRating = reviewRepository.getAverageRating(productId);
        Integer reviewCount = reviewRepository.getReviewCount(productId);

        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            product.setRating(avgRating != null ? avgRating : 0.0);
            product.setReviewCount(reviewCount != null ? reviewCount : 0);
            productRepository.save(product);
        }
    }

    private ReviewDto toDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .userName(review.getUserName())
                .userAvatar(review.getUserAvatar())
                .rating(review.getRating())
                .comment(review.getComment())
                .helpful(review.getHelpful())
                .verified(review.getVerified())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
