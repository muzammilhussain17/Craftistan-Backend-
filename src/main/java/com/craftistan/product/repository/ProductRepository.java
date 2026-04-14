package com.craftistan.product.repository;

import com.craftistan.product.entity.ApprovalStatus;
import com.craftistan.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    // ========================
    // PUBLIC - Approved only
    // ========================

    Page<Product> findByIsActiveTrueAndApprovalStatus(ApprovalStatus status, Pageable pageable);

    Page<Product> findByCategoryAndIsActiveTrueAndApprovalStatus(
            String category, ApprovalStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
           "AND p.approvalStatus = 'APPROVED' " +
           "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchProducts(@Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
           "AND p.approvalStatus = 'APPROVED' " +
           "AND (:category IS NULL OR p.category = :category) " +
           "AND (:style IS NULL OR p.style = :style) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findByFilters(
            @Param("category") String category,
            @Param("style") String style,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    Page<Product> findByIsNewTrueAndIsActiveTrueAndApprovalStatus(
            ApprovalStatus status, Pageable pageable);

    Page<Product> findByIsFeaturedTrueAndIsActiveTrueAndApprovalStatus(
            ApprovalStatus status, Pageable pageable);

    // ========================
    // ARTISAN - own products
    // ========================
    Page<Product> findByArtisanIdAndIsActiveTrue(String artisanId, Pageable pageable);
    List<Product> findByArtisanId(String artisanId);

    // ========================
    // ADMIN - all products
    // ========================
    Page<Product> findByApprovalStatus(ApprovalStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
           "(:status IS NULL OR p.approvalStatus = :status) AND " +
           "(:artisanId IS NULL OR p.artisanId = :artisanId)")
    Page<Product> findByFiltersAdmin(
            @Param("status") ApprovalStatus status,
            @Param("artisanId") String artisanId,
            Pageable pageable);

    long countByApprovalStatus(ApprovalStatus status);
}
