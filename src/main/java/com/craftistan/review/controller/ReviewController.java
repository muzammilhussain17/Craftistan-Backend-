package com.craftistan.review.controller;

import com.craftistan.common.dto.ApiResponse;
import com.craftistan.review.dto.CreateReviewRequest;
import com.craftistan.review.dto.ReviewDto;
import com.craftistan.review.service.ReviewService;
import com.craftistan.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product review endpoints")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/products/{productId}/reviews")
    @Operation(summary = "Get product reviews")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getProductReviews(
            @PathVariable Long productId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ReviewDto> reviews = reviewService.getProductReviews(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @PostMapping("/api/products/{productId}/reviews")
    @Operation(summary = "Add a review")
    public ResponseEntity<ApiResponse<ReviewDto>> createReview(
            @PathVariable Long productId,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal User user) {
        ReviewDto review = reviewService.createReview(productId, request, user);
        return ResponseEntity.ok(ApiResponse.success(review, "Review added successfully"));
    }

    @PutMapping("/api/reviews/{reviewId}")
    @Operation(summary = "Update a review")
    public ResponseEntity<ApiResponse<ReviewDto>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal User user) {
        ReviewDto review = reviewService.updateReview(reviewId, request, user);
        return ResponseEntity.ok(ApiResponse.success(review, "Review updated successfully"));
    }

    @DeleteMapping("/api/reviews/{reviewId}")
    @Operation(summary = "Delete a review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user) {
        reviewService.deleteReview(reviewId, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully"));
    }

    @PostMapping("/api/reviews/{reviewId}/helpful")
    @Operation(summary = "Mark review as helpful")
    public ResponseEntity<ApiResponse<ReviewDto>> markHelpful(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user) {
        ReviewDto review = reviewService.markHelpful(reviewId, user);
        return ResponseEntity.ok(ApiResponse.success(review));
    }
}
