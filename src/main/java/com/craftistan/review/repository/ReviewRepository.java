package com.craftistan.review.repository;

import com.craftistan.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdAndIsHiddenFalseOrderByCreatedAtDesc(Long productId, Pageable pageable);

    Optional<Review> findByProductIdAndUserId(Long productId, String userId);

    boolean existsByProductIdAndUserId(Long productId, String userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
    Double getAverageRating(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId")
    Integer getReviewCount(@Param("productId") Long productId);
}
